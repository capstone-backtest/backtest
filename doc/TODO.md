# To-Do

이 파일은 코드베이스 상태를 바탕으로 우선순위가 높은 개선 사항과 문서-코드 불일치 항목을 정리합니다.

## 문서와 코드의 불일치

- [x] CSV 캐시: 루트 문서와 `.github/copilot-instructions.md`에는 CSV 기반 로컬 캐시 동작이 설명되어 있었으나, 현재 `backend/app/utils/data_fetcher.py`에서는 CSV 입출력이 비활성화되어 있습니다. 관련 문서를 최신화했습니다.
- [x] Alembic: 루트 README에 Alembic이 언급되어 있으나 마이그레이션 스크립트(또는 alembic 디렉터리)가 리포지토리에 포함되어 있지 않습니다. README에서 Alembic 언급을 제거하거나 Alembic 설정을 추가해야 합니다. 이번에 README에서 언급을 제거했습니다.

## 문서 통합 진행 상황

- [x] 중앙 `doc/` 폴더 생성 및 인덱스(`doc/README.md`) 추가
- [x] `backend/doc`와 `frontend/doc`에서 주요 문서를 `doc/backend/`, `doc/frontend/`로 이동(사용자 수행)
- [x] 각 서브문서 상단에 중앙 인덱스 링크 안내 추가

## 다음 정리 작업 (제가 도와드릴 수 있음)

- [ ] 중복 내용 병합: `doc/backend/api.md`와 루트 `README.md`의 yfinance/엔드포인트 설명 중복 제거 및 하나의 진입점으로 병합
 - [x] 중복 내용 병합: yfinance/엔드포인트 설명은 `doc/backend/api.md`를 현재의 단일 진입점으로 정리했습니다.
- [ ] 프론트엔드 컴포넌트 문서(`doc/frontend/COMPONENTS.md`)에서 코드 예시를 최신 `frontend/src/components` 구조에 맞게 정리
- [ ] 문서 스타일 통일: 마크다운 헤더 레벨·용어(예: API vs 엔드포인트) 표준화
- [ ] (선택) MkDocs 설정을 추가해 정적 문서 사이트로 빌드 가능하게 구성

## 우선 순위 작업 (권장)

1. 핵심(High)
   - [ ] 프론트엔드 타입/린트 정리: `frontend/src/App.tsx` 리팩터로 인한 TypeScript 오류를 해결해 Vite dev 서버가 에러 없이 실행되도록 합니다.
   - [ ] DB-only 흐름 검증: 새로운 티커/기간 요청 시 CSV가 생성되지 않고 MySQL에 정상 업서트되는지(동시성 포함) E2E로 검증합니다.
   - [ ] 실패 케이스 대응: yfinance가 빈 결과를 반환할 때 사용자에게 명확한 HTTP 에러(예: 404/422)와 로그를 제공하도록 백엔드를 개선합니다.

2. 개선(Medium)
   - [ ] 동시성 보호: `load_ticker_data`/`save_ticker_data`에 간단한 프로세스 내 락 또는 DB 기반 세마포어를 도입해 중복 fetch를 방지합니다.
   - [ ] 백그라운드 보충(fetch-and-upsert): 대량 또는 느린 fetch 작업은 API 호출에서 동기적으로 처리하지 말고 작업 큐(예: Redis+RQ/Celery)로 비동기화 고려.
   - [ ] yfinance 재시도 정책 개선: 휴일/주말/딜리스트 등 경계 케이스를 더 잘 처리하도록 달력/마켓 휴일 인식 로직 추가.

3. 장기(Low)
   - [ ] Alembic 도입 또는 문서에서 완전 제거: DB 스키마 변경이 빈번하다면 Alembic 마이그레이션을 추가합니다.
   - [ ] 로깅/모니터링: 데이터 백필/백테스트 실패 추적을 위한 Sentry/Prometheus 연동.

## 검증 체크리스트

- [ ] 모든 변경 후 `python -c "import sys; sys.path.insert(0, 'backend'); import app; print('IMPORT_OK')"`로 모듈 임포트 확인
- [ ] `docker compose up --build`로 전체 스택을 띄운 뒤 프론트엔드/백엔드 E2E 시나리오(예: 브라우저에서 백테스트 실행)를 재현

---

원하시면 우선 순위 항목 중 하나를 제가 바로 구현하고 테스트해 드리겠습니다.