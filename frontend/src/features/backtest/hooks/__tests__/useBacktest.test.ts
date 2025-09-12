import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useBacktest } from '../useBacktest';

// Create mock function first
const mockExecuteBacktest = vi.fn();

// Mock the backtest service
vi.mock('../../services', () => ({
  backtestApiService: {
    executeBacktest: mockExecuteBacktest,
  },
}));

describe('useBacktest Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize with default state', () => {
    const { result } = renderHook(() => useBacktest());

    expect(result.current.results).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(result.current.errorType).toBeNull();
    expect(result.current.errorId).toBeNull();
    expect(result.current.isPortfolio).toBe(false);
  });

  it('should handle successful backtest execution', async () => {
    const mockResults = {
      annual_return: 0.12,
      sharpe_ratio: 1.5,
      max_drawdown: -0.08,
      total_return: 0.25,
    };

    mockExecuteBacktest.mockResolvedValue(mockResults);

    const { result } = renderHook(() => useBacktest());

    const testRequest = {
      start_date: '2023-01-01',
      end_date: '2023-12-31',
      initial_capital: 10000,
      portfolio: [{ symbol: 'AAPL', weight: 100, asset_type: 'stock' }],
    };

    await act(async () => {
      await result.current.runBacktest(testRequest);
    });

    expect(result.current.loading).toBe(false);
    expect(result.current.results).toEqual(mockResults);
    expect(result.current.error).toBeNull();
    expect(mockExecuteBacktest).toHaveBeenCalledWith(testRequest);
  });

  it('should handle backtest execution errors', async () => {
    const mockError = new Error('Network error');
    mockExecuteBacktest.mockRejectedValue(mockError);

    const { result } = renderHook(() => useBacktest());

    const testRequest = {
      start_date: '2023-01-01',
      end_date: '2023-12-31',
      initial_capital: 10000,
      portfolio: [{ symbol: 'AAPL', weight: 100, asset_type: 'stock' }],
    };

    await act(async () => {
      await result.current.runBacktest(testRequest);
    });

    expect(result.current.loading).toBe(false);
    expect(result.current.results).toBeNull();
    expect(result.current.error).toBe('백테스트 실행에 실패했습니다.');
  });

  it('should detect portfolio composition correctly', async () => {
    const mockResults = { annual_return: 0.12 };
    mockExecuteBacktest.mockResolvedValue(mockResults);

    const { result } = renderHook(() => useBacktest());

    // Multi-asset portfolio
    const portfolioRequest = {
      start_date: '2023-01-01',
      end_date: '2023-12-31',
      initial_capital: 10000,
      portfolio: [
        { symbol: 'AAPL', weight: 50, asset_type: 'stock' },
        { symbol: 'GOOGL', weight: 50, asset_type: 'stock' },
      ],
    };

    await act(async () => {
      await result.current.runBacktest(portfolioRequest);
    });

    expect(result.current.isPortfolio).toBe(true);
  });

  it('should set loading state during execution', async () => {
    let resolvePromise: (value: any) => void;
    const promise = new Promise((resolve) => {
      resolvePromise = resolve;
    });
    mockExecuteBacktest.mockReturnValue(promise);

    const { result } = renderHook(() => useBacktest());

    const testRequest = {
      start_date: '2023-01-01',
      end_date: '2023-12-31',
      initial_capital: 10000,
      portfolio: [{ symbol: 'AAPL', weight: 100, asset_type: 'stock' }],
    };

    act(() => {
      result.current.runBacktest(testRequest);
    });

    expect(result.current.loading).toBe(true);

    await act(async () => {
      resolvePromise!({ annual_return: 0.12 });
    });

    expect(result.current.loading).toBe(false);
  });
});