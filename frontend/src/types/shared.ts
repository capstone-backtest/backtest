/**
 * 공통 타입 정의
 * 여러 도메인에서 공통적으로 사용되는 타입들을 정의합니다.
 */

// 기본 엔터티 타입
export interface BaseEntity {
  id: string;
  createdAt: string;
  updatedAt: string;
}

// 페이지네이션 메타 정보
export interface PaginationMeta {
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
}

// 페이지네이션된 응답
export interface PaginatedResponse<T> {
  data: T[];
  meta: PaginationMeta;
}

// API 응답 래퍼
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: ApiError;
  message?: string;
  timestamp: string;
}

// API 에러 정보
export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, any>;
  stack?: string;
}

// 로딩 상태
export type LoadingState = 'idle' | 'loading' | 'success' | 'error';

// 정렬 옵션
export interface SortOption {
  field: string;
  direction: 'asc' | 'desc';
}

// 필터 옵션
export interface FilterOption {
  field: string;
  operator: 'eq' | 'ne' | 'gt' | 'gte' | 'lt' | 'lte' | 'like' | 'in' | 'nin';
  value: any;
}

// 검색 쿼리
export interface SearchQuery {
  q?: string;
  filters?: FilterOption[];
  sort?: SortOption[];
  pagination?: {
    page: number;
    pageSize: number;
  };
}

// 날짜 범위
export interface DateRange {
  startDate: string;
  endDate: string;
}

// 키-값 쌍
export interface KeyValuePair<T = any> {
  key: string;
  value: T;
  label?: string;
}

// 선택 옵션
export interface SelectOption<T = string> {
  value: T;
  label: string;
  disabled?: boolean;
  group?: string;
}

// 차트 데이터 포인트 (기본)
export interface ChartDataPoint {
  x: number | string | Date;
  y: number;
  label?: string;
}

// 시계열 데이터 포인트
export interface TimeSeriesDataPoint {
  timestamp: string;
  date: string;
  value: number;
}

// 가격 데이터 (OHLC)
export interface PriceDataPoint {
  timestamp: string;
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume?: number;
}

// 투자 금융 관련 공통 타입
export interface FinancialMetric {
  name: string;
  value: number;
  unit: string;
  description?: string;
  benchmark?: number;
}

// 통계 정보
export interface StatisticalSummary {
  mean: number;
  median: number;
  std: number;
  min: number;
  max: number;
  count: number;
  percentiles?: Record<string, number>;
}

// 성능 메트릭
export interface PerformanceMetrics {
  totalReturn: number;
  annualizedReturn: number;
  volatility: number;
  sharpeRatio: number;
  maxDrawdown: number;
  calmarRatio: number;
  sortinoRatio: number;
  winRate?: number;
  profitFactor?: number;
}

// 리스크 메트릭
export interface RiskMetrics {
  var95: number;  // Value at Risk (95%)
  cvar95: number; // Conditional Value at Risk (95%)
  beta: number;
  alpha: number;
  trackingError: number;
  informationRatio: number;
}

// 투자 전략 파라미터
export interface StrategyParameter {
  name: string;
  type: 'number' | 'string' | 'boolean' | 'select' | 'date' | 'range';
  value: any;
  defaultValue: any;
  min?: number;
  max?: number;
  step?: number;
  options?: SelectOption[];
  description?: string;
  required?: boolean;
}

// 알림/메시지 타입
export type NotificationType = 'info' | 'success' | 'warning' | 'error';

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  duration?: number;
  persistent?: boolean;
  actions?: Array<{
    label: string;
    action: () => void;
  }>;
}

// 테마 관련
export type ThemeMode = 'light' | 'dark' | 'system';

export interface ThemeConfig {
  mode: ThemeMode;
  primaryColor: string;
  accentColor: string;
  customProperties?: Record<string, string>;
}

// 사용자 선호 설정
export interface UserPreferences {
  theme: ThemeConfig;
  locale: string;
  timezone: string;
  notifications: {
    email: boolean;
    push: boolean;
    inApp: boolean;
  };
  dashboard: {
    defaultView: string;
    autoRefresh: boolean;
    refreshInterval: number;
  };
}

// HTTP 관련
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface HttpRequestConfig {
  method: HttpMethod;
  url: string;
  headers?: Record<string, string>;
  params?: Record<string, any>;
  data?: any;
  timeout?: number;
  retries?: number;
}

// 폼 관련
export interface FormField {
  name: string;
  label: string;
  type: string;
  value: any;
  error?: string;
  required?: boolean;
  disabled?: boolean;
  placeholder?: string;
  helpText?: string;
}

export interface FormState {
  fields: Record<string, FormField>;
  errors: Record<string, string>;
  isValid: boolean;
  isSubmitting: boolean;
  isDirty: boolean;
}

// 테이블 관련
export interface TableColumn<T = any> {
  key: string;
  title: string;
  dataIndex?: keyof T;
  width?: number;
  sortable?: boolean;
  filterable?: boolean;
  render?: (value: any, record: T, index: number) => React.ReactNode;
  align?: 'left' | 'center' | 'right';
}

export interface TableProps<T = any> {
  data: T[];
  columns: TableColumn<T>[];
  loading?: boolean;
  pagination?: PaginationMeta;
  onSort?: (field: string, direction: 'asc' | 'desc') => void;
  onFilter?: (filters: FilterOption[]) => void;
  onPageChange?: (page: number) => void;
}

// 유효성 검사
export interface ValidationRule {
  type: 'required' | 'min' | 'max' | 'pattern' | 'custom';
  value?: any;
  message: string;
  validator?: (value: any) => boolean;
}

export interface ValidationResult {
  isValid: boolean;
  errors: string[];
}

// 권한 관리
export type Permission = string;
export type Role = string;

export interface User {
  id: string;
  username: string;
  email: string;
  roles: Role[];
  permissions: Permission[];
  profile?: UserProfile;
  preferences?: UserPreferences;
}

export interface UserProfile {
  firstName: string;
  lastName: string;
  avatar?: string;
  bio?: string;
  location?: string;
  timezone?: string;
}