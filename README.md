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

*   **백엔드**: FastAPI, MySQL, SQLAlchemy, Alembic, backtesting.py, pandas, yfinance, Uvicorn
*   **프론트엔드**: React 18, TypeScript, Vite, Bootstrap, React Bootstrap, Tailwind CSS, Recharts, Axios
*   **컨테이너화**: Docker, Docker Compose
*   **웹 서버**: Nginx

## 4. 프로젝트 구조

```
backtest/
├── backend/            # FastAPI 백엔드 API 서버
├── frontend/           # React 프론트엔드 대시보드
├── database/           # 데이터베이스 초기화 스크립트
├── nginx/              # 리버스 프록시를 위한 Nginx 설정
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

2.  **서비스 빌드 및 실행**:
    ```bash
    # Docker Compose v1
    docker-compose up --build -d

    # 또는 Docker Compose v2 (권장)
    docker compose up --build -d
    ```

### 애플리케이션 접속

스크립트 또는 수동 명령어로 서비스가 모두 실행되면, 웹 브라우저에서 `http://localhost:8080` 주소로 프론트엔드 대시보드에 접속할 수 있습니다.

## 6. 개발

프로젝트 각 부분에 대한 자세한 개발 가이드는 해당 `README.md` 파일을 참조하십시오:

*   **백엔드 개발**: `backend/README.md`
*   **프론트엔드 개발**: `frontend/README.md`

## 7. 라이선스

이 프로젝트는 GNU General Public License v3.0 (GPL-3.0) 기반으로 배포됩니다. 자세한 내용과 저작권 조건은 `LICENSE` 파일을 참조하세요.