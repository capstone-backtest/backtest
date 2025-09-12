/**
 * 백테스트 API 서비스
 * 백테스트 관련 API 호출을 담당합니다.
 */

import { httpClient } from '../../../shared/services/httpClient';
import { 
  BacktestRequest,
  OptimizationRequest,
  OptimizationResult,
  UnifiedBacktestResponse 
} from '../../../types/api';

export class BacktestApiService {
  private readonly baseUrl = '/api/v1/backtest';

  /**
   * 통합 백테스트 실행 (권장)
   */
  async executeBacktest(request: BacktestRequest): Promise<UnifiedBacktestResponse> {
    return httpClient.post<UnifiedBacktestResponse>(`${this.baseUrl}/execute`, request);
  }

  /**
   * 단일 종목 백테스트 (레거시)
   */
  async runSingleStockBacktest(request: {
    ticker: string;
    startDate: string;
    endDate: string;
    initialCash: number;
    strategy: string;
    strategyParams?: Record<string, any>;
  }) {
    const requestBody = {
      ticker: request.ticker,
      start_date: request.startDate,
      end_date: request.endDate,
      initial_cash: request.initialCash,
      strategy: request.strategy,
      strategy_params: request.strategyParams || {},
    };

    return httpClient.post(`${this.baseUrl}/chart-data`, requestBody);
  }

  /**
   * 포트폴리오 백테스트 (레거시)
   */
  async runPortfolioBacktest(request: {
    portfolio: Array<{
      symbol: string;
      amount: number;
      weight?: number;
      investmentType?: string;
      dcaPeriods?: number;
      assetType?: string;
    }>;
    startDate: string;
    endDate: string;
    commission?: number;
    rebalanceFrequency?: string;
    strategy: string;
    strategyParams?: Record<string, any>;
  }) {
    const requestBody = {
      portfolio: request.portfolio,
      start_date: request.startDate,
      end_date: request.endDate,
      commission: request.commission || 0.002,
      rebalance_frequency: request.rebalanceFrequency || 'monthly',
      strategy: request.strategy,
      strategy_params: request.strategyParams || {},
    };

    return httpClient.post(`${this.baseUrl}/portfolio`, requestBody);
  }

  /**
   * 전략 목록 조회
   */
  async getStrategies() {
    return httpClient.get('/api/v1/strategies');
  }

  /**
   * 특정 전략 상세 정보 조회
   */
  async getStrategy(strategyName: string) {
    return httpClient.get(`/api/v1/strategies/${strategyName}`);
  }

  /**
   * 백테스트 최적화 실행
   */
  async runOptimization(request: OptimizationRequest): Promise<OptimizationResult> {
    return httpClient.post<OptimizationResult>('/api/v1/optimize/run', request);
  }

  /**
   * 백테스트 결과 내보내기
   */
  async exportResults(
    backtestId: string, 
    format: 'pdf' | 'excel' | 'csv' = 'pdf'
  ): Promise<Blob> {
    return httpClient.get(`${this.baseUrl}/${backtestId}/export`, {
      headers: {
        'Accept': format === 'pdf' ? 'application/pdf' : 
                 format === 'excel' ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' :
                 'text/csv'
      }
    });
  }

  /**
   * 저장된 백테스트 목록 조회
   */
  async getSavedBacktests(page: number = 1, pageSize: number = 20) {
    const params = new URLSearchParams({
      page: page.toString(),
      pageSize: pageSize.toString(),
    });
    
    return httpClient.get(`${this.baseUrl}/saved?${params}`);
  }

  /**
   * 백테스트 결과 저장
   */
  async saveBacktest(backtestData: any, name?: string) {
    return httpClient.post(`${this.baseUrl}/save`, {
      name: name || `Backtest ${new Date().toISOString()}`,
      data: backtestData,
    });
  }

  /**
   * 저장된 백테스트 삭제
   */
  async deleteBacktest(backtestId: string) {
    return httpClient.delete(`${this.baseUrl}/saved/${backtestId}`);
  }
}

// 싱글톤 인스턴스
export const backtestApiService = new BacktestApiService();