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
*   `indicators`: 기술 지표 데이터 배열입니다.
*   `trades`: 거래 마커 배열입니다.

### 사용법

```tsx
<OHLCChart data={chartData.ohlc_data} indicators={chartData.indicators} trades={chartData.trade_markers} />
```

### 구현 세부 정보

*   `recharts`의 `ComposedChart`를 사용하여 막대 차트(거래량용)와 선 차트(종가용)를 결합합니다.
*   기술 지표는 차트에 추가 선으로 렌더링됩니다.
*   거래 마커는 차트에 참조 점으로 렌더링됩니다.

## `EquityChart`

이 컴포넌트는 자산 곡선 및 손실률 차트를 렌더링합니다.

### Props

*   `data`: 자산 데이터 포인트 배열입니다.

### 사용법

```tsx
<EquityChart data={chartData.equity_data} />
```

### 구현 세부 정보

*   `recharts`의 `ComposedChart`를 사용하여 선 차트(자산 곡선용)와 영역 차트(손실률용)를 결합합니다.
*   y=0의 참조선은 손익분기점을 나타냅니다.

## `TradesChart`

이 컴포넌트는 개별 거래의 산점도를 렌더링합니다.

### Props

*   `data`: 거래 데이터 배열입니다.

### 사용법

```tsx
<TradesChart data={backtestResult.trades} />
```

### 구현 세부 정보

*   `recharts`의 `ScatterChart`를 사용하여 각 거래의 손익(PnL)을 표시합니다.
*   각 점의 색상은 거래의 손익에 따라 결정됩니다(이익은 녹색, 손실은 빨간색).