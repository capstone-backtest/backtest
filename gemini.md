# Gemini CLI Project Context: 주식 백테스팅 플랫폼

## 1. 프로젝트 개요
이 프로젝트는 FastAPI(백엔드)와 React(프론트엔드)를 사용한 주식 백테스팅 웹 애플리케이션입니다. 사용자는 특정 주식 종목과 투자 전략을 선택하여 과거 데이터를 기반으로 성과를 시뮬레이션하고 분석할 수 있습니다. 전체 서비스는 Docker Compose를 통해 관리됩니다.

## 2. 기술 스택
- **Backend**: Python, FastAPI, SQLAlchemy, Alembic, Pandas, Pytest
- **Frontend**: TypeScript, React, Vite, Tailwind CSS
- **Database**: PostgreSQL
- **Web Server**: Nginx (리버스 프록시)
- **Containerization**: Docker, Docker Compose

## 3. 핵심 명령어
- **전체 서비스 시작 (백그라운드)**: `docker-compose up -d`
- **전체 서비스 중지**: `docker-compose down`
- **백엔드 테스트 실행**: `docker-compose exec backend pytest` 또는 `cd backend && pytest`
- **프론트엔드 개발 서버 실행**: `cd frontend && npm run dev`
- **데이터베이스 마이그레이션 (Alembic)**: `docker-compose exec backend alembic upgrade head`

## 4. 디렉토리 구조
- `backend/`: FastAPI 백엔드 애플리케이션 소스 코드
  - `app/api/`: API 엔드포인트 정의
  - `app/services/`: 비즈니스 로직 (백테스팅, 데이터 처리 등)
  - `app/models/`: 데이터베이스 모델 및 Pydantic 스키마
  - `strategies/`: 투자 전략 구현 파일
- `frontend/`: React 프론트엔드 애플리케이션 소스 코드
  - `src/`: 주요 소스 코드
- `database/`: 데이터베이스 초기화 스크립트
- `nginx/`: Nginx 설정 파일
- `data_cache/`: 주식 데이터 캐시 파일 (주로 CSV)

## 5. 주요 아키텍처 및 컨벤션
- **API 버전 관리**: 백엔드 API는 `app/api/v1/` 경로를 통해 버전 관리됩니다.
- **의존성 관리**:
  - 백엔드: `backend/requirements.txt`
  - 프론트엔드: `frontend/package.json`
- **API 문서**: 백엔드 서버 실행 후 `http://localhost:8000/docs` 에서 Swagger UI를 통해 확인할 수 있습니다.
- **상태 관리 (프론트엔드)**: (추정) React Context API 또는 Zustand/Redux와 같은 라이브러리 사용 가능성이 있습니다. (정확한 정보는 `frontend/src` 내부 확인 필요)

## 6. 기타
- `log.txt`: 터미널 외부에서 발생하는 로그 기록용 파일입니다.