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
