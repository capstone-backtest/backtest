import { describe, it, expect } from 'vitest';
import {
  validateEmail,
  validatePassword,
  validateStockSymbol,
  validateDateFormat,
  validateDateRange,
  validateNumberRange,
  validatePortfolioWeights,
  validateBacktestRequest,
  isEmpty,
  isNumeric,
} from '../validators';

describe('Validation Utils', () => {
  describe('validateEmail', () => {
    it('should validate correct email formats', () => {
      expect(validateEmail('user@example.com')).toBe(true);
      expect(validateEmail('test.email@domain.co.kr')).toBe(true);
      expect(validateEmail('user+tag@example.org')).toBe(true);
    });

    it('should reject invalid email formats', () => {
      expect(validateEmail('invalid-email')).toBe(false);
      expect(validateEmail('user@')).toBe(false);
      expect(validateEmail('@domain.com')).toBe(false);
      expect(validateEmail('')).toBe(false);
      expect(validateEmail('user name@domain.com')).toBe(false); // spaces are invalid
    });
  });

  describe('validatePassword', () => {
    it('should validate strong passwords', () => {
      expect(validatePassword('Password123')).toBe(true);
      expect(validatePassword('StrongP@ss1')).toBe(true);
      expect(validatePassword('MySecure123')).toBe(true);
    });

    it('should reject weak passwords', () => {
      expect(validatePassword('weak')).toBe(false); // too short
      expect(validatePassword('password')).toBe(false); // no uppercase/number
      expect(validatePassword('PASSWORD123')).toBe(false); // no lowercase
      expect(validatePassword('Password')).toBe(false); // no number
      expect(validatePassword('')).toBe(false); // empty
    });
  });

  describe('validateStockSymbol', () => {
    it('should validate correct stock symbols', () => {
      expect(validateStockSymbol('AAPL')).toBe(true);
      expect(validateStockSymbol('MSFT')).toBe(true);
      expect(validateStockSymbol('A')).toBe(true);
      expect(validateStockSymbol('GOOGL')).toBe(true);
      expect(validateStockSymbol('aapl')).toBe(true); // lowercase converted to uppercase
    });

    it('should reject invalid stock symbols', () => {
      expect(validateStockSymbol('TOOLONG')).toBe(false); // too long (7 chars)
      expect(validateStockSymbol('123')).toBe(false); // numbers only
      expect(validateStockSymbol('AA-PL')).toBe(false); // special characters
      expect(validateStockSymbol('')).toBe(false); // empty
      expect(validateStockSymbol('ABCDEF')).toBe(false); // 6 chars is too long
    });
  });

  describe('validateDateFormat', () => {
    it('should validate correct date formats', () => {
      expect(validateDateFormat('2023-12-31')).toBe(true);
      expect(validateDateFormat('2024-01-01')).toBe(true);
      expect(validateDateFormat('2024-02-29')).toBe(true); // leap year
    });

    it('should reject invalid date formats', () => {
      expect(validateDateFormat('12/31/2023')).toBe(false); // wrong format
      expect(validateDateFormat('2023-13-01')).toBe(false); // invalid month
      expect(validateDateFormat('2023-12-32')).toBe(false); // invalid day
      expect(validateDateFormat('')).toBe(false); // empty
      expect(validateDateFormat('invalid')).toBe(false);
    });
  });

  describe('validateDateRange', () => {
    it('should validate correct date ranges', () => {
      expect(validateDateRange('2023-01-01', '2023-12-31')).toBe(true);
      expect(validateDateRange('2024-01-01', '2024-01-02')).toBe(true);
    });

    it('should reject invalid date ranges', () => {
      expect(validateDateRange('2023-12-31', '2023-01-01')).toBe(false); // end before start
      expect(validateDateRange('2023-01-01', '2023-01-01')).toBe(false); // same dates
      expect(validateDateRange('invalid', '2023-01-01')).toBe(false); // invalid start
      expect(validateDateRange('2023-01-01', 'invalid')).toBe(false); // invalid end
    });
  });

  describe('validateNumberRange', () => {
    it('should validate numbers within range', () => {
      expect(validateNumberRange(5, 0, 10)).toBe(true);
      expect(validateNumberRange(0, 0, 10)).toBe(true); // boundary
      expect(validateNumberRange(10, 0, 10)).toBe(true); // boundary
    });

    it('should reject numbers outside range', () => {
      expect(validateNumberRange(-1, 0, 10)).toBe(false); // below min
      expect(validateNumberRange(11, 0, 10)).toBe(false); // above max
    });
  });

  describe('validatePortfolioWeights', () => {
    it('should validate weights that sum to 100%', () => {
      expect(validatePortfolioWeights([50, 30, 20])).toBe(true);
      expect(validatePortfolioWeights([100])).toBe(true);
      expect(validatePortfolioWeights([25, 25, 25, 25])).toBe(true);
    });

    it('should reject weights that do not sum to 100%', () => {
      expect(validatePortfolioWeights([50, 30, 10])).toBe(false); // sums to 90
      expect(validatePortfolioWeights([60, 40, 10])).toBe(false); // sums to 110
      expect(validatePortfolioWeights([])).toBe(false); // empty array
    });
  });

  describe('validateBacktestRequest', () => {
    const validRequest = {
      start_date: '2023-01-01',
      end_date: '2023-12-31',
      initial_capital: 10000,
      portfolio: [{ symbol: 'AAPL', weight: 100 }],
    };

    it('should validate correct backtest requests', () => {
      const result = validateBacktestRequest(validRequest);
      expect(result.isValid).toBe(true);
      expect(result.errors).toEqual([]);
    });

    it('should reject requests with invalid dates', () => {
      const invalidRequest = {
        ...validRequest,
        start_date: 'invalid-date',
      };
      const result = validateBacktestRequest(invalidRequest);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('유효한 시작 날짜를 입력해주세요');
    });

    it('should reject requests with invalid capital', () => {
      const invalidRequest = {
        ...validRequest,
        initial_capital: -1000,
      };
      const result = validateBacktestRequest(invalidRequest);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('초기 자본은 0보다 커야 합니다');
    });

    it('should reject requests with empty portfolio', () => {
      const invalidRequest = {
        ...validRequest,
        portfolio: [],
      };
      const result = validateBacktestRequest(invalidRequest);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('최소 하나의 자산이 필요합니다');
    });
  });

  describe('isEmpty', () => {
    it('should detect empty strings', () => {
      expect(isEmpty('')).toBe(true);
      expect(isEmpty('   ')).toBe(true); // whitespace only
      expect(isEmpty(null)).toBe(true);
      expect(isEmpty(undefined)).toBe(true);
    });

    it('should detect non-empty strings', () => {
      expect(isEmpty('hello')).toBe(false);
      expect(isEmpty('  hello  ')).toBe(false); // contains non-whitespace
      expect(isEmpty('0')).toBe(false); // zero is not empty
    });
  });

  describe('isNumeric', () => {
    it('should validate numeric strings', () => {
      expect(isNumeric('123')).toBe(true);
      expect(isNumeric('123.45')).toBe(true);
      expect(isNumeric('-123')).toBe(true);
      expect(isNumeric('0')).toBe(true);
    });

    it('should reject non-numeric strings', () => {
      expect(isNumeric('abc')).toBe(false);
      expect(isNumeric('12abc')).toBe(false);
      expect(isNumeric('')).toBe(false);
      expect(isNumeric('  ')).toBe(false);
    });
  });
});