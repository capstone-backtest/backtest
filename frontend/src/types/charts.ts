/**
 * 차트 및 시각화 도메인 타입 정의
 */

import { TechnicalIndicator } from './backtest';
import { TimeSeriesDataPoint } from './shared';

// === 기본 차트 타입 ===

export type ChartType = 
  | 'line'
  | 'area'
  | 'bar'
  | 'candlestick'
  | 'ohlc'
  | 'scatter'
  | 'pie'
  | 'donut'
  | 'histogram'
  | 'heatmap'
  | 'treemap';

export interface ChartConfig {
  type: ChartType;
  title?: string;
  subtitle?: string;
  width?: number;
  height?: number;
  responsive?: boolean;
  animation?: boolean;
  theme?: ChartTheme;
  backgroundColor?: string;
}

export interface ChartTheme {
  name: string;
  colors: {
    primary: string;
    secondary: string;
    accent: string;
    background: string;
    text: string;
    grid: string;
    axis: string;
    positive: string;
    negative: string;
    neutral: string;
  };
  fonts: {
    title: ChartFont;
    subtitle: ChartFont;
    axis: ChartFont;
    legend: ChartFont;
    tooltip: ChartFont;
  };
}

export interface ChartFont {
  family: string;
  size: number;
  weight: 'normal' | 'bold' | 'lighter' | number;
  color?: string;
}

// === 축 설정 ===

export interface ChartAxis {
  type: 'category' | 'value' | 'time' | 'log';
  position: 'left' | 'right' | 'top' | 'bottom';
  title?: string;
  min?: number;
  max?: number;
  tickInterval?: number;
  tickFormatter?: (value: any) => string;
  gridLines?: boolean;
  axisBorder?: boolean;
  labels?: {
    show: boolean;
    rotate?: number;
    formatter?: (value: any) => string;
  };
}

// === 시리즈 데이터 ===

export interface ChartSeries<T = any> {
  id: string;
  name: string;
  type: ChartType;
  data: T[];
  color?: string;
  yAxisIndex?: number;
  smooth?: boolean;
  symbol?: 'circle' | 'rect' | 'triangle' | 'diamond' | 'none';
  symbolSize?: number;
  lineWidth?: number;
  opacity?: number;
  stack?: string;
  area?: boolean;
}

// === 라인/영역 차트 ===

export interface LineChartDataPoint {
  x: number | string | Date;
  y: number;
  label?: string;
  metadata?: Record<string, any>;
}

export interface AreaChartDataPoint extends LineChartDataPoint {
  y0?: number; // 영역의 시작점
  y1?: number; // 영역의 끝점
}

// === 캔들스틱/OHLC 차트 ===

export interface CandlestickDataPoint {
  timestamp: string;
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume?: number;
  change?: number;
  changePct?: number;
}

export interface OHLCChartData {
  ohlc: CandlestickDataPoint[];
  volume?: VolumeDataPoint[];
  indicators?: TechnicalIndicator[];
  trades?: TradeMarker[];
  annotations?: ChartAnnotation[];
}

export interface VolumeDataPoint {
  timestamp: string;
  date: string;
  volume: number;
  change?: number;
  color?: 'positive' | 'negative' | 'neutral';
}

export interface TradeMarker {
  timestamp: string;
  date: string;
  price: number;
  type: 'buy' | 'sell';
  size: number;
  label?: string;
  color?: string;
  symbol?: string;
}

// === 히트맵 ===

export interface HeatmapDataPoint {
  x: string | number;
  y: string | number;
  value: number;
  color?: string;
  label?: string;
}

export interface HeatmapConfig extends ChartConfig {
  colorScale?: {
    min: string;
    max: string;
    steps?: number;
  };
  showValues?: boolean;
}

// === 파이/도넛 차트 ===

export interface PieChartDataPoint {
  name: string;
  value: number;
  percentage?: number;
  color?: string;
  label?: string;
}

export interface PieChartConfig extends ChartConfig {
  innerRadius?: number;
  outerRadius?: number;
  startAngle?: number;
  endAngle?: number;
  showLabels?: boolean;
  showPercentages?: boolean;
}

// === 주식 차트 특화 ===

export interface StockChartData {
  symbol: string;
  name?: string;
  exchange?: string;
  currency?: string;
  timeframe: '1m' | '5m' | '15m' | '1h' | '4h' | '1d' | '1w' | '1M';
  data: CandlestickDataPoint[];
  volume?: VolumeDataPoint[];
  indicators?: TechnicalIndicator[];
  events?: StockEvent[];
}

export interface StockEvent {
  timestamp: string;
  date: string;
  type: 'dividend' | 'split' | 'earnings' | 'news' | 'announcement';
  title: string;
  description?: string;
  value?: number;
  url?: string;
}

// === 포트폴리오 차트 ===

export interface PortfolioChartData {
  equity: TimeSeriesDataPoint[];
  benchmark?: TimeSeriesDataPoint[];
  drawdown: TimeSeriesDataPoint[];
  allocation: Array<{
    date: string;
    positions: Array<{
      symbol: string;
      weight: number;
      value: number;
    }>;
  }>;
  rebalances?: Array<{
    date: string;
    trades: Array<{
      symbol: string;
      action: 'buy' | 'sell';
      quantity: number;
      price: number;
    }>;
  }>;
}

// === 성능 차트 ===

export interface PerformanceChartData {
  returns: {
    daily: TimeSeriesDataPoint[];
    monthly: TimeSeriesDataPoint[];
    yearly: TimeSeriesDataPoint[];
  };
  rollingMetrics: {
    sharpe: TimeSeriesDataPoint[];
    volatility: TimeSeriesDataPoint[];
    maxDrawdown: TimeSeriesDataPoint[];
  };
  distribution: {
    returns: Array<{
      range: string;
      frequency: number;
      percentage: number;
    }>;
    drawdowns: Array<{
      duration: number;
      magnitude: number;
      startDate: string;
      endDate: string;
    }>;
  };
}

// === 차트 상호작용 ===

export interface ChartInteraction {
  zoom?: {
    enabled: boolean;
    type: 'x' | 'y' | 'xy';
    resetButton?: boolean;
  };
  pan?: {
    enabled: boolean;
    type: 'x' | 'y' | 'xy';
  };
  crosshair?: {
    enabled: boolean;
    snap: boolean;
  };
  tooltip?: {
    enabled: boolean;
    shared: boolean;
    formatter?: (points: any[]) => string;
  };
  legend?: {
    enabled: boolean;
    position: 'top' | 'bottom' | 'left' | 'right';
    clickable: boolean;
  };
  dataLabels?: {
    enabled: boolean;
    formatter?: (value: any) => string;
  };
}

// === 차트 주석 ===

export interface ChartAnnotation {
  id: string;
  type: 'line' | 'rect' | 'circle' | 'text' | 'arrow';
  x?: number | string;
  y?: number;
  x2?: number | string;
  y2?: number;
  text?: string;
  color?: string;
  style?: {
    strokeWidth?: number;
    strokeDashArray?: string;
    fill?: string;
    opacity?: number;
  };
  label?: {
    text: string;
    position: 'start' | 'middle' | 'end';
    backgroundColor?: string;
    borderColor?: string;
  };
}

// === 차트 내보내기 ===

export interface ChartExportOptions {
  format: 'png' | 'jpeg' | 'svg' | 'pdf';
  width?: number;
  height?: number;
  quality?: number;
  filename?: string;
  background?: string;
}

// === 차트 컴포넌트 Props ===

export interface BaseChartProps {
  config?: ChartConfig;
  interaction?: ChartInteraction;
  loading?: boolean;
  error?: string;
  onExport?: (options: ChartExportOptions) => void;
  onDataPointClick?: (point: any, series: ChartSeries) => void;
  onZoom?: (range: { min: number; max: number }) => void;
  className?: string;
  style?: React.CSSProperties;
}

export interface LineChartProps extends BaseChartProps {
  data: LineChartDataPoint[];
  series?: ChartSeries<LineChartDataPoint>[];
  xAxis?: ChartAxis;
  yAxis?: ChartAxis;
}

export interface CandlestickChartProps extends BaseChartProps {
  data: OHLCChartData;
  timeframe?: '1m' | '5m' | '15m' | '1h' | '4h' | '1d' | '1w' | '1M';
  showVolume?: boolean;
  showIndicators?: boolean;
  showTrades?: boolean;
}

export interface PortfolioChartProps extends BaseChartProps {
  data: PortfolioChartData;
  showBenchmark?: boolean;
  showDrawdown?: boolean;
  showAllocation?: boolean;
}

export interface PerformanceChartProps extends BaseChartProps {
  data: PerformanceChartData;
  period: 'daily' | 'monthly' | 'yearly';
  metrics?: ('returns' | 'sharpe' | 'volatility' | 'maxDrawdown')[];
}

// === 차트 유틸리티 ===

export interface ChartDataProcessor {
  resample: (data: TimeSeriesDataPoint[], frequency: string) => TimeSeriesDataPoint[];
  aggregate: (data: TimeSeriesDataPoint[], method: 'sum' | 'avg' | 'min' | 'max') => number;
  normalize: (data: TimeSeriesDataPoint[], base?: number) => TimeSeriesDataPoint[];
  calculateReturns: (data: TimeSeriesDataPoint[], type: 'simple' | 'log') => TimeSeriesDataPoint[];
  calculateMovingAverage: (data: TimeSeriesDataPoint[], window: number) => TimeSeriesDataPoint[];
}

export interface ChartColorPalette {
  name: string;
  colors: string[];
  generateColor: (index: number) => string;
  getColorByValue: (value: number, min: number, max: number) => string;
}