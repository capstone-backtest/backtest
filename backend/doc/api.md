# API 명세서

## 1. 기본 정보

- **Base URL**: `/`
- **API Version**: `v1`
- **API Prefix**: `/api/v1`

## 2. 시스템

### GET /api/v1/backtest/health

백엔드 서비스의 상태를 확인합니다.

- **Tags**: `백테스팅`
- **응답 (200 OK)**:
  ```json
  {
    "status": "healthy",
    "message": "백테스트 서비스가 정상 작동 중입니다.",
    "data_source": "Yahoo Finance 연결 정상"
  }
  ```

## 3. 전략 (Strategies)

### GET /api/v1/strategies/

사용 가능한 모든 투자 전략의 목록과 정보를 조회합니다.

- **Tags**: `전략 관리`
- **응답 (200 OK)**: `StrategyListResponse`

### GET /api/v1/strategies/{strategy_name}

지정된 전략의 상세 정보를 반환합니다.

- **Tags**: `전략 관리`
- **Path Parameter**: `strategy_name` (string, required)
- **응답 (200 OK)**: `StrategyInfo`

### GET /api/v1/strategies/{strategy_name}/validate

주어진 파라미터가 전략에 유효한지 검증합니다.

- **Tags**: `전략 관리`
- **Path Parameter**: `strategy_name` (string, required)
- **Query Parameters**: Dynamic, based on the strategy's parameters.
- **응답 (200 OK)**:
  ```json
  {
    "strategy": "sma_crossover",
    "is_valid": true,
    "validated_params": {
      "short_window": 20,
      "long_window": 50
    },
    "message": "파라미터가 유효합니다."
  }
  ```

## 4. 백테스팅 (Backtesting)

### POST /api/v1/backtest/run

지정한 조건으로 백테스트를 실행하고, 상세한 성과 분석 결과를 반환합니다. 결과는 데이터베이스에 저장됩니다.

- **Tags**: `백테스팅`
- **요청 본문**: `BacktestRequest`
- **응답 (200 OK)**: `BacktestResult`

### POST /api/v1/backtest/chart-data

백테스트 결과를 시각화하는 데 필요한 모든 데이터를 생성하여 반환합니다.

- **Tags**: `백테스팅`
- **요청 본문**: `BacktestRequest`
- **응답 (200 OK)**: `ChartDataResponse`

## 5. 최적화 (Optimization)

### POST /api/v1/optimize/run

전략의 파라미터를 최적화하여 최고의 성과를 내는 조합을 찾습니다.

- **Tags**: `최적화`
- **요청 본문**: `OptimizationRequest`
- **응답 (200 OK)**: `OptimizationResult`

### GET /api/v1/optimize/targets

최적화 대상으로 사용할 수 있는 성능 지표들의 목록을 반환합니다.

- **Tags**: `최적화`
- **응답 (200 OK)**:
  ```json
  {
    "targets": {
      "SQN": { "name": "System Quality Number", ... },
      "Return [%]": { "name": "Total Return", ... }
    },
    "default": "SQN",
    "recommended": ["SQN", "Sharpe Ratio", "Calmar Ratio"]
  }
  ```

### GET /api/v1/optimize/methods

파라미터 최적화에 사용할 수 있는 방법들의 목록을 반환합니다.

- **Tags**: `최적화`
- **응답 (200 OK)**:
  ```json
  {
    "methods": {
      "grid": { "name": "Grid Search", ... },
      "sambo": { "name": "SAMBO Optimization", ... }
    },
    "default": "grid",
    "recommended": "sambo"
  }
  ```