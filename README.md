# 백테스팅 플랫폼 모노레포

리액트 프론트엔드, FastAPI 전략 엔진, Spring Boot 커뮤니티/인증 백엔드가 하나의 모노레포로 구성된 투자 전략 백테스팅 플랫폼입니다. 도커 컴포즈 한 번으로 전체 스택을 개발/운영 환경에서 기동할 수 있도록 구성했습니다.

## 아키텍처 한눈에 보기

```
┌────────────┐   ┌─────────────┐   ┌─────────────┐
│ Frontend   │   │ FastAPI     │   │ Spring Boot │
│ React/Vite │   │ 백테스트 엔진│   │ 인증·커뮤니티 │
└─────┬──────┘   └──────┬──────┘   └──────┬──────┘
      │                 │                 │
      ▼                 ▼                 ▼
┌────────────┐   ┌─────────────┐   ┌────────────────┐
│  사용자 UI │   │ 전략 시뮬레이션│   │ 회원·게시글·채팅 │
└────────────┘   └─────────────┘   └────────┬───────┘
                                            ▼
                                  ┌────────────────┐
                                  │ MySQL / Redis │
                                  └────────────────┘
```

- **FastAPI (Python)**: 전략 실행, 시계열 데이터 수집 및 캐시, 백테스트 결과 산출.
- **Spring Boot (Java)**: 회원 인증/인가, 커뮤니티 게시판, 실시간 채팅(WebSocket/STOMP).
- **Frontend (React/TypeScript)**: 투자 전략 작성, 결과 시각화, 커뮤니티 UI.
- **데이터 스토어**: MySQL(통합 스키마), Redis(캐시/큐), Testcontainers 기반 테스트 지원.

## 주요 디렉터리 구조

```
backend/           # (deprecated) 과거 FastAPI 코드 백업 - 참고용
fastapi/           # FastAPI 백엔드 (전략 엔진)
frontend/          # React + Vite + Tailwind + shadcn/ui 프론트엔드
spring/            # Spring Boot 백엔드 (인증, 커뮤니티, 채팅)
database/          # MySQL 스키마 & 시드 데이터
compose/           # 환경별 docker compose 오버레이
scripts/           # 공용 스크립트 (테스트, 유틸)
docs/              # 아키텍처/운영/테스트 문서 모음
```

### Spring Boot 서브 모듈 요약 (`spring/`)
- `com.backtest.auth`: 회원가입/로그인/토큰 재발급, JWT 기반 인증.
- `com.backtest.user`: 사용자 프로필 API.
- `com.backtest.community`: 게시글·댓글·좋아요 CRUD.
- `com.backtest.chat`: 채팅방 관리 및 STOMP 브로드캐스트 (`/ws`, `/topic/chat/{roomId}`).
- `com.backtest.global`: CORS, 보안, 예외 처리 등 공용 컴포넌트.

## 빠른 시작 (Docker Compose)

> **사전 준비**: Docker + Docker Compose, 4GB 이상 메모리 권장.

```bash
# 개발 환경(핫 리로드 포함)
docker compose -f compose.yml -f compose/compose.dev.yml up --build -d

# 로그 확인
docker compose logs -f fastapi spring frontend

# 종료
docker compose -f compose.yml -f compose/compose.dev.yml down
```

### 서비스 포트
- **Frontend**: http://localhost:5174
- **FastAPI**: http://localhost:8001 (`/api/v1/**`)
- **Spring Boot**: http://localhost:8080 (`/api/**`, `/ws`)
- **OpenAPI 문서**:
  - FastAPI: http://localhost:8001/api/v1/docs
  - Spring Boot: http://localhost:8080/swagger-ui/index.html
- **MySQL**: localhost:3306 (`root/password`)
- **Redis**: localhost:6379

## 개별 서비스 실행 팁

### Spring Boot 백엔드
```bash
cd spring
./gradlew bootRun      # 개발 서버 (http://localhost:8080)
./gradlew test         # H2 (MySQL 모드) 기반 테스트 전체 실행
./gradlew build        # JAR 생성 (build/libs/*.jar)
```
- 기본 설정: `spring/src/main/resources/application.properties`
- 테스트 프로필: `src/test/resources/application-test.properties`
- JWT 비밀키는 Base64 또는 Hex 문자열 모두 지원합니다.

### FastAPI 백엔드
```bash
cd fastapi
poetry install  # 또는 requirements.txt 기반 가상환경 구성
uvicorn app.main:app --reload --host 0.0.0.0 --port 8001
pytest          # FastAPI 단위/통합 테스트
```

### 프론트엔드
```bash
cd frontend
pnpm install
pnpm dev --host --port 5174
pnpm test
```

## 테스트 실행

| 대상 | 명령어 | 비고 |
| ---- | ------ | ---- |
| 전체 스택 (Docker) | `./scripts/test-runner.sh all` | 서비스별 테스트 일괄 실행 |
| Spring Boot | `cd spring && ./gradlew test` | H2(MySQL 모드) + Mock STOMP |
| FastAPI | `cd fastapi && pytest` | pytest + coverage |
| Frontend | `cd frontend && pnpm test` | Vitest + Testing Library |

## 배포 참고

- 생산 환경에서는 `compose/compose.prod.yml` 오버레이 사용.
- Jenkins 파이프라인 예시는 `Jenkinsfile`, CI 관련 가이드는 `docs/jenkins_troubleshooting.md` 참고.
- Spring Boot 이미지는 멀티 스테이지 Dockerfile로 빌드(`spring/Dockerfile`), Gradle wrapper와 Toolchain을 활용해 항상 Java 21로 컴파일됩니다.

## 추가 문서

- [`docs/ARCHITECTURE_GUIDE.md`](docs/ARCHITECTURE_GUIDE.md): 멀티 백엔드 아키텍처 및 데이터 흐름 상세.
- [`docs/DEVELOPMENT_GUIDE.md`](docs/DEVELOPMENT_GUIDE.md): 로컬 개발/테스트 환경 세팅.
- [`docs/TESTING_GUIDE.md`](docs/TESTING_GUIDE.md): 서비스별 테스트 전략.
- [`docs/RUNBOOK.md`](docs/RUNBOOK.md): 장애 대응 및 운영 가이드.

기여 또는 개선 제안은 PR/이슈로 남겨 주세요. 🙌
