# 백엔드 (FastAPI) 안내

간단한 목적: 로컬 개발과 Docker 환경에서 FastAPI 서버를 실행하고 MySQL 기반 yfinance 캐시/백엔드 API를 테스트하는 방법을 정리합니다.

요약
- 프레임워크: FastAPI
- 포트(기본): 8000
- 주요 기술: SQLAlchemy, PyMySQL, pandas, yfinance
- DB 스키마: `database/yfinance.sql`

사전 준비
- Python 3.10+ (권장)
- 가상환경(.venv) 또는 시스템 Python
- MySQL (로컬 또는 컨테이너)
- Docker & Docker Compose (선택)

환경변수
- 백엔드 구성은 환경 변수를 사용합니다. 예시 파일은 `backend/env.example`을 참조하세요.
- 중요: `DATABASE_URL` (예: `mysql+pymysql://user:pass@host:3306/stock_data_cache`)
  - Docker 내부에서 DB에 접속할 때는 호스트명이 달라질 수 있습니다(예: `db`, `host.docker.internal`).

로컬 개발 (venv)
1. 가상환경 활성화(Windows PowerShell):
```powershell
python -m venv .venv; .\.venv\Scripts\Activate.ps1
```
2. 의존성 설치:
```powershell
pip install -r requirements.txt
```
3. 환경 파일 복사/수정:
```powershell
cp env.example .env
# .env에서 DATABASE_URL 등 값을 설정
```
4. 개발 서버 실행 (uvicorn):
```powershell
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

도커(권장: 전체 스택)
- 루트 README에 있는 Docker Compose 설정을 사용하세요. 개발용 override가 포함되어 있어 프론트엔드와 백엔드를 동시에 띄우기 편합니다.

```powershell
# Compose v2 (권장)
docker compose up --build

# 프로덕션/미리보기
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

데이터베이스 초기화
- 스키마 생성 스크립트: `database/yfinance.sql`을 사용해 MySQL에 테이블을 생성하세요.
- 예: 로컬 MySQL에 직접 적용하거나 도커 컨테이너 내부에서 실행합니다.

yfinance 및 캐시
- 프로젝트는 CSV 로컬 캐시를 사용하지 않고 MySQL을 1차 캐시로 사용합니다 (DB-first).
- yfinance 관련 API 및 캐시 동작(예: `/api/v1/yfinance/fetch-and-cache` 및 v2 보충 동작)은 `doc/backend/api.md`에 정리되어 있습니다. 운영·동시성 주의사항 및 권장 패턴도 문서화되어 있으니 참고하세요.

핵심 API(개요)
- v1: 레거시/직접 fetch API(예: `/api/v1/yfinance/fetch-and-cache`, `/api/v1/backtest/*`)
- v2: DB 기반 재사용(backtest와 차트 데이터 제공 시 DB에서 읽어 부족분은 yfinance로 보충)

전략(Strategies) 추가
- 전략은 `strategies/` 폴더에 새로운 Python 파일로 추가하고, `backend/app/services/strategy_service.py`에서 등록하면 API로 노출됩니다.
- 전략은 `backtesting.Strategy`를 상속해 구현하고, `parameters` 메타데이터를 통해 UI에서 노출할 파라미터를 정의하세요.

테스트 및 검증
- 간단한 import 체크(백엔드 모듈이 문제없이 로드되는지 확인):
```powershell
python -c "import sys; sys.path.insert(0, 'C:/Users/USER/source/backtest/backend'); import app; print('IMPORT_OK')"
```
위 커맨드는 이 저장소의 백엔드 코드가 import 가능한지 빠르게 검사합니다. (세션에서 실행되어 `IMPORT_OK`를 확인했습니다.)

- 프로젝트 루트에 포함된 단위/통합 테스트 스크립트(`backend/test_api.py`, `backend/test_chart_api.py`)를 사용해 추가 검증을 수행하세요.

운영/문제 해결 팁
- DB 연결 오류: `DATABASE_URL`을 확인하고 MySQL이 작동 중인지 확인하세요.
- yfinance가 빈 결과 반환 시: 요청 기간이 휴일/딜리스트/딜리스트된 종목인지 확인하고 `doc/TODO.md`에 기록된 개선 항목(명확한 HTTP 에러 매핑)을 참고하세요.
- 동시성 문제(중복 fetch): 현재 간단한 동시성 보호는 구현되어 있지 않으므로, 프로덕션에서는 Redis 기반 락 또는 작업 큐 도입을 권장합니다.

향후 작업(권장)
- 프론트엔드 타입/린트 정리: `frontend/src/App.tsx` 리팩터로 인한 TypeScript 경고/오류 수리
- DB 마이그레이션: Alembic 도입 고려(현재 Alembic 설정은 포함되어 있지 않음)
- 백필 대량 작업은 비동기 작업 큐로 전환(RQ/Celery)

참고 문서
- 중앙 API 문서: `doc/backend/api.md`
- 개발 가이드(프로젝트 루트): `README.md`
- 작업 TODO/이슈: `doc/TODO.md`
