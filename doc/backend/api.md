````markdown
# API 명세서

## 문서 위치 안내

- 중앙 문서 인덱스: `../README.md`
- 백엔드 상세 문서: 이 파일(`api.md`)

## 1. 기본 정보

- **Base URL**: `/`
- **API Version**: `v1`
- **API Prefix**: `/api/v1`

## 2. 시스템

### GET /health

시스템의 상태를 확인합니다.

- **Tags**: `시스템`
- **응답 (200 OK)**: `HealthResponse`
  ```json
  {
    "status": "healthy",
    "timestamp": "2024-01-15T10:30:00",
    "version": "1.0.0"
  }
  ```

## 3. 전략 (Strategies)

### GET /api/v1/strategies/

사용 가능한 모든 투자 전략의 목록과 정보를 조회합니다.

- **Tags**: `전략`
- **응답 (200 OK)**: `StrategyListResponse`
  ```json
  {
    "strategies": [
      {
        "name": "sma_crossover",
        "description": "단순 이동평균 교차 전략",
        "parameters": {
          "short_window": { "type": "int", "default": 10, "min": 5, "max": 50 },
          "long_window": { "type": "int", "default": 20, "min": 10, "max": 200 }
        }
      }
    ],
    "total_count": 1
  }
  ```

## 4. 백테스팅 (Backtesting)

### POST /api/v1/backtest/run

지정한 조건으로 백테스트를 실행하고, 상세한 성과 분석 결과를 반환합니다.

- **Tags**: `백테스트`
- **요청 본문**: `BacktestRequest` (예시)
  ```json
  {
    "ticker": "AAPL",
    "start_date": "2022-01-01",
    "end_date": "2023-12-31",
    "initial_cash": 10000,
    "strategy": "sma_crossover",
    "strategy_params": { "short_window": 20, "long_window": 50 },
    "commission": 0.001
  }
  ```
- **응답 (200 OK)**: `BacktestResult` (요약 예시)
  ```json
  { "ticker": "AAPL", "final_equity": 12500.50, "total_return_pct": 25.0 }
  ```

### POST /api/v1/backtest/chart-data

백테스트 결과를 시각화하는 데 필요한 OHLC, equity, trade markers, indicators, summary stats 등을 반환합니다.

- **Tags**: `백테스트`
- **요청 본문**: `BacktestRequest` (위와 동일)
- **응답 (200 OK)**: `ChartDataResponse` (예시 생략)

## 5. YFinance 데이터 조회 및 캐시


### POST /api/v1/yfinance/fetch-and-cache

설명: yfinance에서 지정한 `ticker`와 `start`/`end` 기간을 받아 원본 시계열(OHLCV)을 데이터베이스에 저장(업서트)합니다. 이 API는 DB 캐시를 직접 채우기 위한 용도입니다.

- **Tags**: `yfinance 캐시`
- **요청 방식**: POST (쿼리/JSON 파라미터 가능)
- **주요 파라미터**:
  - `ticker` (string, required)
  - `start` (date, required, YYYY-MM-DD)
  - `end` (date, required, YYYY-MM-DD)
  - `interval` (string, optional, default `1d`)

- **동작**: 서버가 yfinance로부터 데이터를 받아 `stocks` 및 `daily_prices` 테이블에 upsert 합니다. 이미 존재하는 데이터는 갱신됩니다. 빈 결과나 비정상 응답은 4xx/5xx로 매핑됩니다.

- **응답 (200 OK)**:
  ```json
  { "ticker": "AAPL", "rows_saved": 252, "start": "2023-01-01", "end": "2023-12-31" }
  ```

- **오류 예시**:
  - 400: 잘못된 파라미터
  - 404: 티커/데이터 없음
  - 500: yfinance 호출 실패 또는 DB 저장 실패

- **운영 주의사항**:
  - 대량/장기간 분봉 요청은 응답 크기/시간이 크므로 주의하세요.
  - DB 연결 문자열은 `DATABASE_URL` 환경변수로 설정됩니다.
  - 동시성으로 인한 중복 fetch는 현재 보호되지 않으므로 필요시 분산 락(예: Redis)을 도입하세요.

## 6. 최적화 (Optimization)

### POST /api/v1/optimize/run

전략 파라미터를 그리드/랜덤 방식 등으로 탐색하여 최적 조합을 찾습니다. 요청/응답 구조 및 예시는 하단의 예시 블록을 참고하세요.
