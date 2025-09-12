/**
 * 유효성 검사 유틸리티 함수들
 */

/**
 * 이메일 유효성 검사
 */
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * 비밀번호 유효성 검사 (최소 8자, 대소문자, 숫자 포함)
 */
export const validatePassword = (password: string): boolean => {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/;
  return passwordRegex.test(password);
};

/**
 * 주식 심볼 유효성 검사
 */
export const validateStockSymbol = (symbol: string): boolean => {
  const symbolRegex = /^[A-Z]{1,5}$/;
  return symbolRegex.test(symbol.toUpperCase());
};

/**
 * 날짜 형식 유효성 검사 (YYYY-MM-DD)
 */
export const validateDateFormat = (dateStr: string): boolean => {
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
  if (!dateRegex.test(dateStr)) return false;
  
  const date = new Date(dateStr);
  return date instanceof Date && !isNaN(date.getTime());
};

/**
 * 날짜 범위 유효성 검사
 */
export const validateDateRange = (startDate: string, endDate: string): boolean => {
  if (!validateDateFormat(startDate) || !validateDateFormat(endDate)) return false;
  
  const start = new Date(startDate);
  const end = new Date(endDate);
  
  return start < end;
};

/**
 * 숫자 범위 유효성 검사
 */
export const validateNumberRange = (value: number, min: number, max: number): boolean => {
  return value >= min && value <= max;
};

/**
 * 포트폴리오 가중치 유효성 검사 (총합 100%)
 */
export const validatePortfolioWeights = (weights: number[]): boolean => {
  const total = weights.reduce((sum, weight) => sum + weight, 0);
  return Math.abs(total - 100) < 0.01; // 소수점 오차 허용
};

/**
 * 백테스트 요청 데이터 유효성 검사
 */
export interface BacktestValidationResult {
  isValid: boolean;
  errors: string[];
}

export const validateBacktestRequest = (data: any): BacktestValidationResult => {
  const errors: string[] = [];

  if (!data.start_date || !validateDateFormat(data.start_date)) {
    errors.push('유효한 시작 날짜를 입력해주세요');
  }

  if (!data.end_date || !validateDateFormat(data.end_date)) {
    errors.push('유효한 종료 날짜를 입력해주세요');
  }

  if (data.start_date && data.end_date && !validateDateRange(data.start_date, data.end_date)) {
    errors.push('종료 날짜는 시작 날짜보다 늦어야 합니다');
  }

  if (!data.initial_capital || data.initial_capital <= 0) {
    errors.push('초기 자본은 0보다 커야 합니다');
  }

  if (!data.portfolio || !Array.isArray(data.portfolio) || data.portfolio.length === 0) {
    errors.push('최소 하나의 자산이 필요합니다');
  }

  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * 문자열이 빈 값인지 검사
 */
export const isEmpty = (str: string | null | undefined): boolean => {
  return !str || str.trim().length === 0;
};

/**
 * 숫자 문자열 유효성 검사
 */
export const isNumeric = (str: string): boolean => {
  return !isNaN(Number(str)) && !isNaN(parseFloat(str));
};