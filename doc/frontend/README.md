# 문서 위치 안내

- 중앙 문서 인덱스: `../README.md`
- 프론트엔드 상세 문서: 이 파일(`README.md`) 및 `doc/frontend/` 하위 문서

# Frontend Dashboard

## 1. 개요

이 프로젝트는 백테스팅 API 서버의 결과를 시각화하는 React 기반의 프론트엔드 애플리케이션입니다. 사용자는 웹 인터페이스를 통해 간편하게 백테스트를 실행하고, Recharts를 활용한 인터랙티브 차트를 통해 그 결과를 직관적으로 분석할 수 있습니다.

## 2. 주요 기능

*   **인터랙티브 차트**: OHLC, 자산 곡선, 거래 내역 등 다양한 차트 제공
*   **다양한 전략 지원**: 이동평균 교차, RSI, 볼린저 밴드 등 여러 전략 테스트 가능
*   **동적 파라미터 설정**: 각 전략에 맞는 파라미터를 실시간으로 변경하며 테스트
*   **성과 분석 대시보드**: 수익률, 승률, 최대 손실 등 핵심 지표를 한눈에 파악
*   **반응형 디자인**: 데스크톱, 태블릿, 모바일 등 다양한 기기 지원

## 3. 기술 스택

*   **Framework**: React 18 + TypeScript
*   **Build Tool**: Vite
*   **UI/UX**: Bootstrap, React Bootstrap, Tailwind CSS
*   **Charting**: Recharts
*   **HTTP Client**: Axios

## 4. 프로젝트 구조

```
frontend/
├── public/             # 정적 에셋
├── src/
│   ├── App.tsx         # 메인 애플리케이션 컴포넌트
│   ├── main.tsx        # React 진입점
│   └── types/          # TypeScript 타입 정의
├── Dockerfile          # Docker 이미지 빌드 설정
├── package.json        # 의존성 및 스크립트 관리
├── vite.config.ts      # Vite 설정
└── README.md           # 프론트엔드 README
```

## 5. 시작하기

이 프로젝트는 Docker Compose를 사용하여 모든 서비스(백엔드, 프론트엔드, 데이터베이스)를 한 번에 실행하는 것을 권장합니다. 자세한 내용은 프로젝트 루트의 [README.md](../README.md)를 참고하세요.

### 로컬 개발 환경 (Docker 미사용)

1.  **의존성 설치**
    ```bash
    npm install
    ```

2.  **API 서버 연결 설정**
    로컬 개발 시 백엔드 API 서버와 통신하기 위해 `vite.config.ts` 파일에 프록시 설정을 추가합니다.
    ```typescript
    // vite.config.ts
    export default defineConfig({
      server: {
        proxy: {
          '/api': {
            target: 'http://localhost:8000', // 로컬 백엔드 서버 주소
            changeOrigin: true,
          }
        }
      }
    })
    ```

3.  **개발 서버 실행**
    ```bash
    npm run dev
    ```
    이제 `http://localhost:5173` (또는 다른 포트)에서 개발 서버에 접속할 수 있습니다.

## 6. 사용 가능한 스크립트

*   `npm run dev`: 개발 서버를 시작합니다.
*   `npm run build`: 프로덕션용으로 애플리케이션을 빌드합니다.
*   `npm run lint`: ESLint를 사용하여 코드 품질을 검사합니다.
*   `npm run preview`: 프로덕션 빌드 결과물을 미리 봅니다.

## 7. Docker

`frontend/Dockerfile`은 프로덕션용 React 애플리케이션을 빌드하고 Nginx를 사용하여 정적 파일로 서비스하도록 구성되어 있습니다.

**주요 단계:**

1.  **Build Stage**: Node.js 환경에서 `npm install` 및 `npm run build`를 실행하여 빌드 결과물을 생성합니다.
2.  **Final Stage**: Nginx 이미지를 기반으로, 빌드된 정적 파일들을 Nginx의 웹 루트 디렉터리로 복사합니다.

이 이미지는 프로젝트 루트의 `docker-compose.yml` 파일에서 `frontend` 서비스로 정의되어 다른 서비스들과 함께 실행됩니다.


# 프론트엔드 문서 모음

## 개요

백테스팅 프론트엔드 애플리케이션의 모든 문서를 모아놓은 디렉토리입니다. 사용법부터 개발 가이드까지 체계적으로 정리되어 있습니다.

## 문서 목록

### 시작하기
- **[../README.md](../README.md)** - 메인 프로젝트 문서
  - 프로젝트 개요 및 주요 기능
  - 설치 및 실행 방법
  - 기술 스택 및 디펜던시
  - 사용 가이드 및 문제 해결

### 개발자 가이드
- **[DEVELOPMENT.md](DEVELOPMENT.md)** - 개발 환경 설정 및 아키텍처
  - 프로젝트 구조 및 컴포넌트 분석
  - 상태 관리 패턴
  - 스타일링 전략
  - 성능 최적화 방법
  - 코딩 컨벤션

### 컴포넌트 가이드
- **[COMPONENTS.md](COMPONENTS.md)** - UI 컴포넌트 상세 설명
  - 컴포넌트 계층 구조
  - 각 컴포넌트 기능 및 구현
  - 프로퍼티 및 이벤트 핸들링
  - 스타일링 방법
  - 상태 관리 패턴

### API 연동
- **[API_GUIDE.md](API_GUIDE.md)** - 백엔드 API 연동 가이드
  - API 엔드포인트 및 데이터 타입
  - 요청/응답 형식
  - 에러 처리 방법
  - 테스트 및 디버깅

## 문서별 사용 목적

### 새로운 개발자를 위한 순서
1. **[../README.md](../README.md)** - 프로젝트 전체적인 이해
2. **[DEVELOPMENT.md](DEVELOPMENT.md)** - 개발 환경 설정
3. **[COMPONENTS.md](COMPONENTS.md)** - UI 컴포넌트 구조 파악
4. **[API_GUIDE.md](API_GUIDE.md)** - API 연동 방법 학습

### 기능 개발 시 참고 순서
1. **[DEVELOPMENT.md](DEVELOPMENT.md)** - 아키텍처 및 패턴 확인
2. **[COMPONENTS.md](COMPONENTS.md)** - 관련 컴포넌트 분석
3. **[API_GUIDE.md](API_GUIDE.md)** - 데이터 타입 및 API 명세
4. **[../README.md](../README.md)** - 사용자 관점에서 기능 검증

### UI/UX 개선 시 참고 순서
1. **[COMPONENTS.md](COMPONENTS.md)** - 기존 컴포넌트 구조 파악
2. **[DEVELOPMENT.md](DEVELOPMENT.md)** - 스타일링 시스템 확인
3. **[../README.md](../README.md)** - 사용자 경험 관점 검토

### 문제 해결 시 참고 순서
1. **[../README.md](../README.md)** - 일반적인 문제 해결
2. **[API_GUIDE.md](API_GUIDE.md)** - API 관련 에러 처리
3. **[DEVELOPMENT.md](DEVELOPMENT.md)** - 개발 환경 관련 이슈
4. **[COMPONENTS.md](COMPONENTS.md)** - 컴포넌트 관련 오류

## 프론트엔드 아키텍처 요약

```
React 18 + TypeScript + Vite
├── UI 프레임워크
│   ├── Bootstrap 5.3.6 (컴포넌트)
│   ├── React Bootstrap 2.10.10 (React 래퍼)
│   └── Tailwind CSS 3.3 (유틸리티)
├── 차트 라이브러리  
│   └── Recharts 2.9.0 (React 네이티브)
├── 개발 도구
│   ├── ESLint (코드 품질)
│   ├── TypeScript (타입 안전성)
│   └── Vite (빌드 도구)
└── API 통신
    ├── Fetch API (HTTP 클라이언트)
    ├── Vite Proxy (개발 환경)
    └── Axios (향후 계획)
```

## 주요 컴포넌트 구조

```
App.tsx (843줄)
├── 입력 폼 섹션
│   ├── 티커 입력
│   ├── 날짜 선택
│   ├── 투자금 설정
│   ├── 전략 선택 (5가지)
│   └── 파라미터 설정 (동적)
├── 프리셋 버튼
│   ├── AAPL 2023
│   ├── TSLA 2022
│   └── NVDA 2023
├── 차트 시각화
│   ├── OHLCChart (가격 + 기술지표)
│   ├── EquityChart (수익률 곡선)
│   └── TradesChart (거래 분석)
├── 성과 지표 대시보드
│   └── StatsSummary (6개 지표 카드)
└── 상태 관리
    ├── 백테스트 파라미터
    ├── API 응답 데이터
    └── UI 상태 (로딩, 에러)
```

## 데이터 플로우

```
1. 사용자 입력
   ↓
2. 파라미터 검증
   ↓
3. API 호출 (/api/v1/backtest/chart-data)
   ↓
4. 응답 데이터 수신
   ↓
5. 차트 데이터 변환
   ↓
6. Recharts 렌더링
   ↓
7. 성과 지표 계산 및 표시
```

## 지원하는 투자 전략

| 전략 | 설명 | 파라미터 | 구현 위치 |
|------|------|----------|-----------|
| **Buy & Hold** | 매수 후 보유 | 없음 | `App.tsx:400-450` |
| **SMA Crossover** | 이동평균 교차 | 단기/장기 기간 | `App.tsx:451-520` |
| **RSI Strategy** | 과매수/과매도 | RSI 기간, 임계값 | `App.tsx:521-590` |
| **Bollinger Bands** | 볼린저 밴드 | 기간, 표준편차 배수 | `App.tsx:591-650` |
| **MACD Strategy** | MACD 교차 | 빠른/느린/시그널 기간 | `App.tsx:651-720` |

## 표시하는 성과 지표

| 지표 | 설명 | 계산 방법 | 구현 위치 |
|------|------|-----------|-----------|
| **총 수익률** | 투자 수익률 | (최종값 - 초기값) / 초기값 × 100 | `App.tsx:780` |
| **총 거래 수** | 거래 횟수 | 매수/매도 카운트 | `App.tsx:785` |
| **승률** | 수익 거래 비율 | 수익 거래 / 전체 거래 × 100 | `App.tsx:790` |
| **최대 손실** | 최대 드로우다운 | 고점 대비 최대 하락폭 | `App.tsx:795` |
| **샤프 비율** | 위험 대비 수익 | (평균 수익률 - 무위험 수익률) / 표준편차 | `App.tsx:800` |
| **수익 팩터** | 이익/손실 비율 | 총 이익 / 총 손실 | `App.tsx:805` |

## 차트 컴포넌트 구조

### OHLCChart (가격 차트)
- **위치**: `App.tsx:450-550`
- **기능**: OHLC 데이터, 기술 지표, 거래량, 거래 마커
- **라이브러리**: Recharts ComposedChart
- **Y축**: 이중 축 (가격/거래량)

### EquityChart (자산 곡선)
- **위치**: `App.tsx:550-620`
- **기능**: 누적 수익률, 드로우다운 영역
- **라이브러리**: Recharts ComposedChart
- **기준선**: 0% 손익분기점

### TradesChart (거래 분석)
- **위치**: `App.tsx:620-690`
- **기능**: 개별 거래 손익 스캐터 플롯
- **라이브러리**: Recharts ScatterChart
- **색상**: 수익(초록)/손실(빨강) 구분

## 개발 환경 요구사항

### 필수 도구
- **Node.js**: 16.0.0 이상
- **npm**: 8.0.0 이상
- **브라우저**: Chrome 90+, Firefox 88+, Safari 14+

### 권장 도구
- **VS Code**: TypeScript 지원
  - 추천 확장: ES7+ React/Redux/React-Native snippets
  - 추천 확장: TypeScript Importer
  - 추천 확장: Auto Rename Tag
- **Chrome DevTools**: 디버깅 및 네트워크 분석
- **React Developer Tools**: 컴포넌트 인스펙션

### 백엔드 의존성
- **API 서버**: http://localhost:8000
- **엔드포인트**: `/api/v1/backtest/chart-data`
- **프로토콜**: HTTP/HTTPS
- **응답 형식**: JSON

## 향후 개발 계획

### 단기 목표 (1-2개월)
- [ ] **컴포넌트 분리**: App.tsx를 여러 파일로 분할
- [ ] **Jest + Testing Library**: 단위 테스트 추가
- [ ] **React Query**: 서버 상태 관리 개선
- [ ] **Storybook**: 컴포넌트 문서화

### 중기 목표 (3-6개월)
- [ ] **다크 모드**: 테마 전환 기능
- [ ] **데이터 내보내기**: CSV/Excel 내보내기
- [ ] **커스텀 지표**: 사용자 정의 지표 생성기
- [ ] **PWA**: Progressive Web App 지원

### 장기 목표 (6개월+)
- [ ] **실시간 데이터**: WebSocket 스트리밍
- [ ] **포트폴리오 백테스팅**: 다중 자산 지원
- [ ] **모바일 앱**: React Native 포팅
- [ ] **AI 추천**: 전략 최적화 자동화

## 문의 및 지원

### 개발 관련
- **GitHub Issues**: 버그 리포트 및 기능 요청
- **Pull Requests**: 코드 기여 및 개선사항
- **Discussions**: 아이디어 및 질문

### 사용법 관련
- **README.md**: 기본 사용법 및 설치 가이드
- **예제 코드**: 각 문서에 포함된 코드 샘플
- **API 테스트**: 브라우저 개발자 도구 활용

### 문서 기여
이 문서들을 개선하고 싶으시다면:
1. 오타나 오류 발견 시 Issue 제출
2. 새로운 가이드나 예제 추가 제안
3. 코드 변경 시 관련 문서 업데이트

---

**참고**: 이 문서들은 실제 코드와 동기화되어 있으며, 코드 변경 시 문서도 함께 업데이트됩니다. 최신 정보는 각 개별 문서를 참고해주세요.

**문서 버전**: v1.0.0 (2024년 1월 기준)
**마지막 업데이트**: 실제 App.tsx 구현 기반으로 전면 개편