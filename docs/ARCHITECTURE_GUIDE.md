# 아키텍처 가이드

이 문서는 백테스팅 플랫폼의 전체 아키텍처, 서비스 간 경계, 데이터 흐름을 설명합니다. 2024년 3분기 기준으로 시스템은 `FastAPI 전략 엔진`과 `Spring Boot 커뮤니티/인증`으로 백엔드가 이원화되어 있습니다.

## 1. 시스템 개요

### 1.1 구성 요소

```
┌────────────────────┐
│    Frontend (Vite) │
│ React • TS • shadcn │
└─────────┬──────────┘
          │ REST/WS
          ▼
┌────────────────────┐         ┌────────────────────┐
│ FastAPI Backtester │  gRPC?  │ Spring Boot Social │
│ 전략 시뮬레이션     │◄──────►│ 인증·커뮤니티·채팅 │
└─────────┬──────────┘         └─────────┬──────────┘
          │                                    │
          ▼                                    ▼
┌────────────────────┐         ┌────────────────────┐
│  Redis Cache       │         │ MySQL 8 (공유 스키마) │
│  백테스트 메타/큐   │         │ 사용자·커뮤니티 데이터 │
└────────────────────┘         └────────────────────┘
```

| 계층 | 주요 역할 | 기술 스택 |
| --- | --- | --- |
| 프런트엔드 | 전략 작성, 결과 시각화, 커뮤니티 UI | React 18, TypeScript, Vite, Tailwind CSS, shadcn/ui |
| 전략 엔진 | 백테스트 실행, 데이터 수집/캐시, 리스크 분석 | FastAPI, Pydantic, SQLAlchemy, Celery, Redis |
| 커뮤니티 백엔드 | JWT 인증/인가, 회원/권한, 게시판, 실시간 채팅 | Spring Boot 3.3, JPA, WebSocket(STOMP), Spring Security |
| 데이터 레이어 | 트랜잭션 데이터, 캐시 | MySQL 8, Redis 7, yfinance, Testcontainers |

### 1.2 서비스 경계

- **FastAPI 서비스** (`fastapi/`)
  - 전략 파라미터 검증 및 실행 파이프라인
  - 시계열 데이터 수집 (Yahoo Finance, 네이버 뉴스)
  - 백테스트 결과/리포트 계산, 캐시 업데이트
  - REST API: `/api/v1/**`
- **Spring Boot 서비스** (`spring/`)
  - 회원 가입/로그인, JWT 토큰 발급 및 세션 관리
  - 커뮤니티 게시글/댓글/좋아요 API (`/api/community/**`)
  - 사용자 프로필 관리 (`/api/users/**`)
  - WebSocket(STOMP) 기반 실시간 채팅 (`/ws`, `/topic/chat/{roomId}`)
  - REST API: `/api/auth/**`, `/api/users/**`, `/api/chat/**`

두 서비스는 MySQL 스키마와 이벤트(향후 Kafka 도입 예정)를 통해 데이터를 공유하며, HTTP API를 통해 상호 연동합니다. 예를 들어 백테스트 히스토리는 FastAPI가 생성하고, Spring Boot가 사용자와의 매핑을 수행합니다.

## 2. 인프라 & 배포

- **컨테이너 오케스트레이션**: Docker Compose (`compose/compose.*.yml`)
  - 공통: `fastapi`, `spring`, `frontend`, `mysql`, `redis`
  - 개발용 오버레이: 라이브 리로드, 볼륨 마운트, 테스트 DB
  - 프로덕션 오버레이: 멀티 스테이지 이미지, 헬스체크 강화
- **CI/CD**: Jenkins 파이프라인 (`Jenkinsfile`)에서 서비스별 빌드 → 통합 테스트 → 이미지 푸시 → 배포 순으로 수행
- **모니터링**: Spring Boot Actuator (`/actuator/**`), FastAPI metrics endpoint, Prometheus/Grafana (준비 중)

## 3. 도메인 구조

### 3.1 FastAPI (전략 엔진)

```
fastapi/app/
├── api/          # REST 라우터 (전략 실행, 보고서 조회)
├── services/     # 애플리케이션 서비스 (백테스트, 최적화, 데이터 파이프라인)
├── domains/      # 도메인 모델 (Backtest, Portfolio, MarketData 등)
├── repositories/ # DB/캐시 어댑터 (SQLAlchemy, Redis)
└── events/       # 도메인 이벤트 & 핸들러
```

- **DDD**: Backtest/Portfolio/Data 도메인으로 구분, CQRS 스타일 명령/조회 객체 활용.
- **이벤트 흐름**: 백테스트 완료 → 결과 이벤트 발행 → 리포트/알림/히스토리 업데이트.
- **데이터**: 전략 결과는 `backtest_history`, 시계열 데이터는 `stock_data_cache` 스키마에 저장.

### 3.2 Spring Boot (커뮤니티/인증)

```
spring/src/main/java/com/backtest/
├── auth/         # AuthController, AuthService, UserSession 엔티티
├── user/         # 사용자 조회/프로필 수정 API
├── community/    # 게시글·댓글·좋아요 도메인 + 서비스
├── chat/         # 채팅방, 메시지, WebSocket/STOMP 핸들러
└── global/       # 보안 설정, CORS, 예외 처리, JWT 유틸리티
```

- **보안**: Spring Security + JWT(HS256). `JwtAuthenticationFilter`가 Authorization 헤더를 검증하고 SecurityContext를 구성합니다.
- **데이터 접근**: Spring Data JPA + Hibernate. MySQL ENUM 컬럼은 AttributeConverter로 대/소문자 이슈 해결.
- **채팅**: `/ws` 엔드포인트에 SockJS/STOMP 지원. 메시지는 `SimpMessagingTemplate`을 통해 `/topic/chat/{roomId}`로 브로드캐스팅 되고, 영속화하여 이력을 남깁니다.
- **테스트**: H2(MySQL 모드) 프로파일 + SpringBootTest, STOMP는 Mock 브로커로 검증.

## 4. 데이터베이스

### 4.1 스키마 구분

모든 테이블은 `database/schema.sql`에 정의되어 있으며, 역할상으로 다음과 같이 나뉩니다.

| 범주 | 담당 서비스 | 주요 테이블 |
| ---- | ---------- | ----------- |
| 회원/인증 | Spring Boot | `users`, `user_sessions`, `user_social_accounts` |
| 커뮤니티 | Spring Boot | `posts`, `post_comments`, `post_likes`, `comment_likes`, `notices`, `reports` |
| 채팅 | Spring Boot | `chat_rooms`, `chat_room_members`, `chat_messages` |
| 백테스트 | FastAPI + Spring Boot | `backtest_history`, `strategy_favorites`, `backtest_metrics` |
| 시세 캐시 | FastAPI | `stocks`, `daily_prices`, `dividends`, `cache_metadata` |

### 4.2 데이터 흐름

```
사용자 → Frontend → (JWT 포함 요청)
          ├─ Spring Boot → 사용자 검증, 커뮤니티 콘텐츠, 채팅
          │               ↕ MySQL (users/posts/rooms ...)
          │               ↔ Redis (채팅 세션 캐시 예정)
          └─ FastAPI     → 전략 검증 & 실행 → 결과 저장(Backtest History)
                          ↕ MySQL (백테스트 결과)
                          ↕ Redis (데이터 캐시)
                          ↔ 외부 API (Yahoo Finance, Naver News)
```

- Spring Boot는 `backtest_history.user_id`를 통해 FastAPI 결과와 사용자를 연결합니다.
- 향후 이벤트 브로커(Kafka) 도입 시, 백테스트 완료 이벤트 → 커뮤니티에 자동 공유 등의 워크플로우를 구성할 예정입니다.

## 5. 보안 & 인증 흐름

1. **회원가입/로그인**: `/api/auth/register`, `/api/auth/login` → Access/Refresh 토큰 발급.
2. **요청 보호**: `SecurityConfig`에서 `/api/auth/**`, `/health`, `/actuator/**`, `/swagger-ui/**`를 제외한 모든 요청에 인증을 요구합니다.
3. **토큰 검증**: `Authorization: Bearer <token>` 헤더 → `JwtAuthenticationFilter` → `CustomUserDetailsService` 로드.
4. **리프레시**: `/api/auth/refresh`에서 Refresh 토큰 검증 후 새 토큰 발급, 세션 갱신.
5. **로그아웃**: `/api/auth/logout` → 세션 레코드 `is_revoked` 처리.

## 6. 실시간 채팅

- STOMP 엔드포인트: `/ws` (SockJS fallback 지원)
- Application Prefix: `/app`
- Subscribe Prefix: `/topic`
- 메시지 흐름: 클라이언트 `/app/chat/{roomId}` 전송 → `ChatService.sendMessage` → DB 저장 → `/topic/chat/{roomId}` 브로드캐스트.
- REST 보조 API: `/api/chat/rooms`, `/api/chat/messages`, `/api/chat/rooms/{id}/messages`.

## 7. 테스트 전략

| 계층 | 도구 | 비고 |
| --- | --- | --- |
| Spring Boot | JUnit 5, AssertJ, SpringBootTest, H2(MySQL 모드) | 인증/커뮤니티/채팅 서비스 단위 테스트 |
| FastAPI | pytest, httpx, pytest-asyncio | API/도메인/리포트 검증 |
| Frontend | Vitest, Testing Library | 컴포넌트/훅/상태 관리 테스트 |
| 통합 | docker compose + scripts/test-runner.sh | 서비스 간 계약 검증 |

## 8. 향후 로드맵

- Kafka 기반 이벤트 아키텍처 도입 (백테스트 완료 → 커뮤니티 자동 공유, 알림).
- Spring Boot ↔ FastAPI 간 gRPC(or REST) 비동기 호출 정비.
- 채팅 서버 확장성 강화를 위한 Redis Pub/Sub 또는 메시지 브로커 연동.
- Zero-downtime 배포를 위한 Blue/Green 전략 및 헬스체크 고도화.

---

자세한 개발 및 운영 절차는 [`docs/DEVELOPMENT_GUIDE.md`](DEVELOPMENT_GUIDE.md)와 [`docs/RUNBOOK.md`](RUNBOOK.md)를 참고하세요.
