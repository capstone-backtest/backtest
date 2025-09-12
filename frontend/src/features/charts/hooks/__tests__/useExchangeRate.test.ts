import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useExchangeRate } from '../useExchangeRate';

// Create mock function first
const mockGetExchangeRate = vi.fn();

// Mock the charts service
vi.mock('../../services', () => ({
  chartsApiService: {
    getExchangeRate: mockGetExchangeRate,
  },
}));

describe('useExchangeRate Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize with default state', () => {
    const { result } = renderHook(() => 
      useExchangeRate({ 
        startDate: '2023-01-01', 
        endDate: '2023-12-31',
        enabled: false 
      })
    );

    expect(result.current.exchangeData).toEqual([]);
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('should fetch exchange rate data when enabled', async () => {
    const mockData = [
      { date: '2023-01-01', close: 1200 },
      { date: '2023-01-02', close: 1250 },
    ];

    mockGetExchangeRate.mockResolvedValue(mockData);

    const { result } = renderHook(() => 
      useExchangeRate({ 
        startDate: '2023-01-01', 
        endDate: '2023-12-31',
        enabled: true 
      })
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.exchangeData).toEqual(mockData);
    expect(result.current.error).toBeNull();
    expect(mockGetExchangeRate).toHaveBeenCalledWith('2023-01-01', '2023-12-31');
  });

  it('should handle API errors gracefully', async () => {
    const mockError = new Error('API Error');
    mockGetExchangeRate.mockRejectedValue(mockError);

    const { result } = renderHook(() => 
      useExchangeRate({ 
        startDate: '2023-01-01', 
        endDate: '2023-12-31',
        enabled: true 
      })
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.exchangeData).toEqual([]);
    expect(result.current.error).toBe('환율 데이터 조회에 실패했습니다.');
  });

  it('should not fetch when disabled', () => {
    renderHook(() => 
      useExchangeRate({ 
        startDate: '2023-01-01', 
        endDate: '2023-12-31',
        enabled: false 
      })
    );

    expect(mockGetExchangeRate).not.toHaveBeenCalled();
  });

  it('should not fetch when dates are missing', () => {
    renderHook(() => 
      useExchangeRate({ 
        startDate: '', 
        endDate: '2023-12-31',
        enabled: true 
      })
    );

    expect(mockGetExchangeRate).not.toHaveBeenCalled();
  });

  it('should refetch data when calling refetch', async () => {
    const mockData = [{ date: '2023-01-01', close: 1200 }];
    mockGetExchangeRate.mockResolvedValue(mockData);

    const { result } = renderHook(() => 
      useExchangeRate({ 
        startDate: '2023-01-01', 
        endDate: '2023-12-31',
        enabled: false 
      })
    );

    await waitFor(async () => {
      await result.current.refetch();
    });

    expect(mockGetExchangeRate).toHaveBeenCalledWith('2023-01-01', '2023-12-31');
  });
});