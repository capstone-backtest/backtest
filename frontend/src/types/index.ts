/**
 * 통합 타입 내보내기
 * 모든 도메인 타입을 중앙에서 관리하고 내보냅니다.
 */

// === 공통 타입 ===
export * from './shared';

// === 도메인별 타입 (명시적 내보내기로 충돌 방지) ===
export type {
  // auth 도메인 (shared와 충돌하는 것들은 별칭 사용)
  User as AuthUser,
  UserProfile as AuthUserProfile,
  Role as AuthRole,
  Permission as AuthPermission,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  AuthContextValue,
} from './auth';

export type {
  // backtest 도메인
  Asset,
  Portfolio,
  PortfolioPosition,
  BacktestConfig,
  BacktestStrategy,
  BacktestSettings,
  BacktestResult,
  BacktestSummary,
  OptimizationConfig,
  TechnicalIndicator,
  Trade,
  Position,
} from './backtest';

export type {
  // charts 도메인
  ChartType,
  ChartConfig,
  ChartTheme,
  ChartSeries,
  CandlestickDataPoint,
  OHLCChartData,
  LineChartProps,
  CandlestickChartProps,
} from './charts';

export type {
  // community 도메인 (shared와 충돌하는 것들은 별칭 사용)
  Post,
  Comment,
  Like,
  Bookmark,
  Tag,
  Follow,
  Notification as CommunityNotification,
  SearchResult,
  CommunityProfile,
  Feed,
  FeedItem,
} from './community';

// === 기존 호환성 유지 (레거시 타입들) ===
export type {
  // API 타입 (기존과 겹치지 않는 것만)
  PortfolioStock,
  BacktestStats,
  PortfolioStats,
  ChartDataResponse,
  PortfolioBacktestResponse,
  Strategy,
  NewsResponse,
  VolatilityData,
  ExchangeRateData,
  SystemInfo,
  OptimizationRequest,
  UnifiedBacktestResponse,
  UnifiedBacktestResult,
  ApiEndpoint,
} from './api';

export type {
  // 백테스트 폼 타입
  Stock as FormStock,
  PortfolioInputMode,
  BacktestFormState,
  BacktestFormAction,
} from './backtest-form';

export type {
  // 백테스트 결과 타입 (레거시)
  IndividualReturn,
  PortfolioStatistics,
  ChartData as LegacyChartData,
  PortfolioData,
  BacktestResultsProps,
  StockDataItem,
  EquityChartDataItem,
} from './backtest-results';

export * from './theme';

export type {
  NewsItem as VolatilityNewsItem,
} from './volatility-news';

// === 타입 가드 함수 ===

// 백테스트 결과 타입 가드
export function isChartDataResponse(data: any): data is import('./api').ChartDataResponse {
  return data && typeof data === 'object' && 'ticker' in data && 'ohlc_data' in data;
}

export function isPortfolioBacktestResponse(data: any): data is import('./api').PortfolioBacktestResponse {
  return data && typeof data === 'object' && 'portfolio_composition' in data && 'chart_data' in data;
}

// 사용자 타입 가드
export function isUser(obj: any): obj is import('./auth').User {
  return obj && typeof obj === 'object' && 'id' in obj && 'username' in obj && 'email' in obj;
}

// 에러 타입 가드
export function isApiError(obj: any): obj is import('./shared').ApiError {
  return obj && typeof obj === 'object' && 'code' in obj && 'message' in obj;
}

// === 유틸리티 타입 ===

// 부분적 업데이트를 위한 타입
export type PartialUpdate<T extends { id: string }> = Partial<T> & Pick<T, 'id'>;

// 생성 요청을 위한 타입 (id, createdAt, updatedAt 제외)
export type CreateRequest<T extends import('./shared').BaseEntity> = Omit<T, 'id' | 'createdAt' | 'updatedAt'>;

// 업데이트 요청을 위한 타입 (createdAt, updatedAt 제외)
export type UpdateRequest<T extends import('./shared').BaseEntity> = Partial<Omit<T, 'createdAt' | 'updatedAt'>> & Pick<T, 'id'>;

// API 응답에서 데이터만 추출하는 타입
export type ExtractData<T> = T extends import('./shared').ApiResponse<infer U> ? U : T;

// 폼 상태를 위한 타입
export type FormState<T> = {
  [K in keyof T]: {
    value: T[K];
    error?: string;
    touched: boolean;
    dirty: boolean;
  };
} & {
  isValid: boolean;
  isSubmitting: boolean;
  isDirty: boolean;
  errors: Record<keyof T, string>;
};

// 테이블 행을 위한 타입
export type TableRow<T> = T & {
  _selected?: boolean;
  _expanded?: boolean;
  _loading?: boolean;
};

// 차트 시리즈 데이터를 위한 타입
export type ChartSeriesData<T> = {
  name: string;
  data: T[];
  color?: string;
  type?: import('./charts').ChartType;
};

// === 열거형 상수 ===

export const USER_ROLES = {
  ADMIN: 'admin',
  MODERATOR: 'moderator',
  USER: 'user',
  GUEST: 'guest',
} as const;

export const POST_STATUS = {
  DRAFT: 'draft',
  PUBLISHED: 'published',
  ARCHIVED: 'archived',
  DELETED: 'deleted',
} as const;

export const ASSET_TYPES = {
  STOCK: 'stock',
  ETF: 'etf',
  CRYPTO: 'crypto',
  BOND: 'bond',
  COMMODITY: 'commodity',
  CASH: 'cash',
} as const;

export const CHART_TYPES = {
  LINE: 'line',
  AREA: 'area',
  BAR: 'bar',
  CANDLESTICK: 'candlestick',
  OHLC: 'ohlc',
  SCATTER: 'scatter',
  PIE: 'pie',
  DONUT: 'donut',
  HISTOGRAM: 'histogram',
  HEATMAP: 'heatmap',
  TREEMAP: 'treemap',
} as const;

export const LOADING_STATES = {
  IDLE: 'idle',
  LOADING: 'loading',
  SUCCESS: 'success',
  ERROR: 'error',
} as const;

// === 타입 유틸리티 ===

// 객체의 키를 타입으로 변환
export type KeysOf<T> = keyof T;

// 객체의 값을 타입으로 변환  
export type ValuesOf<T> = T[keyof T];

// 중첩된 객체의 키 경로 (최대 3단계 깊이로 제한)
export type DeepKeyOf<T, Depth extends ReadonlyArray<unknown> = []> = Depth['length'] extends 3
  ? never
  : {
      [K in keyof T]: T[K] extends object 
        ? `${K & string}` | `${K & string}.${DeepKeyOf<T[K], [...Depth, unknown]> & string}`
        : `${K & string}`;
    }[keyof T];

// 조건부 타입
export type ConditionalType<T, U, V> = T extends U ? V : never;

// 배열에서 요소 타입 추출
export type ArrayElement<T> = T extends readonly (infer U)[] ? U : never;

// Promise에서 결과 타입 추출
export type PromiseResult<T> = T extends Promise<infer U> ? U : T;