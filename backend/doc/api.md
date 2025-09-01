# 백엔드 API 문서

## 개요

백테스팅 플랫폼의 REST API 엔드포인트를 설명합니다. 모든 API는 JSON 형식으로 데이터를 주고받습니다.

## 최근 업데이트 (2025-09-01)

### 시스템 안정성 개선
- **오프라인 모킹**: yfinance API 의존성 제거로 테스트 환경 안정성 확보
- **데이터 생성기**: 수학적 알고리즘 기반 현실적 주식 데이터 생성
- **예외 처리**: DataNotFoundError, InvalidSymbolError 일관된 응답
- **CI/CD 호환**: 젠킨스 환경에서 네트워크 의존성 없는 테스트

### 📊 데이터 품질 향상
- **OHLCV 정확성**: DB 스키마(DECIMAL 19,4)에 맞는 정밀도
- **시장 시뮬레이션**: 기하 브라운 운동으로 실제 주가 패턴 모사
- **다양한 시나리오**: bull_market, bear_market, volatile 등 테스트 케이스

## 기본 정보

- **Base URL**: `http://localhost:8001` (개발), `https://backtest-be.yeonjae.kr` (프로덕션)
- **API 버전**: v1
- **Content-Type**: `application/json`

## 인증

현재 API는 인증이 필요하지 않습니다. (향후 JWT 인증 계획)

## 주요 엔드포인트

### 1. 백테스트 API

#### 차트 데이터 조회

```http
POST /api/v1/backtest/chart-data
```

**요청 본문:**
```json
{
  "ticker": "AAPL",
  "start_date": "2023-01-01",
  "end_date": "2024-12-31",
  "initial_cash": 10000,
  "strategy": "sma_crossover",
  "strategy_params": {
    "short_window": 10,
    "long_window": 20
  }
}
```

**응답:**
```json
{
  "ticker": "AAPL",
  "strategy": "sma_crossover",
  "start_date": "2023-01-01",
  "end_date": "2024-12-31",
  "ohlc_data": [
    {
      "timestamp": "2023-01-03T00:00:00",
      "date": "2023-01-03",
      "open": 130.28,
      "high": 130.90,
      "low": 124.17,
      "close": 125.07,
      "volume": 112117471
    }
  ],
  "equity_data": [
    {
      "timestamp": "2023-01-03T00:00:00",
      "date": "2023-01-03",
      "equity": 10000.0,
      "return_pct": 0.0,
      "drawdown_pct": 0.0
    }
  ],
  "trade_markers": [
    {
      "timestamp": "2023-01-03T00:00:00",
      "date": "2023-01-03",
      "price": 125.07,
      "type": "entry",
      "side": "buy",
      "size": 79.96
    }
  ],
  "indicators": [
    {
      "name": "SMA_10",
      "type": "overlay",
      "color": "#ff7300",
      "data": [
        {
          "timestamp": "2023-01-13T00:00:00",
          "date": "2023-01-13",
          "value": 127.45
        }
      ]
    }
  ],
  "summary_stats": {
    "total_return_pct": 23.46,
    "total_trades": 15,
    "win_rate_pct": 66.67,
    "max_drawdown_pct": -8.23,
    "sharpe_ratio": 1.45,
    "profit_factor": 1.85
  }
}
```

#### 포트폴리오 백테스트 실행

```http
POST /api/v1/backtest/portfolio
```

**요청 본문:**
```json
{
  "portfolio": [
    {
      "symbol": "AAPL",
      "amount": 4000,
      "investment_type": "lump_sum"
    },
    {
      "symbol": "GOOGL",
      "amount": 3000,
      "investment_type": "dca",
      "dca_periods": 12
    },
    {
      "symbol": "MSFT",
      "amount": 3000,
      "investment_type": "lump_sum"
    }
  ],
  "start_date": "2023-01-01",
  "end_date": "2024-12-31",
  "strategy": "buy_and_hold",
  "commission": 0.002,
  "rebalance_frequency": "monthly"
}
```

**응답:**
```json
{
  "status": "success",
  "data": {
    "portfolio_statistics": {
      "Start": "2023-01-01",
      "End": "2024-12-31",
      "Duration": "730 days",
      "Initial_Value": 10000.0,
      "Final_Value": 12345.67,
      "Peak_Value": 13000.0,
      "Total_Return": 23.46,
      "Annual_Return": 11.23,
      "Annual_Volatility": 18.45,
      "Sharpe_Ratio": 0.61,
      "Max_Drawdown": -8.23,
      "Total_Trading_Days": 504,
      "Positive_Days": 280,
      "Negative_Days": 224,
      "Win_Rate": 55.56
    },
    "individual_returns": {
      "AAPL": {
        "weight": 0.4,
        "return": 28.5,
        "start_price": 125.07,
        "end_price": 160.77,
        "investment_type": "lump_sum"
      },
      "GOOGL": {
        "weight": 0.3,
        "return": 18.2,
        "start_price": 89.12,
        "end_price": 105.35,
        "investment_type": "dca",
        "dca_periods": 12
      },
      "MSFT": {
        "weight": 0.3,
        "return": 22.8,
        "start_price": 239.82,
        "end_price": 294.51,
        "investment_type": "lump_sum"
      }
    },
    "portfolio_composition": [
      {
        "symbol": "AAPL",
        "weight": 0.4,
        "amount": 4000,
        "investment_type": "lump_sum"
      },
      {
        "symbol": "GOOGL",
        "weight": 0.3,
        "amount": 3000,
        "investment_type": "dca"
      },
      {
        "symbol": "MSFT",
        "weight": 0.3,
        "amount": 3000,
        "investment_type": "lump_sum"
      }
    ],
    "equity_curve": {
      "2023-01-03": 10000.0,
      "2023-01-04": 10123.45,
      "...": "..."
    },
    "daily_returns": {
      "2023-01-03": 0.0,
      "2023-01-04": 1.23,
      "...": "..."
    }
  }
}
```

### 2. 시스템 API

#### 시스템 정보 조회

```http
GET /api/v1/system/info
```

**응답:**
```json
{
  "backend": {
    "version": "1.0.0",
    "uptime": "2024-01-15T10:30:00Z",
    "status": "healthy",
    "git_commit": "fd358f3e",
    "git_branch": "main",
    "build_number": "123",
    "environment": "development"
  },
  "frontend": {
    "version": "1.0.0", 
    "git_commit": "fd358f3e",
    "build_number": "123"
  },
  "docker": {
    "backend_image": "ghcr.io/redyeji/backtest-backend:latest",
    "frontend_image": "ghcr.io/redyeji/backtest-frontend:latest"
  }
}
```

### 3. 네이버 뉴스 API

#### 뉴스 검색

```http
GET /api/v1/naver-news/search?query={검색어}&display={결과수}
```

**파라미터:**
- `query` (string, 필수): 검색할 키워드
- `display` (int, 선택): 검색 결과 수 (1-100, 기본값: 10)

**응답:**
```json
{
  "status": "success",
  "message": "검색어 관련 뉴스 10건을 조회했습니다.",
  "data": {
    "query": "검색어",
    "total_count": 10,
    "news_list": [
      {
        "title": "뉴스 제목",
        "link": "https://n.news.naver.com/mnews/article/...",
        "description": "뉴스 내용 요약...",
        "pubDate": "Mon, 01 Sep 2025 21:01:00 +0900"
      }
    ]
  }
}
```

#### 종목별 뉴스 검색

```http
GET /api/v1/naver-news/ticker/{ticker}?display={결과수}
```

**파라미터:**
- `ticker` (string, 필수): 종목 코드 (예: 005930.KS, AAPL)
- `display` (int, 선택): 검색 결과 수 (1-100, 기본값: 10)

**응답:**
```json
{
  "status": "success",
  "message": "005930.KS(삼성전자 주가) 관련 뉴스 10건을 조회했습니다.",
  "data": {
    "ticker": "005930.KS",
    "query": "삼성전자 주가",
    "total_count": 10,
    "news_list": [
      {
        "title": "삼성전자 관련 뉴스 제목",
        "link": "https://n.news.naver.com/mnews/article/...",
        "description": "삼성전자 관련 뉴스 내용...",
        "pubDate": "Mon, 01 Sep 2025 21:01:00 +0900"
      }
    ]
  }
}
```

#### 날짜별 뉴스 검색

```http
GET /api/v1/naver-news/search-by-date?query={검색어}&start_date={시작일}&end_date={종료일}&display={결과수}
```

**파라미터:**
- `query` (string, 필수): 검색할 키워드
- `start_date` (string, 필수): 검색 시작일 (YYYY-MM-DD 형식)
- `end_date` (string, 선택): 검색 종료일 (YYYY-MM-DD 형식, 없으면 시작일과 동일)
- `display` (int, 선택): 검색 결과 수 (10-100, 기본값: 50)

**응답:**
```json
{
  "status": "success",
  "message": "검색어 관련 뉴스를 2025-09-01~2025-09-01 기간에서 48건 조회했습니다.",
  "data": {
    "query": "검색어",
    "start_date": "2025-09-01",
    "end_date": "2025-09-01",
    "total_count": 48,
    "news_list": [
      {
        "title": "특정 날짜 뉴스 제목",
        "link": "https://n.news.naver.com/mnews/article/...",
        "description": "특정 날짜 뉴스 내용...",
        "pubDate": "Mon, 01 Sep 2025 21:01:00 +0900"
      }
    ]
  }
}
```

#### 종목별 날짜 뉴스 검색

```http
GET /api/v1/naver-news/ticker/{ticker}/date?start_date={시작일}&end_date={종료일}&display={결과수}
```

**파라미터:**
- `ticker` (string, 필수): 종목 코드 (예: 005930.KS, AAPL)
- `start_date` (string, 필수): 검색 시작일 (YYYY-MM-DD 형식)
- `end_date` (string, 선택): 검색 종료일 (YYYY-MM-DD 형식, 없으면 시작일과 동일)
- `display` (int, 선택): 검색 결과 수 (10-100, 기본값: 50)

**응답:**
```json
{
  "status": "success",
  "message": "005930.KS(삼성전자 주가) 관련 뉴스를 2025-09-01~2025-09-01 기간에서 48건 조회했습니다.",
  "data": {
    "ticker": "005930.KS",
    "query": "삼성전자 주가",
    "start_date": "2025-09-01",
    "end_date": "2025-09-01",
    "total_count": 48,
    "news_list": [
      {
        "title": "삼성전자 특정 날짜 뉴스 제목",
        "link": "https://n.news.naver.com/mnews/article/...",
        "description": "삼성전자 특정 날짜 뉴스 내용...",
        "pubDate": "Mon, 01 Sep 2025 21:01:00 +0900"
      }
    ]
  }
}
```

#### 네이버 뉴스 API 테스트

```http
GET /api/v1/naver-news/test
```

API 연결 상태를 확인하는 테스트 엔드포인트입니다.

**응답:**
```json
{
  "status": "success",
  "message": "네이버 뉴스 API가 정상적으로 작동하고 있습니다.",
  "data": {
    "test_query": "테스트",
    "total_count": 5,
    "news_list": [
      {
        "title": "테스트 뉴스 제목",
        "link": "https://example.com",
        "description": "테스트 뉴스 내용",
        "pubDate": "Mon, 01 Sep 2025 21:01:00 +0900"
      }
    ]
  }
}
```

### 4. 최적화 API (v2, 미구현)

#### 전략 파라미터 최적화 실행

*주의: 이 기능은 아직 구현되지 않았습니다. v2 API에서 지원 예정입니다.*

#### 전략 파라미터 최적화 실행

```http
POST /api/v1/optimize/run
```

**요청 본문:**
```json
{
  "ticker": "AAPL",
  "start_date": "2023-01-01",
  "end_date": "2024-12-31",
  "initial_cash": 10000,
  "strategy": "sma_crossover",
  "parameter_ranges": {
    "short_window": [5, 20],
    "long_window": [20, 50]
  },
  "optimization_target": "total_return_pct",
  "method": "grid"
}
```

## 지원 전략 및 파라미터

### 1. Buy & Hold (`buy_and_hold`)
- **파라미터**: 없음
- **설명**: 매수 후 보유 전략

### 2. SMA Crossover (`sma_crossover`)
- **파라미터**:
  - `short_window` (int): 단기 이동평균 기간 (5-50, 기본값: 10)
  - `long_window` (int): 장기 이동평균 기간 (10-200, 기본값: 20)
- **설명**: 단순이동평균 교차 전략

### 3. RSI Strategy (`rsi_strategy`)
- **파라미터**:
  - `rsi_period` (int): RSI 계산 기간 (5-30, 기본값: 14)
  - `rsi_overbought` (int): 과매수 기준 (60-90, 기본값: 70)
  - `rsi_oversold` (int): 과매도 기준 (10-40, 기본값: 30)
- **설명**: RSI 과매수/과매도 기반 전략

### 4. Bollinger Bands (`bollinger_bands`)
- **파라미터**:
  - `period` (int): 이동평균 기간 (10-50, 기본값: 20)
  - `std_dev` (float): 표준편차 배수 (1.0-3.0, 기본값: 2.0)
- **설명**: 볼린저 밴드 기반 전략

### 5. MACD Strategy (`macd_strategy`)
- **파라미터**:
  - `fast_period` (int): 빠른 EMA 기간 (5-20, 기본값: 12)
  - `slow_period` (int): 느린 EMA 기간 (20-50, 기본값: 26)
  - `signal_period` (int): 시그널 라인 기간 (5-15, 기본값: 9)
- **설명**: MACD 교차 기반 전략

## 네이버 뉴스 API 지원 종목

### 한국 주요 종목 (총 40+개)
- **삼성 계열**: 005930.KS (삼성전자), 006400.KS (삼성SDI), 207940.KS (삼성바이오로직스), 028260.KS (삼성물산), 009150.KS (삼성전기), 018260.KS (삼성에스디에스), 032830.KS (삼성생명)
- **SK 계열**: 000660.KS (SK하이닉스), 096770.KS (SK이노베이션), 017670.KS (SK텔레콤), 034730.KS (SK)
- **LG 계열**: 051910.KS (LG화학), 373220.KS (LG에너지솔루션), 066570.KS (LG전자), 003550.KS (LG)
- **금융**: 055550.KS (신한지주), 105560.KS (KB금융), 086790.KS (하나금융지주), 316140.KS (우리금융지주)
- **자동차**: 005380.KS (현대차), 000270.KS (기아), 012330.KS (현대모비스)
- **IT/게임**: 035420.KS (NAVER), 035720.KS (카카오), 323410.KS (카카오뱅크), 036570.KS (엔씨소프트), 251270.KS (넷마블)
- **기타**: 030200.KS (KT), 015760.KS (한국전력), 068270.KS (셀트리온), 003670.KS (포스코퓨처엠), 009540.KS (HD한국조선해양), 033780.KS (KT&G), 090430.KS (아모레퍼시픽), 180640.KS (한진칼), 128940.KS (한미약품), 047050.KS (포스코인터내셔널), 010950.KS (S-Oil)

### 미국 주요 종목 (총 30+개)
- **빅테크**: AAPL (애플), MSFT (마이크로소프트), GOOGL (구글), AMZN (아마존), META (메타), NFLX (넷플릭스)
- **반도체**: NVDA (엔비디아), AMD (AMD), INTC (인텔)
- **테슬라/EV**: TSLA (테슬라), RIVN (리비안), LCID (루시드)
- **소프트웨어**: CRM (세일즈포스), ORCL (오라클), ADBE (어도비), OKTA (옥타), DDOG (데이터독), SNOW (스노우플레이크), PLTR (팔란티어)
- **핀테크**: PYPL (페이팔), SQ (스퀘어), COIN (코인베이스)
- **소셜/엔터**: SNAP (스냅챗), SPOT (스포티파이), PINS (핀터레스트), RBLX (로블록스)
- **기타**: UBER (우버), ZOOM (줌), SHOP (쇼피파이), ROKU (로쿠), DOCU (도큐사인), U (유니티)

### 네이버 뉴스 API 특징
- **회사명 매핑**: 종목 코드를 한국어 회사명으로 자동 변환하여 정확한 뉴스 검색
- **콘텐츠 필터링**: `[역사속 오늘]`, 부고, 날씨, 운세 등 불필요한 콘텐츠 자동 제거
- **네트워크 안정성**: 최대 3회 재시도 + 지수 백오프로 간헐적 연결 오류 해결
- **날짜 정확성**: RFC 2822 형식 날짜 파싱으로 정확한 날짜별 필터링
- **HTML 정리**: 뉴스 제목과 내용에서 HTML 태그 자동 제거

## 투자 방식 (Portfolio API)

### 1. 일시 투자 (`lump_sum`)
- 백테스트 시작일에 전체 금액을 한 번에 투자

### 2. 분할 매수 (`dca`)
- 설정된 기간에 걸쳐 일정 금액씩 분할 투자
- `dca_periods`: 분할 기간 (1-60개월, 기본값: 12)

## 에러 코드

| HTTP 상태 코드 | 설명 | 예시 |
|---------------|------|------|
| 200 | 성공 | 정상적으로 처리됨 |
| 400 | 잘못된 요청 | 파라미터 오류, 유효성 검증 실패 |
| 404 | 리소스 없음 | 존재하지 않는 티커 심볼 |
| 422 | 처리할 수 없는 엔티티 | 날짜 범위 오류, 데이터 부족 |
| 500 | 서버 내부 오류 | 백테스팅 엔진 오류, 데이터 처리 실패 |
| 503 | 서비스 사용 불가 | yfinance API 제한, 네트워크 오류 |

## 에러 응답 형식

```json
{
  "detail": "Ticker 'INVALID' not found or has insufficient data",
  "status_code": 404
}
```

## 주의사항

1. **데이터 제공**: yfinance를 통해 주식 데이터를 가져오므로 실시간이 아닌 지연된 데이터입니다.
2. **캐싱**: 주식 데이터는 MySQL 데이터베이스에 캐시되어 반복 요청 시 빠른 응답을 제공합니다.
3. **제한사항**: 너무 짧은 백테스트 기간이나 데이터가 없는 기간에서는 오류가 발생할 수 있습니다.
4. **성능**: 대량의 데이터나 복잡한 전략의 경우 응답 시간이 길어질 수 있습니다.
5. **포트폴리오**: 최대 10개 종목까지 포트폴리오 구성이 가능합니다.

## 업데이트 내역

- **v1.0**: 기본 백테스트 및 차트 데이터 API
- **v1.1**: 포트폴리오 백테스트 기능 추가
- **v1.2**: 최적화 API 추가 (베타)
- **v1.3**: 분할 매수(DCA) 기능 추가
- **v1.4**: 네이버 뉴스 검색 API 추가 (2025-09-01)
  - 종목별 뉴스 검색 (70+개 종목 지원)
  - 날짜별 뉴스 필터링
  - 네트워크 재시도 로직 및 안정성 개선
  - 불필요한 콘텐츠 자동 필터링
