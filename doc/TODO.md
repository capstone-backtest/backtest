# To-Do

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

- 모든 변경 후 `python -c "import sys; sys.path.insert(0, 'backend'); import app; print('IMPORT_OK')"`로 모듈 임포트 확인 (세션에서 실행되어 `IMPORT_OK` 확인됨)
- `docker compose up --build`로 전체 스택을 띄운 뒤 프론트엔드/백엔드 E2E 시나리오(예: 브라우저에서 백테스트 실행)를 재현