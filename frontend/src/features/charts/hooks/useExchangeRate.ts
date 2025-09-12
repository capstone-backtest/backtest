import { useState, useEffect } from 'react';
import { chartsApiService } from '../services';
import type { ExchangeRateData } from '../../../types';

interface UseExchangeRateParams {
  startDate: string;
  endDate: string;
  enabled?: boolean;
}

interface UseExchangeRateReturn {
  exchangeData: ExchangeRateData[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

/**
 * 환율 데이터 페칭을 위한 커스텀 훅
 * USD/KRW 환율 데이터를 가져오고 캐싱 기능 제공
 */
export const useExchangeRate = ({ 
  startDate, 
  endDate, 
  enabled = true 
}: UseExchangeRateParams): UseExchangeRateReturn => {
  const [exchangeData, setExchangeData] = useState<ExchangeRateData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchExchangeRate = async () => {
    if (!enabled || !startDate || !endDate) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const exchangeRates = await chartsApiService.getExchangeRate(startDate, endDate);
      setExchangeData(exchangeRates);
    } catch (err) {
      console.error('환율 데이터 조회 실패:', err);
      setError('환율 데이터 조회에 실패했습니다.');
      setExchangeData([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExchangeRate();
  }, [startDate, endDate, enabled]);

  return {
    exchangeData,
    loading,
    error,
    refetch: fetchExchangeRate
  };
};
