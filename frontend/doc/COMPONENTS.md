# 프론트엔드 컴포넌트

이 문서는 프론트엔드 애플리케이션에서 사용되는 주요 React 컴포넌트에 대한 개요를 제공합니다.

## 컴포넌트 개요

주요 애플리케이션 로직은 다양한 컴포넌트를 조율하는 `App.tsx` 파일에 포함되어 있습니다. 주요 컴포넌트는 다음과 같습니다:

*   **`StatsSummary`**: 카드 기반 레이아웃으로 백테스트 결과 요약을 표시합니다.
*   **`OHLCChart`**: 거래량 및 기술 지표를 포함한 OHLC(시가, 고가, 저가, 종가) 캔들스틱 차트를 렌더링합니다.
*   **`EquityChart`**: 자산 곡선 및 손실률 차트를 렌더링합니다.
*   **`TradesChart`**: 개별 거래의 산점도를 렌더링합니다.

## `StatsSummary`

이 컴포넌트는 백테스트 결과에서 핵심 성과 지표(KPI) 세트를 표시합니다.

### Props

*   `stats`: 백테스트 결과의 요약 통계를 포함하는 객체입니다.

### 사용법

```tsx
<StatsSummary stats={backtestResult.summary_stats} />
```

### 구현 세부 정보

*   레이아웃에 `react-bootstrap`의 `Card` 및 `Badge` 컴포넌트를 사용합니다.
*   각 통계는 아이콘, 레이블 및 값과 함께 별도의 카드에 표시됩니다.
*   배지의 색상은 통계 값에 따라 결정됩니다(예: 양수 수익률은 녹색, 음수 수익률은 빨간색).

## `OHLCChart`

이 컴포넌트는 주요 캔들스틱 차트를 렌더링합니다.

### Props

*   `data`: OHLC 데이터 포인트 배열입니다.
# 프론트엔드 컴포넌트 (업데이트됨)

이 문서는 실제 코드(`frontend/src/components`)에 있는 컴포넌트들을 기준으로 각 컴포넌트의 파일 위치, 주요 props와 간단한 사용 예시를 정리합니다.

경로: `frontend/src/components`

목록(파일 기준):
- `OHLCChart.tsx` — OHLC + 거래량 + 지표 + 거래 마커
- `EquityChart.tsx` — 자산(Equity) 곡선 + Drawdown
- `TradesChart.tsx` — 거래별 PnL 산점도
- `StatsSummary.tsx` — 카드형 성과 요약
- `CustomTooltip.tsx` — Recharts 툴팁 커스텀 렌더러
- `BacktestForm.tsx` — 백테스트 입력 폼

---

## OHLCChart

- 파일: `frontend/src/components/OHLCChart.tsx`
- 설명: OHLC(종가 선으로 표현), 거래량 바, 여러 기술지표(선), 그리고 거래 마커(ReferenceLine)를 함께 그립니다.

### Props
- `data: Array<{ date: string; open?: number; high?: number; low?: number; close?: number; volume?: number }>`
- `indicators: Array<{ name: string; color?: string; data?: Array<{ date: string; value: number }> }>`
- `trades: Array<{ date: string; type: 'entry' | 'exit'; side?: 'buy'|'sell'; price?: number; pnl_pct?: number }>`

### 사용 예
```tsx
<OHLCChart
	data={chartData.ohlc_data}
	indicators={chartData.indicators}
	trades={chartData.trade_markers}
/>
```

### 노트
- 내부에서 `recharts`의 `ComposedChart`, `Bar`, `Line`, `ReferenceLine` 등을 사용합니다.
- `indicators`의 각 항목은 `name` 키를 차트의 데이터Key로 추가해 선으로 렌더링됩니다.

---

## EquityChart

- 파일: `frontend/src/components/EquityChart.tsx`
- 설명: 백테스트 기간 동안의 equity(누적 자산) 변동을 선으로, drawdown을 영역(Area)으로 그립니다.

### Props
- `data: Array<{ date: string; equity?: number; return_pct?: number; drawdown_pct?: number }>`

### 사용 예
```tsx
<EquityChart data={chartData.equity_data} />
```

### 노트
- `ComposedChart`와 `Area`를 조합해 수익률과 드로우다운을 동시에 표시합니다.
- 툴팁은 `CustomTooltip`을 사용합니다.

---

## TradesChart

- 파일: `frontend/src/components/TradesChart.tsx`
- 설명: 종료(Exit) 거래들의 PnL(%)을 산점도로 표시합니다. 색상은 손익(양수: 녹색, 음수: 빨간색)으로 구분됩니다.

### Props
- `trades: Array<{ date: string; type: 'entry'|'exit'; pnl_pct?: number }>`

### 사용 예
```tsx
<TradesChart trades={backtestResult.trades} />
```

### 노트
- 내부에서 `ScatterChart`와 `Scatter`를 사용하며, exit 타입의 거래만 시각화하는 경우가 많습니다.

---

## StatsSummary

- 파일: `frontend/src/components/StatsSummary.tsx`
- 설명: 백테스트의 요약 통계(총 수익률, 거래수, 승률, 최대 손실, 샤프, Profit Factor 등)를 카드 그리드로 렌더링합니다.

### Props
- `stats: { total_return_pct: number; total_trades: number; win_rate_pct: number; max_drawdown_pct: number; sharpe_ratio: number; profit_factor: number; [key: string]: any }
`

### 사용 예
```tsx
<StatsSummary stats={backtestResult.summary_stats} />
```

### 노트
- `react-bootstrap`의 `Card`, `Badge`, `OverlayTrigger` 등을 사용합니다.
- 통계값에 따라 배지 색상을 바꿔 가독성을 높입니다.

---

## CustomTooltip

- 파일: `frontend/src/components/CustomTooltip.tsx`
- 설명: Recharts의 툴팁(custom) 렌더러로, `active`, `payload`, `label`을 받아 내부적으로 포맷해 보여줍니다.

### Props
- `active: boolean`
- `payload: any[]` (Recharts가 전달하는 데이터 배열)
- `label: string | number`

### 사용 예
```tsx
<RechartsTooltip content={<CustomTooltip />} />
```

### 노트
- payload의 각 항목에서 `dataKey`와 `value`를 포맷해 출력합니다.

---

## BacktestForm

- 파일: `frontend/src/components/BacktestForm.tsx`
- 설명: 백테스트 매개변수(티커, 기간, 초기 자금, 전략 등)를 입력받는 폼 컴포넌트입니다. 전략 선택 시 기본 파라미터를 자동으로 채우는 로직이 포함되어 있습니다.

### Props
- `backtestParams: any` — 현재 폼 상태 객체(예: `{ ticker, start_date, end_date, initial_cash, strategy, strategy_params }`)
- `setBacktestParams: (updater: any) => void` — 상태 업데이트 함수
- `runBacktest: () => void` — 제출 시 호출되는 실행 함수
- `loading: boolean` — 실행 중 로딩 표시 제어

### 사용 예
```tsx
<BacktestForm
	backtestParams={params}
	setBacktestParams={setParams}
	runBacktest={run}
	loading={isRunning}
/>
```

### 노트
- 내부적으로 `react-bootstrap`의 `Form`, `Row`, `Col`, `Button`, `Spinner` 등을 사용합니다.
- `strategy` 변경 시 `strategy_params`를 기본값으로 채우는 헬퍼 로직이 포함되어 있습니다.

---

추가 제안
- 문서에 각 컴포넌트의 prop 타입을 더 엄격히 표기하려면 프로젝트의 TypeScript 타입 정의(`frontend/src/types`)와 연동해 자동 생성하는 스크립트를 도입할 수 있습니다.
