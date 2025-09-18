# 개발 가이드

이 문서는 백테스팅 플랫폼의 프론트엔드/백엔드 개발 환경 구성과 주요 워크플로우를 설명합니다. 현재 모노레포는 `frontend`(React), `fastapi`(전략 엔진), `spring`(인증·커뮤니티) 세 서비스를 포함합니다.

## 1. 개발 환경 설정

### 1.1 필수 요구사항
- Docker & Docker Compose (v2 이상)
- Git
- 옵션: Node.js 20+, pnpm, Python 3.11+, Java 21 (각 서비스 개별 실행 시)

### 1.2 Docker Compose 기반 개발
```bash
# 개발 환경 전체 기동
docker compose -f compose.yml -f compose/compose.dev.yml up --build

# 백그라운드 실행
docker compose -f compose.yml -f compose/compose.dev.yml up -d

# 서비스별 재시작
docker compose -f compose.yml -f compose/compose.dev.yml restart fastapi spring frontend

# 종료 및 볼륨 정리
docker compose -f compose.yml -f compose/compose.dev.yml down -v
```

### 1.3 환경 변수
- FastAPI: `fastapi/.env` 참고 (기본값 제공).
- Spring Boot: Docker Compose에서 `SPRING_*` 환경변수 주입. 로컬 실행 시 `spring/src/main/resources/application.properties` 또는 `--spring.config.location` 사용.
- 공통 MySQL/Redis 자격 증명은 `compose.yml`과 `database/schema.sql` 참고.

## 2. 백엔드 개발

### 2.1 FastAPI (전략 엔진)

- **기술 스택**: FastAPI, Pydantic v2, SQLAlchemy, Celery, yfinance, Redis
- **주요 역할**: 백테스트 실행, 전략 최적화, 시계열 데이터 수집/캐시, 백테스트 히스토리 관리

```
fastapi/
├── app/
│   ├── api/            # REST 라우터
│   ├── services/       # 백테스트/데이터 서비스
│   ├── domains/        # DDD 모델 (Backtest, Portfolio 등)
│   ├── repositories/   # DB/Redis 어댑터
│   ├── events/         # 도메인 이벤트
│   └── core/           # 설정, 예외 처리, 의존성 주입
├── strategies/         # 사용자 정의 전략 예시
├── tests/              # pytest 기반 테스트
└── run_server.py       # 개발용 엔트리포인트
```

```bash
# 의존성 설치 및 개발 서버 실행
cd fastapi
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8001

# 테스트
pytest
```

주요 엔드포인트
- `POST /api/v1/backtest/run` : 단일 전략 백테스트
- `POST /api/v1/backtest/portfolio` : 포트폴리오 백테스트
- `GET /api/v1/backtest/history` : 실행 이력 조회
- `GET /api/v1/naver-news/search` : 네이버 뉴스 검색

### 2.2 Spring Boot (인증 · 커뮤니티 · 채팅)

- **기술 스택**: Spring Boot 3.3, Spring Security, JPA(Hibernate), WebSocket(STOMP), MySQL, Redis(향후)
- **주요 역할**: 회원가입/로그인, JWT 세션 관리, 커뮤니티 게시판, 실시간 채팅, 사용자 프로필

```
spring/
├── src/main/java/com/backtest/
│   ├── auth/          # AuthController, AuthService, JWT
│   ├── user/          # 사용자 프로필/정보조회
│   ├── community/     # 게시글/댓글/좋아요 도메인
│   ├── chat/          # 채팅방, 메시지, WebSocket 설정
│   └── global/        # 보안 설정, 예외 처리, 공용 설정
├── src/test/java/     # SpringBootTest + H2(MySQL 모드)
├── build.gradle       # Gradle 8.10, Java Toolchain 21
└── Dockerfile         # 멀티 스테이지 이미지
```

```bash
# 개발 서버 실행
cd spring
./gradlew bootRun

# 단위/통합 테스트 (H2 MySQL 모드)
./gradlew test

# 패키징
./gradlew build
```

주요 REST/WS 엔드포인트
- 인증: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`
- 사용자: `GET /api/users/me`, `PUT /api/users/me`
- 커뮤니티: `GET/POST /api/community/posts`, `POST /api/community/posts/{id}/comments`, `POST /api/community/posts/{id}/like`
- 채팅: `POST /api/chat/rooms`, `GET /api/chat/rooms/{id}/messages`, `POST /api/chat/messages`
- WebSocket: `/ws` (SockJS), 구독 채널 `/topic/chat/{roomId}`

## 3. 프론트엔드 개발

- **기술 스택**: React 18, TypeScript, Vite, Tailwind CSS, shadcn/ui, TanStack Query

```
frontend/
├── src/
│   ├── app/            # 라우팅 & 페이지 구성
│   ├── components/     # UI 컴포넌트
│   ├── features/       # 도메인 단위 상태 및 UI
│   ├── hooks/          # 공용 훅
│   └── lib/            # API 클라이언트, 유틸리티
├── public/             # 정적 에셋
└── tests/              # Vitest 설정
```

```bash
cd frontend
pnpm install
pnpm dev --host --port 5174
pnpm test
```

## 4. 아키텍처 개요

- 상위 개요와 데이터 흐름은 [`docs/ARCHITECTURE_GUIDE.md`](ARCHITECTURE_GUIDE.md)를 참고하세요.
- 데이터베이스 스키마는 `database/schema.sql`, `database/yfinance.sql`에서 관리합니다.
- 로컬 테스트 시 Spring Boot는 H2(MySQL 모드), FastAPI는 SQLite/메모리 DB를 활용할 수 있습니다.

## 5. 디버깅 & 공통 스크립트

- `scripts/test-runner.sh`: `unit`, `integration`, `all` 옵션으로 서비스별 테스트 일괄 실행.
- `scripts/warmup.sh`: 캐시 프리로드, 샘플 데이터 로드 (추후 제공).
- 로그 확인: `docker compose logs -f fastapi`, `docker compose logs -f spring`, `docker compose logs -f frontend`.

## 6. 트러블슈팅 메모

- **Gradle 빌드 실패**: 로컬 JDK가 없다면 Docker 또는 Gradle Toolchain이 자동으로 Temurin 21을 내려받습니다. 그래도 실패하면 `./gradlew --stop && ./gradlew clean`. 
- **DB 스키마 오류**: `database/schema.sql`을 수정한 경우 `docker compose down -v` 후 재기동하여 초기화.
- **JWT 시크릿**: `jwt.secret`는 Base64 또는 Hex 문자열을 지원합니다. 최소 256비트 키를 사용하세요.
- **WebSocket CORS**: 개발 환경에서는 모든 오리진 허용, 운영환경에서는 `cors.allowed-origins` 프로퍼티로 제한하세요.

---

추가 질문이나 개선 제안은 PR 또는 Issue로 남겨주세요.
