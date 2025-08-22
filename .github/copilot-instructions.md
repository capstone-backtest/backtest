## 목표
이 저장소에서 AI 코딩 에이전트가 빠르게 생산적으로 작업하기 위한 간단·구체적 가이드입니다. 핵심 아키텍처, 실행/디버그 명령, 프로젝트 고유 패턴, 그리고 코드 예시를 포함합니다.

## 빠른 개요 (한 줄)
백엔드는 FastAPI(포트 8000, `backend/`), 프론트엔드는 Vite 기반 React(개발: 5173, 프로덕션: 8080), 전략은 `backend/app/services/strategy_service.py`에 등록된 Strategy 클래스들을 사용해 `backtesting` 라이브러리로 실행됩니다.

## 주요 위치
- 백엔드 엔트리: `backend/run_server.py` (uvicorn 실행)
- FastAPI 앱: `backend/app/main.py`
- API 라우트: `backend/app/api/v1/endpoints/` (예: `backtest.py`, `strategies.py`)
- 전략 정의 & 등록: `backend/app/services/strategy_service.py` (여기에 전략 클래스와 메타데이터가 있음)
- 백테스트 실행 로직: `backend/app/services/backtest_service.py`
- 데이터 페처(캐시 포함): `backend/app/utils/data_fetcher.py` (기본 캐시 디렉토리: `data_cache/`)
- 프로젝트 루트: Docker Compose 파일들 (`docker-compose.yml`, `docker-compose.override.yml`, `docker-compose.prod.yml`)
 - 데이터 페처(캐시 포함): `backend/app/utils/data_fetcher.py` (기본 캐시 디렉토리: `data_cache/`) — 추가로 MySQL에 원본 응답을 저장하는 `yfinance` 캐시 엔드포인트(`/api/v1/yfinance/fetch-and-cache`)가 있습니다. 이 엔드포인트는 티커·기간을 받아 yfinance에서 시계열(OHLCV)을 불러와 `stocks`와 `daily_prices`에 upsert합니다.

## 아키텍처·데이터 흐름 (요지)
- 클라이언트 -> FastAPI 엔드포인트(`/api/v1/backtest/*`) 호출
- 엔드포인트가 `BacktestRequest`를 파싱해 `backtest_service`에 전달
- `backtest_service`는 `data_fetcher.get_stock_data()`로 OHLCV를 로드(파일 캐시 우선)
- 전략 클래스는 `strategy_service.get_strategy_class()`로 조회되어 `backtesting.Backtest`에 주입됨
- 결과는 `BacktestResult` 형태로 반환되고, 차트 데이터는 `generate_chart_data()`로 변환되어 JSON 응답을 생성

## 프로젝트-특화 규칙 / 패턴 (에이전트가 알아야 할 것)
- 전략 등록은 `StrategyService._strategies` 딕셔너리에 key(예: `sma_crossover`)와 `class`(Strategy 서브클래스)를 추가하는 방식입니다. 또한 `parameters` 메타데이터(타입, 기본값, min/max)를 포함해야 합니다.
- 전략 클래스의 파라미터는 클래스 속성으로 선언합니다. 백테스트 실행 시 `BacktestService`가 `setattr(strategy_class, param, value)`로 값을 설정합니다.
- 데이터 캐시는 `backend/data_cache/` (기본값 `data_cache`)에 CSV로 저장되며, 과거 데이터(종료일 < 오늘)는 영구 캐시, 현재/미래 데이터는 `max_cache_age_hours` (기본 24h)로 만료 처리됩니다.
- 최적화(optimization): 백엔드에서 `Backtest.optimize()`를 사용하며, 파라미터 범위는 [min,max]로 전달되고 정수이면 range로, 실수이면 11개 샘플(10단계)으로 변환됩니다.
- 오류 대응: backtesting 내부 오류(예: Timedelta 관련)는 `_patch_backtesting_stats()`로 폴백 처리합니다. 에러 시 fallback 결과(대부분 Buy & Hold)로 대체됩니다.

## 실행, 빌드, 테스트 — 핵심 명령
- 로컬 개발(권장): docker compose up --build (개발 override 자동 병합)
  - 루트 README에 예시가 있음: `docker compose up --build` 또는 `docker-compose up --build`(Compose v1)
- 백엔드 단독(로컬 Python): `python backend/run_server.py` — 설정은 `backend/.env` 또는 루트 `.env`를 사용합니다(`app/core/config.py`의 env_file은 `.env`).
- 테스트 스크립트(간단한 통합): `python backend/test_api.py` 및 `python backend/test_chart_api.py` — 두 파일은 로컬 서버가 실행 중일 때 API를 호출합니다.

## 빈번한 코드 변경 포인트(PR 시 체크리스트)
- 전략 추가 시
  - `backend/app/services/strategy_service.py`에 Strategy 클래스와 `_strategies` 엔트리 추가
  - `parameters` 메타데이터(타입, default, min, max) 정의
  - 만약 외부 의존 라이브러리가 필요하면 `backend/requirements.txt`에 추가
- 데이터 관련 변경
  - `backend/app/utils/data_fetcher.py`는 yfinance를 사용. 외부 API 키 불필요. 캐시 경로·유효기간 변경은 `app/core/config.py` 참조
- API 변경
  - 새 엔드포인트는 `backend/app/api/v1/endpoints/`에 추가. Response/Request 모델은 `backend/app/models/`에 추가/확장

## 예시 스니펫(에이전트가 자주 사용할 것)
- 전략 등록 예시 (간단 발췌):
  - key: 'sma_crossover'
  - class: SMAStrategy (서브클래스는 backtesting.Strategy를 상속)
  - parameters: { 'short_window': {type:'int', default:10, min:5, max:50}, 'long_window': {...} }

- 데이터 캐시 파일명: `{TICKER}_{start_date}_{end_date}.csv` (예: AAPL_2023-01-01_2023-12-31.csv)

## 외부 의존성 및 통합 포인트
- Yahoo Finance via yfinance (데이터 수집 및 ticker 검증)
- backtesting.py (전략 실행, optimize, stats)
- Docker Compose (전체 서비스: FastAPI + frontend + nginx in prod)

## 제한 사항 / 주의점 (현 코드에서 관찰된 것)
- 전략 파라미터를 클래스 속성으로 설정하는 접근은 동시성(여러 요청이 동일 클래스의 class 속성을 덮어씀) 문제를 일으킬 수 있습니다. 멀티스레드/멀티요청 환경에서 안전하지 않을 수 있음 — 변경 시 주의.
- yfinance의 MultiIndex 컬럼 처리, 컬럼 누락 처리(예: Volume 없음 → 0) 같은 데이터 정제 로직이 있어 에이전트가 데이터 컬럼 존재 여부를 항상 확인해야 합니다.
- 일부 경로는 `backend/app/...` 에 존재함. 실행/참조할 때 루트가 `backend/`인 것처럼 상대경로를 잡지 않도록 주의.

## 작업 제안(우선순위)
1. 전략 추가/변경: `backend/app/services/strategy_service.py`를 직접 수정(간단함)
2. 새로운 엔드포인트 추가: `backend/app/api/v1/endpoints/`에 파일 추가 및 `app/api/api.py`에 라우터 등록
3. 데이터 캐시 정책 변경: `app/core/config.py`의 `data_cache_dir` 및 `max_cache_age_hours` 수정

## 피드백 요청
이 문서는 코드베이스에서 자동으로 발견한 내용만을 기반으로 작성했습니다. 누락되었거나 불명확한 부분(예: 실제 배포 파이프라인, env 파일의 예시, 프론트엔드 개발 워크플로 상세)은 알려주시면 바로 반영하겠습니다.
