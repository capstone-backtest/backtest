# Backtesting API Server

## 1. 개요

이 프로젝트는 주식 투자 전략의 성과를 분석하고 최적화하기 위한 FastAPI 기반의 REST API 서버입니다. 사용자는 다양한 투자 전략을 정의하고, 과거 데이터를 기반으로 백테스팅을 실행하여 전략의 수익률, 변동성, 최대 손실 등 다양한 성과 지표를 확인할 수 있습니다.

백테스트 결과는 MySQL 데이터베이스에 저장되어 영속적으로 관리됩니다.

## 2. 주요 기능

*   **전략 백테스팅**: 사용자가 정의한 투자 전략을 과거 주가 데이터에 적용하여 성과를 시뮬레이션합니다.
*   **상세한 성과 분석**: 총 수익률, 연평균 복리 수익률(CAGR), 샤프 비율, 최대 손실(MDD) 등 20가지 이상의 상세한 성과 지표를 제공합니다.
*   **결과 영속성**: 모든 백테스트 결과를 MySQL 데이터베이스에 저장하여 추적 및 비교 분석이 가능합니다.
*   **파라미터 최적화**: 전략의 성능을 극대화하는 최적의 파라미터 조합을 탐색합니다.
*   **동적 전략 관리**: `strategies` 디렉터리에 새로운 전략 파일을 추가하여 동적으로 API에 등록하고 테스트할 수 있습니다.
*   **차트 데이터 제공**: 웹 기반 차트 라이브러리(Recharts, ECharts 등)와 쉽게 연동할 수 있는 JSON 형식의 차트 데이터를 생성합니다.
*   **비동기 처리**: FastAPI를 기반으로 비동기 처리를 지원하여 여러 요청을 효율적으로 처리합니다.

## 3. 기술 스택

*   **Framework**: FastAPI
*   **Database**: MySQL
*   **ORM**: SQLAlchemy
*   **Migrations**: Alembic
*   **Core Logic**: backtesting.py, pandas, yfinance
*   **Server**: Uvicorn

## 4. 프로젝트 구조

```
backend/
├── alembic/            # Alembic 마이그레이션 스크립트
├── app/                # FastAPI 서버 핵심 코드
│   ├── api/            # API 라우팅 (v1 포함)
│   ├── core/           # 설정, 예외 등 핵심 기능
│   ├── db/             # 데이터베이스 모델 및 세션 관리
│   ├── models/         # Pydantic 데이터 모델 (요청/응답)
│   ├── services/       # 핵심 비즈니스 로직
│   └── utils/          # 공통 유틸리티 함수
├── strategies/         # 커스텀 투자 전략 파일
├── alembic.ini         # Alembic 설정 파일
├── Dockerfile          # Docker 이미지 빌드 설정
├── requirements.txt    # Python 의존성 목록
└── README.md           # 백엔드 README
```

## 5. 시작하기

이 프로젝트는 Docker Compose를 사용하여 모든 서비스(백엔드, 프론트엔드, 데이터베이스)를 한 번에 실행하는 것을 권장합니다. 자세한 내용은 프로젝트 루트의 [README.md](../README.md)를 참고하세요.

### 로컬 개발 환경 (Docker 미사용)

1.  **저장소 복제 및 디렉터리 이동**
    ```bash
    git clone <repository-url>
    cd backtest/backend
    ```

2.  **가상환경 생성 및 활성화**
    ```bash
    python -m venv .venv
    # Windows
    .venv\Scripts\activate
    # macOS / Linux
    source .venv/bin/activate
    ```

3.  **의존성 설치**
    ```bash
    pip install -r requirements.txt
    ```

4.  **환경변수 설정**
    `env.example` 파일을 복사하여 `.env` 파일을 생성하고, 로컬 데이터베이스 정보를 입력합니다.
    ```bash
    cp env.example .env
    ```

5.  **데이터베이스 마이그레이션**
    Alembic을 사용하여 데이터베이스 스키마를 최신 상태로 업데이트합니다.
    ```bash
    alembic upgrade head
    ```

6.  **서버 실행**
    ```bash
    uvicorn app.main:app --reload
    ```

## 6. 데이터베이스

### 스키마

데이터베이스 스키마는 `app/db/models.py`에 SQLAlchemy 모델로 정의되어 있습니다.

*   `backtest_results`: 각 백테스트 실행의 요약 결과를 저장합니다.
*   `trades`: `backtest_results`에 대한 개별 거래 내역을 저장합니다.

### 마이그레이션

데이터베이스 스키마 변경은 Alembic을 통해 관리됩니다.

*   **마이그레이션 생성 (모델 변경 후):**
    ```bash
    alembic revision --autogenerate -m "<migration-message>"
    ```
*   **마이그레이션 적용:**
    ```bash
    alembic upgrade head
    ```

## 7. 환경 설정

`.env` 파일을 통해 서버의 주요 설정을 관리합니다. 데이터베이스 연결 정보는 필수적으로 설정해야 합니다.

*   `DB_HOST`: 데이터베이스 호스트
*   `DB_PORT`: 데이터베이스 포트
*   `DB_USER`: 사용자 이름
*   `DB_PASSWORD`: 비밀번호
*   `DB_NAME`: 데이터베이스 이름

## 8. API 주요 엔드포인트

(이하 내용은 기존 README와 동일하게 유지)