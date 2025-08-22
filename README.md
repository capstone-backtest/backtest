# 주식 백테스팅 플랫폼

## 1. 개요

이 프로젝트는 주식 투자 전략을 분석하고 최적화하기 위해 설계된 종합적인 백테스팅 플랫폼입니다. FastAPI 기반의 백엔드 API 서버와 React 기반의 프론트엔드 대시보드로 구성되어 있습니다. 사용자는 다양한 투자 전략을 정의하고, 과거 데이터를 기반으로 백테스트를 실행하며, 수익률, 변동성, 최대 낙폭과 같은 성과 지표를 시각화할 수 있습니다.

## 2. 주요 기능

*   **전략 백테스팅**: 사용자가 정의한 투자 전략을 과거 주식 데이터에 대해 시뮬레이션합니다.
*   **상세한 성과 분석**: 총수익률, 연평균 성장률(CAGR), 샤프 지수, 최대 낙폭(MDD) 등 20가지 이상의 상세한 성과 지표를 제공합니다.
*   **결과 저장**: 모든 백테스트 결과는 추적 및 비교 분석을 위해 MySQL 데이터베이스에 저장됩니다.
*   **파라미터 최적화**: 전략의 성과를 극대화하기 위한 최적의 파라미터 조합을 탐색합니다.
*   **동적 전략 관리**: `strategies` 디렉터리에 새로운 전략 파일을 추가하여 API를 통해 동적으로 등록하고 테스트할 수 있습니다.
*   **차트 데이터 제공**: 웹 기반 차트 라이브러리(예: Recharts, ECharts)와 쉽게 통합할 수 있는 JSON 형식의 차트 데이터를 생성합니다.
*   **비동기 처리**: FastAPI를 기반으로 구축되어 여러 요청을 효율적으로 처리하기 위한 비동기 처리를 지원합니다.

## 3. 기술 스택

*   **백엔드**: FastAPI, MySQL, SQLAlchemy, backtesting.py, pandas, yfinance, Uvicorn
*   **프론트엔드**: React 18, TypeScript, Vite, Bootstrap, React Bootstrap, Tailwind CSS, Recharts, Axios
*   **컨테이너화**: Docker, Docker Compose
*   **웹 서버(개발)**: Vite 개발 서버 (프로덕션에서는 정적 빌드를 별도 서버로 서빙 가능)

## 4. 프로젝트 구조

```
backtest/
├── backend/            # FastAPI 백엔드 API 서버
├── frontend/           # React 프론트엔드 대시보드
├── database/           # 데이터베이스 초기화 스크립트
...existing code...
├── docker-compose.yml  # Docker Compose 설정
├── LICENSE             # 프로젝트 라이선스
└── README.md           # 프로젝트 루트 README
```

## 5. 시작하기

이 프로젝트는 Docker Compose를 사용하여 모든 서비스를 간편하게 실행할 수 있도록 구성되어 있습니다.

### 사전 요구사항

*   Docker
*   Docker Compose

### 실행

프로젝트 전체 서비스를 실행하려면 Docker와 Docker Compose가 설치되어 있어야 합니다.

1.  **리포지토리 클론 및 `.env` 파일 생성**:
    ```bash
    git clone <repository-url>
    cd backtest
    cp backend/env.example backend/.env
    cp frontend/.env.example frontend/.env
    ```

2.  **개발 환경 (권장)**:

    개발 전용 Compose 파일은 `docker-compose.dev.yml`에 보관되어 있으며 자동 병합을 기대하지 않습니다. 개발 환경을 실행하려면 아래처럼 명시적으로 파일을 지정하세요.

    ```bash
    # Docker Compose v1
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build

    # 또는 Docker Compose v2 (권장)
    docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
    ```

    백그라운드 실행을 원하면 `-d`를 추가하세요. 개발 구성에서는 프론트엔드가 Vite 개발 서버로 동작하므로 브라우저에서 `http://localhost:5173`로 접속합니다.

3.  **프로덕션/미리보기(정적 서빙)**:

    프로덕션 빌드(또는 미리보기)를 위해서는 기본 `docker-compose.yml`과 `docker-compose.prod.yml`을 명시적으로 사용합니다. 이 구성은 프론트엔드를 정적 빌드로 서빙(컨테이너 내부 Nginx, 호스트 포트 8080)합니다.

    ```bash
    docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
    ```

    또는 `docker compose -f docker-compose.prod.yml up -d --build`로 프로덕션 전용 파일만 사용할 수 있습니다.

### 애플리케이션 접속

서비스가 실행되면, 웹 브라우저에서 다음 주소로 접속하세요:

- 개발 (dev 파일 사용): 프론트엔드(Vite) http://localhost:5173
- 프로덕션/미리보기: 프론트엔드 (정적 서빙) http://localhost:8080
- 백엔드(FastAPI): http://localhost:8000

## 6. 개발

프로젝트 각 부분에 대한 자세한 개발 가이드는 해당 `README.md` 파일을 참조하십시오:

*   **백엔드 개발**: `backend/README.md`
*   **프론트엔드 개발**: `frontend/README.md`

## 참고 (데이터 캐시 관련)

- 현재 구현은 CSV 기반 로컬 캐시 대신 MySQL(DB)을 1차 저장소/캐시로 사용하는 DB-first 흐름을 따릅니다.
- yfinance 관련 API 및 DB 캐시 동작(예: `/api/v1/yfinance/fetch-and-cache`, v2의 DB 보충 동작)은 `doc/backend/api.md`에 자세히 정리되어 있으니 해당 문서를 참조하세요.

## 7. 라이선스

이 프로젝트는 GNU General Public License v3.0 (GPL-3.0) 기반으로 배포됩니다. 자세한 내용과 저작권 조건은 `LICENSE` 파일을 참조하세요.