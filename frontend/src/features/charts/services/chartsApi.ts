/**
 * 차트 및 시장 데이터 API 서비스
 */

import { httpClient } from '../../../shared/services/httpClient';
import { 
  ExchangeRateData,
  VolatilityData,
  NewsResponse 
} from '../../../types/api';

export class ChartsApiService {
  /**
   * 주식 데이터 조회
   */
  async getStockData(
    ticker: string, 
    startDate: string, 
    endDate: string,
    frequency: 'daily' | 'weekly' | 'monthly' = 'daily'
  ) {
    const params = new URLSearchParams({
      start_date: startDate,
      end_date: endDate,
      frequency,
    });

    return httpClient.get(`/api/v1/backtest/stock-data/${ticker}?${params}`);
  }

  /**
   * 환율 데이터 조회
   */
  async getExchangeRate(
    startDate: string, 
    endDate: string,
    baseCurrency: string = 'USD',
    targetCurrency: string = 'KRW'
  ): Promise<ExchangeRateData[]> {
    const params = new URLSearchParams({
      start_date: startDate,
      end_date: endDate,
      base: baseCurrency,
      target: targetCurrency,
    });

    return httpClient.get<ExchangeRateData[]>(`/api/v1/backtest/exchange-rate?${params}`);
  }

  /**
   * 주식 변동성 및 뉴스 데이터 조회
   */
  async getStockVolatilityNews(
    ticker: string, 
    startDate: string, 
    endDate: string, 
    threshold: number = 5.0
  ): Promise<VolatilityData[]> {
    const params = new URLSearchParams({
      start_date: startDate,
      end_date: endDate,
      threshold: threshold.toString(),
    });

    return httpClient.get<VolatilityData[]>(`/api/v1/backtest/stock-volatility-news/${ticker}?${params}`);
  }

  /**
   * 네이버 뉴스 검색 (일반)
   */
  async searchNews(
    query: string, 
    display: number = 15,
    sort: 'date' | 'sim' = 'date'
  ): Promise<NewsResponse> {
    const params = new URLSearchParams({
      query: query,
      display: display.toString(),
      sort,
    });

    return httpClient.get<NewsResponse>(`/api/v1/naver-news/search?${params}`);
  }

  /**
   * 특정 날짜의 종목 관련 뉴스 조회
   */
  async getTickerNews(
    ticker: string, 
    date: string, 
    display: number = 10
  ): Promise<NewsResponse> {
    const params = new URLSearchParams({
      start_date: date,
      end_date: date,
      display: display.toString(),
    });

    return httpClient.get<NewsResponse>(`/api/v1/naver-news/ticker/${ticker}/date?${params}`);
  }

  /**
   * 시장 지수 데이터 조회
   */
  async getMarketIndexes(
    indexes: string[] = ['SPY', 'QQQ', 'VTI'],
    startDate?: string,
    endDate?: string
  ) {
    const params = new URLSearchParams();
    indexes.forEach(index => params.append('symbols', index));
    
    if (startDate) params.append('start_date', startDate);
    if (endDate) params.append('end_date', endDate);

    return httpClient.get(`/api/v1/market/indexes?${params}`);
  }

  /**
   * 섹터 성과 데이터 조회
   */
  async getSectorPerformance(period: '1d' | '1w' | '1m' | '3m' | '1y' = '1d') {
    return httpClient.get(`/api/v1/market/sectors?period=${period}`);
  }

  /**
   * 기술적 지표 계산
   */
  async calculateTechnicalIndicators(
    ticker: string,
    indicators: Array<{
      name: string;
      parameters: Record<string, any>;
    }>,
    startDate?: string,
    endDate?: string
  ) {
    return httpClient.post('/api/v1/charts/indicators', {
      ticker,
      indicators,
      start_date: startDate,
      end_date: endDate,
    });
  }

  /**
   * 실시간 가격 데이터 조회
   */
  async getRealTimePrice(symbols: string[]) {
    const params = new URLSearchParams();
    symbols.forEach(symbol => params.append('symbols', symbol));

    return httpClient.get(`/api/v1/market/realtime?${params}`);
  }

  /**
   * 주식 기본 정보 조회
   */
  async getStockInfo(ticker: string) {
    return httpClient.get(`/api/v1/market/stocks/${ticker}/info`);
  }

  /**
   * 주식 재무 데이터 조회
   */
  async getFinancialData(
    ticker: string,
    statement: 'income' | 'balance' | 'cashflow' = 'income',
    period: 'annual' | 'quarterly' = 'annual'
  ) {
    const params = new URLSearchParams({
      statement,
      period,
    });
    
    return httpClient.get(`/api/v1/market/stocks/${ticker}/financials?${params}`);
  }

  /**
   * 시장 캘린더 데이터 조회
   */
  async getMarketCalendar(
    startDate?: string,
    endDate?: string,
    eventType?: 'earnings' | 'dividends' | 'splits' | 'all'
  ) {
    const params = new URLSearchParams();
    if (startDate) params.append('start_date', startDate);
    if (endDate) params.append('end_date', endDate);
    if (eventType && eventType !== 'all') params.append('type', eventType);

    return httpClient.get(`/api/v1/market/calendar?${params}`);
  }
}

// 싱글톤 인스턴스
export const chartsApiService = new ChartsApiService();