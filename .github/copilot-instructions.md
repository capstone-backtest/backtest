## 목표
이 저장소에서 AI 코딩 에이전트가 빠르게 생산적으로 작업하기 위한 간단·구체적 가이드입니다. 핵심 아키텍처, 실행/디버그 명령, 프로젝트 고유 패턴, 그리고 코드 예시를 포함합니다.

## 빠른 개요 (한 줄)
백엔드는 FastAPI(포트 8000, `backend/`), 프론트엔드는 Vite 기반 React(개발: 5173, 프로덕션: 8080), 전략은 `backend/app/services/strategy_service.py`에 등록된 Strategy 클래스들을 사용해 `backtesting` 라이브러리로 실행됩니다.

## 주요 위치
  - `backend/app/utils/data_fetcher.py`는 yfinance를 사용합니다. yfinance 관련 API 및 DB 캐시 동작의 상세 설명은 `doc/backend/api.md`를 참조하세요. 캐시 경로·유효기간 변경은 `app/core/config.py`를 참조하세요.

## 아키텍처·데이터 흐름 (요지)

## 프로젝트-특화 규칙 / 패턴 (에이전트가 알아야 할 것)
  - 코드를 수정하면 항상 마크다운 문서에 최신화를 해야 한다.

## 실행, 빌드, 테스트 — 핵심 명령
  - 개발 환경은 윈도우, powershell, 도커 데스크탑을 사용한다.
  - `docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build`로 프로젝트를 실행한다.

## 빈번한 코드 변경 포인트(PR 시 체크리스트)
  - `backend/app/services/strategy_service.py`에 Strategy 클래스와 `_strategies` 엔트리 추가
  - `parameters` 메타데이터(타입, default, min, max) 정의
  - 만약 외부 의존 라이브러리가 필요하면 `backend/requirements.txt`에 추가
  - `backend/app/utils/data_fetcher.py`는 yfinance를 사용. 외부 API 키 불필요. 캐시 경로·유효기간 변경은 `app/core/config.py` 참조
  - 새 API는 `backend/app/api/v1/endpoints/`에 추가. Response/Request 모델은 `backend/app/models/`에 추가/확장

## 예시 스니펫(에이전트가 자주 사용할 것)
  - key: 'sma_crossover'
  - class: SMAStrategy (서브클래스는 backtesting.Strategy를 상속)
  - parameters: { 'short_window': {type:'int', default:10, min:5, max:50}, 'long_window': {...} }

## 외부 의존성 및 통합 포인트

## 제한 사항 / 주의점 (현 코드에서 관찰된 것)