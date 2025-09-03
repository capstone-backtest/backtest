# 백엔드 개발 가이드

## 최근 업데이트 (2025-09-03)

### 🚀 Pydantic V2 완전 마이그레이션
- **@validator → @field_validator**: 모든 검증 로직 최신 문법 적용
- **schema_extra → json_schema_extra**: 스키마 문서 생성 현대화
- **lifespan 패턴**: FastAPI on_event 대신 최신 lifespan 관리 적용
- **Query 매개변수**: regex → pattern 매개변수 업데이트
- **완전 호환성**: 모든 Pydantic deprecation 경고 제거

### 🧪 테스트 시스템 완전 재설계
- **완전 오프라인 모킹**: yfinance API와 MySQL 의존성 완전 제거
- **수학적 데이터 생성**: 기하 브라운 운동 기반 현실적 주식 데이터 시뮬레이션
- **CI/CD 안정성**: 젠킨스 우분투 환경에서 네트워크 의존성 없는 테스트 실행
- **DB 스키마 준수**: 실제 stock_data_cache 테이블 구조와 일치하는 모의 데이터

### � 최신 구현 완료 기능
- **진짜 현금 자산 처리**: CASH 티커 문제 해결, 실제 무위험 자산으로 구현
- **자산 타입 구분**: asset_type 필드로 현금과 주식 명확히 분리
- **포트폴리오 안정성**: 현금 혼합 포트폴리오에서 리스크 완화 기능 구현

---

## 개요

FastAPI 기반의 백테스팅 API 서버입니다. 포트폴리오 백테스트, 투자 전략 실행, 주식 데이터 관리 기능을 제공합니다.

## 기술 스택

- **프레임워크**: FastAPI 0.104+
- **언어**: Python 3.11+
- **데이터 처리**: pandas, numpy
- **주식 데이터**: yfinance
- **백테스팅**: backtesting.py
- **데이터베이스**: MySQL

## 프로젝트 구조

```
backend/
├── app/
│   ├── api/             # API 라우터 계층
│   │   ├── v1/          # v1 API 엔드포인트
│   │   │   └── endpoints/  # 개별 엔드포인트 (backtest, stock, naver_news)
│   │   ├── v2/          # v2 API (확장 예정)
│   │   └── api.py       # 기본 API 라우터
│   ├── core/            # 핵심 설정 및 예외 처리
│   ├── models/          # Pydantic 모델 (요청/응답 스키마)
│   ├── services/        # 비즈니스 로직 (백테스트, 포트폴리오, 전략)
│   ├── utils/           # 유틸리티 (데이터 수집, 직렬화, 포트폴리오)
│   └── main.py          # FastAPI 애플리케이션 엔트리포인트
├── strategies/          # 투자 전략 구현체 (RSI, SMA 등)
├── tests/              # 백엔드 테스트 코드 (재설계 중)
│   ├── conftest.py     # pytest 설정 및 전역 픽스처
│   ├── unit/           # 단위 테스트 (계층별 분리)
│   ├── integration/    # 통합 테스트 (컴포넌트 간)
│   ├── e2e/           # End-to-End 테스트 (전체 시나리오)
│   └── fixtures/      # 모의 데이터 및 테스트 픽스처
│       ├── mock_data_generator.py  # 수학적 주식 데이터 생성기
│       ├── stock_metadata.json    # 테스트용 주식 메타데이터
│       └── expected_results.json  # 예상 백테스팅 결과
├── doc/                # 백엔드 개발 문서
├── Dockerfile          # 백엔드 도커 이미지 설정
└── requirements.txt    # Python 의존성 패키지
```

## 개발 환경 설정

### Docker 사용 (권장)

```bash
# 프로젝트 루트에서
docker compose -f docker-compose.yml -f docker-compose.dev.yml up backend --build
```

### 로컬 개발

```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

**주의**: Docker 환경에서는 호스트 포트 8001로 매핑됩니다.

## 주요 기능

### 1. 포트폴리오 백테스트

투자 금액 기반 포트폴리오 구성 및 전략 적용:

```python
# 포트폴리오 백테스트 요청
{
  "portfolio": [
    {"symbol": "AAPL", "amount": 10000},
    {"symbol": "GOOGL", "amount": 15000}
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

### 2. 투자 전략

지원하는 전략들:

- **Buy & Hold**: 매수 후 보유 전략
- **SMA Crossover**: 단순이동평균 교차 전략
- **RSI Strategy**: 상대강도지수 기반 전략

### 3. 데이터 관리

- yfinance를 통한 주식 데이터 수집
- 로컬 캐시를 통한 빠른 데이터 접근
- 자동 데이터 업데이트

## API 엔드포인트

자세한 API 문서는 [api.md](api.md)를 참조하세요.

### 주요 엔드포인트

- `POST /api/v1/backtest/portfolio` - 포트폴리오 백테스트
- `POST /api/v1/backtest/chart-data` - 차트 데이터 조회

## 새로운 전략 추가

1. `strategies/` 디렉터리에 새 전략 파일 생성
2. `backtesting.Strategy` 클래스 상속
3. `app/services/strategy_service.py`에 전략 등록

예시:

```python
# strategies/my_strategy.py 또는 app/services/strategy_service.py 내부에 직접 구현
from backtesting import Strategy

class MyStrategy(Strategy):
    # 전략 파라미터 (클래스 변수)
    param1 = 10
    param2 = 20
    
    def init(self):
        # 초기화 로직 (지표 계산 등)
        close = self.data.Close
        self.indicator = self.I(SomeIndicator, close, self.param1)
    
    def next(self):
        # 매 봉마다 실행되는 매매 로직
        if some_condition:
            self.buy()
        elif other_condition:
            self.sell()

# app/services/strategy_service.py의 _strategies 딕셔너리에 등록
_strategies = {
    # 기존 전략들...
    'my_strategy': {
        'class': MyStrategy,
        'parameters': {
            'param1': {'type': 'int', 'default': 10, 'min': 5, 'max': 50},
            'param2': {'type': 'int', 'default': 20, 'min': 10, 'max': 100}
        }
    }
}
```

## 문제 해결

### 자주 발생하는 문제

1. **종목 데이터 로딩 실패**
   - 종목 심볼 확인 (대문자 사용)
   - 네트워크 연결 상태 확인
   - yfinance 서비스 상태 확인

2. **백테스트 실행 오류**
   - 전략 파라미터 범위 확인
   - 날짜 범위 유효성 확인
   - 충분한 데이터 기간 확인

3. **성능 이슈**
   - 데이터 캐시 상태 확인
   - 메모리 사용량 모니터링
   - DB 연결 상태 확인

### 로그 확인

```bash
# Docker 환경
docker logs backtest-backend-1

# 로컬 개발 환경
# 콘솔에서 직접 확인 가능
```

## 개발 참고사항

### 코드 스타일

- PEP 8 준수
- Type hints 사용 권장
- Docstring 작성 (Google 스타일)

### 테스트

```bash
# 전체 테스트 실행 (새로운 오프라인 모킹 시스템)
python -m pytest backend/tests/ -v

# 계층별 테스트 실행
python -m pytest backend/tests/unit/ -v           # 단위 테스트
python -m pytest backend/tests/integration/ -v   # 통합 테스트
python -m pytest backend/tests/e2e/ -v           # E2E 테스트

# 커버리지와 함께 실행
python -m pytest backend/tests/ --cov=app --cov-report=html

# Docker 환경에서 테스트 실행
docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec backend pytest tests/ -v
```

**개발 환경 주의사항:**
- 개발 환경에서는 볼륨 마운트(`./backend:/app`)로 인해 로컬 파일 변경 시 즉시 반영됨
- 새로운 테스트 파일 추가 후에는 컨테이너 재빌드 필요: `docker-compose up --build`
- 테스트 파일 수정은 로컬에서 하고, 실행은 Docker 컨테이너에서 하는 것을 권장

**테스트 특징:**
- **완전 오프라인**: yfinance API, MySQL 연결 없이 실행
- **수학적 모의 데이터**: 기하 브라운 운동으로 현실적 주가 생성
- **CI/CD 호환**: 젠킨스 우분투 환경에서 100% 안정성
- **DB 스키마 준수**: 실제 데이터베이스 구조와 동일한 모의 데이터
- **시나리오 테스트**: bull_market, bear_market, volatile 등 다양한 시장 상황

### 배포

프로덕션 배포 시 고려사항:

- 환경 변수 설정 (`.env` 파일)
- 데이터베이스 마이그레이션
- SSL 인증서 설정
- 로그 레벨 조정

자세한 배포 가이드는 프로젝트 루트의 `README.md`를 참조하세요.

## 관련 문서

- **[API 가이드](./api.md)**: 백엔드 API 엔드포인트 및 사용법
- **[네이버 뉴스 API](./naver_news_api.md)**: 종목별 뉴스 검색 기능
- **[테스트 아키텍처](./TEST_ARCHITECTURE.md)**: 백엔드 테스트 구조 및 모킹 시스템
- **[현금 자산 처리](./CASH_ASSETS.md)**: 무위험 현금 자산 처리 로직
