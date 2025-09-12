/**
 * 백테스팅 도메인 타입 정의
 */

import { BaseEntity, DateRange, PerformanceMetrics, RiskMetrics, StrategyParameter } from './shared';

// === 자산 및 포트폴리오 ===

export type AssetType = 'stock' | 'etf' | 'crypto' | 'bond' | 'commodity' | 'cash';
export type InvestmentType = 'lump_sum' | 'dca' | 'periodic';

export interface Asset {
  symbol: string;
  name?: string;
  assetType: AssetType;
  exchange?: string;
  currency?: string;
  sector?: string;
  industry?: string;
}

export interface PortfolioPosition {
  asset: Asset;
  amount: number;
  weight?: number;
  investmentType: InvestmentType;
  dcaPeriods?: number;
  allocation?: {
    target: number;
    current: number;
    deviation: number;
  };
}

export interface Portfolio {
  id?: string;
  name?: string;
  positions: PortfolioPosition[];
  totalValue: number;
  currency: string;
  rebalanceFrequency?: 'never' | 'daily' | 'weekly' | 'monthly' | 'quarterly' | 'annually';
  benchmarkSymbol?: string;
}

// === 백테스팅 설정 ===

export interface BacktestConfig {
  portfolio: Portfolio;
  dateRange: DateRange;
  strategy: BacktestStrategy;
  settings: BacktestSettings;
}

export interface BacktestStrategy {
  name: string;
  description?: string;
  parameters: Record<string, any>;
  category?: string;
}

export interface BacktestSettings {
  initialCash: number;
  commission: number;
  slippage?: number;
  borrowRate?: number;
  dividendHandling?: 'reinvest' | 'cash' | 'ignore';
  rebalanceFrequency: string;
  rebalanceThreshold?: number;
  benchmarkSymbol?: string;
}

// === 시장 데이터 ===

export interface MarketDataPoint {
  timestamp: string;
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  adjustedClose?: number;
  dividends?: number;
  splits?: number;
}

export interface PriceHistory {
  symbol: string;
  data: MarketDataPoint[];
  startDate: string;
  endDate: string;
  frequency: 'daily' | 'weekly' | 'monthly';
}

// === 거래 및 신호 ===

export type OrderSide = 'buy' | 'sell';
export type OrderType = 'market' | 'limit' | 'stop' | 'stop_limit';

export interface TradingSignal {
  timestamp: string;
  symbol: string;
  signal: 'buy' | 'sell' | 'hold';
  strength: number; // 0-1
  price: number;
  confidence?: number;
  reason?: string;
}

export interface Trade extends BaseEntity {
  symbol: string;
  side: OrderSide;
  type: OrderType;
  quantity: number;
  price: number;
  executionPrice: number;
  timestamp: string;
  commission: number;
  slippage?: number;
  pnl?: number;
  pnlPct?: number;
  tags?: string[];
}

export interface Position {
  symbol: string;
  quantity: number;
  avgPrice: number;
  currentPrice: number;
  marketValue: number;
  unrealizedPnl: number;
  unrealizedPnlPct: number;
  realizedPnl?: number;
  dayChange?: number;
  dayChangePct?: number;
}

// === 기술적 지표 ===

export interface TechnicalIndicator {
  name: string;
  type: 'overlay' | 'oscillator' | 'volume';
  parameters: Record<string, any>;
  data: Array<{
    timestamp: string;
    value: number | number[];
    signal?: 'buy' | 'sell' | 'neutral';
  }>;
  color?: string;
  style?: 'line' | 'histogram' | 'area';
}

// === 백테스트 결과 ===

export interface BacktestResult extends BaseEntity {
  config: BacktestConfig;
  performance: PerformanceMetrics;
  risk: RiskMetrics;
  trades: Trade[];
  positions: Position[];
  equityCurve: Array<{
    date: string;
    value: number;
    return: number;
    drawdown: number;
  }>;
  dailyReturns: Array<{
    date: string;
    return: number;
    benchmark?: number;
  }>;
  monthlyReturns: Record<string, number>;
  yearlyReturns: Record<string, number>;
  benchmarkComparison?: {
    symbol: string;
    performance: PerformanceMetrics;
    correlation: number;
    trackingError: number;
    informationRatio: number;
  };
  summary: BacktestSummary;
}

export interface BacktestSummary {
  startDate: string;
  endDate: string;
  duration: string;
  initialValue: number;
  finalValue: number;
  peakValue: number;
  totalReturn: number;
  annualizedReturn: number;
  volatility: number;
  sharpeRatio: number;
  maxDrawdown: number;
  maxDrawdownDuration: string;
  winRate: number;
  profitFactor: number;
  totalTrades: number;
  positiveDays: number;
  negativeDays: number;
  bestDay: number;
  worstDay: number;
  avgDailyReturn: number;
}

// === 최적화 ===

export interface OptimizationConfig {
  strategy: BacktestStrategy;
  dateRange: DateRange;
  parameterRanges: Record<string, {
    min: number;
    max: number;
    step: number;
    type: 'int' | 'float';
  }>;
  objective: 'total_return' | 'sharpe_ratio' | 'calmar_ratio' | 'sortino_ratio' | 'max_drawdown';
  constraints?: Array<{
    metric: string;
    operator: 'gt' | 'gte' | 'lt' | 'lte';
    value: number;
  }>;
}

export interface OptimizationResult {
  bestParameters: Record<string, any>;
  bestScore: number;
  results: Array<{
    parameters: Record<string, any>;
    score: number;
    metrics: PerformanceMetrics & RiskMetrics;
  }>;
  convergenceHistory: number[];
  totalIterations: number;
  executionTime: number;
}

// === 차트 데이터 ===

export interface ChartData {
  ohlc: MarketDataPoint[];
  equity: Array<{
    date: string;
    value: number;
    return: number;
    drawdown: number;
  }>;
  trades: Array<{
    date: string;
    price: number;
    side: OrderSide;
    size: number;
  }>;
  indicators: TechnicalIndicator[];
  volume?: Array<{
    date: string;
    volume: number;
    change: number;
  }>;
}

// === API 요청/응답 타입 ===

export interface BacktestRequest {
  portfolio: {
    positions: Array<{
      symbol: string;
      amount: number;
      investmentType?: InvestmentType;
      dcaPeriods?: number;
      assetType?: AssetType;
    }>;
  };
  startDate: string;
  endDate: string;
  strategy: string;
  strategyParams?: Record<string, any>;
  settings?: {
    commission?: number;
    rebalanceFrequency?: string;
    benchmarkSymbol?: string;
  };
}

export interface BacktestResponse {
  status: 'success' | 'error';
  backtestType: 'single_stock' | 'portfolio';
  data: BacktestResult;
  message?: string;
  executionTime?: number;
}

// === 전략 정의 ===

export interface StrategyDefinition {
  name: string;
  displayName: string;
  description: string;
  category: 'trend' | 'mean_reversion' | 'momentum' | 'arbitrage' | 'fundamental' | 'multi_factor';
  parameters: StrategyParameter[];
  requirements: {
    minDataPoints: number;
    requiredIndicators: string[];
    marketTypes: AssetType[];
  };
  riskLevel: 'low' | 'medium' | 'high';
  complexity: 'beginner' | 'intermediate' | 'advanced';
  author?: string;
  version?: string;
  lastUpdated?: string;
}

// === 벤치마크 및 비교 ===

export interface BenchmarkComparison {
  benchmark: {
    symbol: string;
    name: string;
    performance: PerformanceMetrics;
  };
  strategy: {
    name: string;
    performance: PerformanceMetrics;
  };
  comparison: {
    outperformance: number;
    correlation: number;
    beta: number;
    alpha: number;
    trackingError: number;
    informationRatio: number;
    upCaptureRatio: number;
    downCaptureRatio: number;
  };
}

// === 리포트 ===

export interface BacktestReport {
  id: string;
  title: string;
  summary: BacktestSummary;
  performance: PerformanceMetrics;
  risk: RiskMetrics;
  benchmark?: BenchmarkComparison;
  charts: {
    equity: string;      // Base64 encoded chart image
    drawdown: string;
    monthly: string;
    rolling: string;
  };
  generatedAt: string;
  format: 'pdf' | 'html' | 'json';
}