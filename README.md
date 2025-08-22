# 포트폴리오 백테스팅 플랫폼

## 1. 개요

이 프로젝트는 포트폴리오 기반 투자 전략을 분석하고 최적화하기 위해 설계된 종합적인 백테스팅 플랫폼입니다. FastAPI 기반의 백엔드 API 서버와 React 기반의 프론트엔드 대시보드로 구성되어 있습니다. 사용자는 하나 이상의 종목으로 구성된 포트폴리오에 다양한 투자 전략을 적용하고, 과거 데이터를 기반으로 백테스트를 실행하며, 수익률, 변동성, 최대 낙폭과 같은 성과 지표를 시각화할 수 있습니다.

## 2. 주요 기능

### 포트폴리오 백테스팅
*   **단일 종목 및 다중 종목 지원**: 1개부터 최대 10개 종목으로 구성된 포트폴리오 백테스트
*   **투자 금액 기반 포트폴리오**: 각 종목별로 투자할 금액을 직접 설정
*   **자동 비중 계산**: 입력한 투자 금액을 기반으로 자동으로 포트폴리오 비중 계산

### 투자 전략
*   **Buy & Hold**: 전통적인 매수 후 보유 전략
*   **단순이동평균 교차(SMA Crossover)**: 단기/장기 이동평균을 이용한 매매 신호
*   **RSI 전략**: 상대강도지수를 이용한 과매수/과매도 매매 전략
*   **전략 파라미터 최적화**: 각 전략별로 최적의 파라미터 조합 탐색

### 상세한 성과 분석
*   **개별 종목 분석**: 포트폴리오 내 각 종목의 개별 성과 분석
*   **포트폴리오 통합 성과**: 가중치를 반영한 전체 포트폴리오 성과
*   **20가지 이상의 성과 지표**: 총수익률, 연평균 성장률, 샤프 지수, 최대 낙폭 등
*   **실시간 차트 시각화**: 포트폴리오 가치 변화 및 일별 수익률 차트

### 기타 기능
*   **DB 기반 데이터 관리**: 주식 데이터 캐싱 및 빠른 백테스트 실행
*   **RESTful API**: 표준 REST API를 통한 백테스트 실행 및 결과 조회
*   **반응형 웹 UI**: 모바일 및 데스크톱 환경에서 모두 사용 가능
*   **실시간 에러 핸들링**: 상세한 오류 메시지 및 유효성 검증

## 3. 기술 스택

*   **백엔드**: FastAPI, Python 3.11+, pandas, yfinance, backtesting.py
*   **데이터베이스**: SQLite (개발), PostgreSQL (프로덕션 권장)
*   **프론트엔드**: React 18, TypeScript, Vite, React Bootstrap, Recharts
*   **컨테이너화**: Docker, Docker Compose
*   **개발 도구**: Hot Reload, ESLint, TypeScript 컴파일러

## 4. 프로젝트 구조

```
backtest/
├── backend/                    # FastAPI 백엔드 API 서버
│   ├── app/
│   │   ├── api/v1/            # 통합된 v1 API 엔드포인트
│   │   ├── models/            # Pydantic 모델 및 스키마
│   │   ├── services/          # 비즈니스 로직 서비스
│   │   └── utils/             # 유틸리티 함수
│   ├── strategies/            # 투자 전략 구현
│   ├── data_cache/            # 주식 데이터 캐시
│   └── requirements.txt       # Python 의존성
├── frontend/                   # React 프론트엔드
│   ├── src/
│   │   ├── components/        # 통합된 React 컴포넌트
│   │   ├── types/             # TypeScript 타입 정의
│   │   └── App.tsx            # 메인 애플리케이션
│   ├── package.json           # Node.js 의존성
│   └── vite.config.ts         # Vite 설정
├── docker-compose.dev.yml     # 개발환경 Docker Compose
├── docker-compose.prod.yml    # 프로덕션 Docker Compose
└── README.md                  # 프로젝트 문서
```

## 5. 시작하기

### 사전 요구사항

*   Docker Desktop 또는 Docker Engine
*   Docker Compose v2.0+

### 개발환경 실행

1. **리포지토리 클론**:
    ```bash
    git clone <repository-url>
    cd backtest
    ```

2. **개발환경 컨테이너 실행**:
    ```bash
    docker-compose -f docker-compose.dev.yml up -d
    ```

3. **서비스 접속**:
    - **프론트엔드**: http://localhost:5173
    - **백엔드 API**: http://localhost:8000
    - **API 문서**: http://localhost:8000/docs

### 프로덕션 실행

1. **프로덕션 환경 실행**:
    ```bash
    docker-compose -f docker-compose.prod.yml up -d
    ```

2. **서비스 접속**:
    - **애플리케이션**: http://localhost (포트 80)
    - **API**: http://localhost:8000

### 사용법

1. **포트폴리오 구성**:
   - 1-10개 종목의 심볼 입력 (예: AAPL, GOOGL, TSLA)
   - 각 종목에 투자할 금액 설정 (예: AAPL $30,000, GOOGL $40,000, TSLA $30,000)
   - 시스템이 자동으로 비중 계산 (예: 30%, 40%, 30%)

2. **백테스트 설정**:
   - 시작/종료 날짜 선택
   - 투자 전략 선택 및 파라미터 조정

3. **결과 분석**:
   - 포트폴리오 전체 성과 지표 확인
   - 개별 종목별 성과 비교
   - 시각화된 차트로 수익률 추이 분석

## 6. API 엔드포인트

### 포트폴리오 백테스트

```http
POST /api/v1/backtest/portfolio
Content-Type: application/json

{
  "portfolio": [
    {"symbol": "AAPL", "amount": 30000},
    {"symbol": "GOOGL", "amount": 40000},
    {"symbol": "TSLA", "amount": 30000}
  ],
  "start_date": "2023-01-01",
  "end_date": "2024-12-31",
  "strategy": "sma_crossover",
  "strategy_params": {
    "short_window": 10,
    "long_window": 20
  }
}
```

### 개별 종목 백테스트

```http
POST /api/v1/backtest/run
Content-Type: application/json

{
  "ticker": "AAPL",
  "start_date": "2023-01-01",
  "end_date": "2024-12-31",
  "initial_cash": 100000,
  "strategy": "sma_crossover",
  "strategy_params": {
    "short_window": 10,
    "long_window": 20
  }
}
```

## 7. 지원하는 투자 전략

| 전략명 | 설명 | 파라미터 |
|--------|------|----------|
| `buy_and_hold` | 매수 후 보유 | 없음 |
| `sma_crossover` | 단순이동평균 교차 | `short_window`, `long_window` |
| `rsi_strategy` | RSI 기반 매매 | `rsi_period`, `rsi_oversold`, `rsi_overbought` |

### 전략 파라미터 예시

**SMA Crossover:**
- `short_window`: 단기 이동평균 기간 (기본값: 10, 범위: 5-50)
- `long_window`: 장기 이동평균 기간 (기본값: 20, 범위: 10-100)

**RSI Strategy:**
- `rsi_period`: RSI 계산 기간 (기본값: 14, 범위: 5-30)
- `rsi_oversold`: 과매도 기준선 (기본값: 30, 범위: 10-40)
- `rsi_overbought`: 과매수 기준선 (기본값: 70, 범위: 60-90)

## 8. 개발 가이드

### 프론트엔드 개발

```bash
cd frontend
npm install
npm run dev
```

### 백엔드 개발

```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 새로운 전략 추가

1. `backend/strategies/` 디렉터리에 새 전략 파일 생성
2. `Strategy` 클래스를 상속받아 구현
3. 백엔드 재시작 후 API를 통해 사용 가능

## 9. 문제 해결

### 일반적인 문제

**포트 충돌:**
- 다른 서비스가 5173 또는 8000 포트를 사용 중인지 확인
- `docker-compose.dev.yml`에서 포트 번호 변경 가능

**데이터 로딩 실패:**
- 종목 심볼이 올바른지 확인 (대문자 사용)
- 날짜 범위가 너무 오래되지 않았는지 확인
- 네트워크 연결 상태 확인

**백테스트 실행 오류:**
- 전략 파라미터가 유효 범위 내에 있는지 확인
- 투자 금액이 모두 양수인지 확인
- 총 투자 금액이 0보다 큰지 확인

### 로그 확인

```bash
# 모든 컨테이너 로그 확인
docker-compose -f docker-compose.dev.yml logs

# 특정 서비스 로그 확인
docker-compose -f docker-compose.dev.yml logs backend
docker-compose -f docker-compose.dev.yml logs frontend
```

## 10. 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.
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