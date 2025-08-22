"""
포트폴리오 백테스트 서비스
"""
import pandas as pd
import numpy as np
from typing import List, Dict, Any, Tuple
from datetime import datetime, timedelta
import logging

from app.models.schemas import PortfolioBacktestRequest, PortfolioWeight
from app.models.requests import BacktestRequest
from app.services.yfinance_db import load_ticker_data
from app.services.backtest_service import backtest_service
from app.utils.serializers import recursive_serialize

logger = logging.getLogger(__name__)


class PortfolioBacktestService:
    """포트폴리오 백테스트 서비스"""
    
    @staticmethod
    def calculate_portfolio_returns(
        portfolio_data: Dict[str, pd.DataFrame],
        weights: Dict[str, float],
        rebalance_frequency: str = "monthly"
    ) -> pd.DataFrame:
        """
        포트폴리오 수익률을 계산합니다.
        
        Args:
            portfolio_data: 각 종목의 가격 데이터 {symbol: DataFrame}
            weights: 각 종목의 가중치 {symbol: weight}
            rebalance_frequency: 리밸런싱 주기
            
        Returns:
            포트폴리오 가치와 수익률이 포함된 DataFrame
        """
        # 모든 종목의 날짜 범위를 통합
        all_dates = set()
        for df in portfolio_data.values():
            all_dates.update(df.index)
        
        date_range = pd.DatetimeIndex(sorted(all_dates))
        
        # 각 종목의 수익률 계산
        returns_data = {}
        for symbol, df in portfolio_data.items():
            if len(df) == 0:
                continue
            # 종목별 일일 수익률 계산
            daily_returns = df['Close'].pct_change().fillna(0)
            returns_data[symbol] = daily_returns.reindex(date_range, fill_value=0)
        
        if not returns_data:
            raise ValueError("유효한 데이터가 없습니다.")
        
        # 포트폴리오 수익률 계산
        portfolio_returns = pd.Series(0.0, index=date_range)
        
        for symbol, weight in weights.items():
            if symbol in returns_data:
                portfolio_returns += returns_data[symbol] * weight
        
        # 누적 가치 계산 (1부터 시작)
        portfolio_value = (1 + portfolio_returns).cumprod()
        
        # 결과 DataFrame 생성
        result = pd.DataFrame({
            'Date': date_range,
            'Portfolio_Value': portfolio_value,
            'Daily_Return': portfolio_returns,
            'Cumulative_Return': (portfolio_value - 1) * 100
        })
        result.set_index('Date', inplace=True)
        
        return result
    
    @staticmethod
    def calculate_portfolio_statistics(portfolio_data: pd.DataFrame, initial_cash: float) -> Dict[str, Any]:
        """
        포트폴리오 통계 계산
        
        Args:
            portfolio_data: 포트폴리오 가치 데이터
            initial_cash: 초기 자본금
            
        Returns:
            포트폴리오 통계 딕셔너리
        """
        if len(portfolio_data) == 0:
            raise ValueError("포트폴리오 데이터가 없습니다.")
        
        # 기본 통계
        start_date = portfolio_data.index[0]
        end_date = portfolio_data.index[-1]
        duration = (end_date - start_date).days
        
        final_value = portfolio_data['Portfolio_Value'].iloc[-1]
        peak_value = portfolio_data['Portfolio_Value'].max()
        
        total_return = (final_value - 1) * 100
        
        # 드로우다운 계산
        running_max = portfolio_data['Portfolio_Value'].expanding().max()
        drawdown = (portfolio_data['Portfolio_Value'] - running_max) / running_max * 100
        max_drawdown = drawdown.min()
        avg_drawdown = drawdown[drawdown < 0].mean() if len(drawdown[drawdown < 0]) > 0 else 0
        
        # 변동성 및 샤프 비율
        daily_returns = portfolio_data['Daily_Return']
        annual_volatility = daily_returns.std() * np.sqrt(252) * 100
        annual_return = ((final_value ** (365.25 / duration)) - 1) * 100 if duration > 0 else 0
        
        # 무위험 수익률을 0으로 가정한 샤프 비율
        sharpe_ratio = (annual_return / annual_volatility) if annual_volatility > 0 else 0
        
        # 최대 연속 상승/하락일
        daily_changes = daily_returns > 0
        consecutive_gains = PortfolioBacktestService._get_max_consecutive(daily_changes, True)
        consecutive_losses = PortfolioBacktestService._get_max_consecutive(daily_changes, False)
        
        return {
            'Start': start_date.strftime('%Y-%m-%d'),
            'End': end_date.strftime('%Y-%m-%d'),
            'Duration': f'{duration} days',
            'Initial_Value': initial_cash,
            'Final_Value': final_value * initial_cash,
            'Peak_Value': peak_value * initial_cash,
            'Total_Return': total_return,
            'Annual_Return': annual_return,
            'Annual_Volatility': annual_volatility,
            'Sharpe_Ratio': sharpe_ratio,
            'Max_Drawdown': max_drawdown,
            'Avg_Drawdown': avg_drawdown,
            'Max_Consecutive_Gains': consecutive_gains,
            'Max_Consecutive_Losses': consecutive_losses,
            'Total_Trading_Days': len(portfolio_data),
            'Positive_Days': len(daily_returns[daily_returns > 0]),
            'Negative_Days': len(daily_returns[daily_returns < 0]),
            'Win_Rate': len(daily_returns[daily_returns > 0]) / len(daily_returns) * 100 if len(daily_returns) > 0 else 0
        }
    
    @staticmethod
    def _get_max_consecutive(series: pd.Series, target_value: bool) -> int:
        """연속된 값의 최대 길이 계산"""
        max_count = 0
        current_count = 0
        
        for value in series:
            if value == target_value:
                current_count += 1
                max_count = max(max_count, current_count)
            else:
                current_count = 0
        
        return max_count
    
    async def run_portfolio_backtest(self, request: PortfolioBacktestRequest) -> Dict[str, Any]:
        """
        포트폴리오 백테스트 실행
        
        Args:
            request: 포트폴리오 백테스트 요청
            
        Returns:
            백테스트 결과
        """
        try:
            logger.info(f"포트폴리오 백테스트 시작: 전략={request.strategy}, 종목수={len(request.portfolio)}")
            
            # 전략이 buy_and_hold가 아닌 경우 개별 종목별로 전략 백테스트 실행
            if request.strategy != "buy_and_hold":
                return await self.run_strategy_portfolio_backtest(request)
            else:
                return await self.run_buy_and_hold_portfolio_backtest(request)
                
        except Exception as e:
            logger.exception("포트폴리오 백테스트 실행 중 오류 발생")
            return {
                'status': 'error',
                'error': str(e),
                'code': 'PORTFOLIO_BACKTEST_ERROR'
            }
    
    async def run_strategy_portfolio_backtest(self, request: PortfolioBacktestRequest) -> Dict[str, Any]:
        """
        전략 기반 포트폴리오 백테스트 실행
        각 종목에 동일한 전략을 적용하고 가중치로 결합
        """
        try:
            portfolio_results = {}
            individual_returns = {}
            total_portfolio_value = 0
            
            logger.info(f"전략 기반 백테스트: {request.strategy}")
            
            # 각 종목별로 전략 백테스트 실행
            for item in request.portfolio:
                symbol = item.symbol
                weight = item.weight
                allocated_cash = request.initial_capital * weight
                
                logger.info(f"종목 {symbol} 전략 백테스트 실행 (가중치: {weight:.3f}, 자본: ${allocated_cash:,.2f})")
                
                # 개별 종목 백테스트 요청 생성
                backtest_req = BacktestRequest(
                    ticker=symbol,
                    start_date=request.start_date,
                    end_date=request.end_date,
                    initial_cash=allocated_cash,
                    strategy=request.strategy,
                    strategy_params=request.strategy_params or {}
                )
                
                try:
                    # 개별 종목 백테스트 실행
                    result = await backtest_service.run_backtest(backtest_req)
                    
                    if result and 'final_equity' in result:
                        final_value = result['final_equity']
                        initial_value = allocated_cash
                        stock_return = (final_value / initial_value - 1) * 100
                        
                        portfolio_results[symbol] = {
                            'initial_value': initial_value,
                            'final_value': final_value,
                            'return_pct': stock_return,
                            'weight': weight,
                            'strategy_stats': result
                        }
                        
                        individual_returns[symbol] = {
                            'weight': weight,
                            'return': stock_return,
                            'initial_value': initial_value,
                            'final_value': final_value,
                            'trades': result.get('num_trades', 0),
                            'win_rate': result.get('win_rate_pct', 0)
                        }
                        
                        total_portfolio_value += final_value
                        
                        logger.info(f"종목 {symbol} 완료: {stock_return:.2f}% 수익률")
                    else:
                        logger.warning(f"종목 {symbol} 백테스트 실패")
                        
                except Exception as e:
                    logger.error(f"종목 {symbol} 백테스트 오류: {str(e)}")
                    continue
            
            if not portfolio_results:
                raise ValueError("모든 종목의 백테스트가 실패했습니다.")
            
            # 포트폴리오 전체 통계 계산
            portfolio_return = (total_portfolio_value / request.initial_capital - 1) * 100
            total_trades = sum(result.get('strategy_stats', {}).get('num_trades', 0) 
                             for result in portfolio_results.values())
            
            # 가중 평균 승률 계산
            weighted_win_rate = sum(
                result['weight'] * result.get('strategy_stats', {}).get('win_rate_pct', 0)
                for result in portfolio_results.values()
            )
            
            portfolio_statistics = {
                'Start': request.start_date,
                'End': request.end_date,
                'Strategy': request.strategy,
                'Strategy_Params': request.strategy_params,
                'Initial_Value': request.initial_capital,
                'Final_Value': total_portfolio_value,
                'Total_Return': portfolio_return,
                'Total_Trades': total_trades,
                'Portfolio_Win_Rate': weighted_win_rate,
                'Active_Stocks': len(portfolio_results),
                'Commission': request.commission
            }
            
            result = {
                'status': 'success',
                'data': {
                    'portfolio_statistics': portfolio_statistics,
                    'individual_returns': individual_returns,
                    'portfolio_composition': [
                        {'symbol': symbol, 'weight': result['weight']}
                        for symbol, result in portfolio_results.items()
                    ],
                    'strategy_details': {
                        symbol: result['strategy_stats']
                        for symbol, result in portfolio_results.items()
                    }
                }
            }
            
            logger.info(f"전략 포트폴리오 백테스트 완료: 총 수익률 {portfolio_return:.2f}%")
            
            return recursive_serialize(result)
            
        except Exception as e:
            logger.exception("전략 포트폴리오 백테스트 실행 중 오류 발생")
            return {
                'status': 'error',
                'error': str(e),
                'code': 'STRATEGY_PORTFOLIO_BACKTEST_ERROR'
            }
    
    async def run_buy_and_hold_portfolio_backtest(self, request: PortfolioBacktestRequest) -> Dict[str, Any]:
        """
        기존 Buy & Hold 포트폴리오 백테스트 실행 (기존 로직 유지)
        """
        try:
            # 각 종목의 데이터 수집
            portfolio_data = {}
            weights = {}
            
            for item in request.portfolio:
                symbol = item.symbol
                weight = item.weight
                
                logger.info(f"종목 {symbol} 데이터 로드 중 (가중치: {weight:.3f})")
                
                # DB에서 데이터 로드
                df = load_ticker_data(symbol, request.start_date, request.end_date)
                
                if df is None or df.empty:
                    logger.warning(f"종목 {symbol}의 데이터가 없습니다.")
                    continue
                
                portfolio_data[symbol] = df
                weights[symbol] = weight
                
                logger.info(f"종목 {symbol} 데이터 로드 완료: {len(df)} 행")
            
            if not portfolio_data:
                raise ValueError("포트폴리오의 어떤 종목도 데이터를 가져올 수 없습니다.")
            
            # 실제 사용된 종목들의 가중치 정규화
            total_weight = sum(weights.values())
            if total_weight != 1.0:
                logger.info(f"가중치 정규화: {total_weight:.3f} -> 1.0")
                weights = {k: v / total_weight for k, v in weights.items()}
            
            # 포트폴리오 수익률 계산
            logger.info("포트폴리오 수익률 계산 중...")
            portfolio_result = self.calculate_portfolio_returns(
                portfolio_data, weights, request.rebalance_frequency
            )
            
            # 통계 계산
            logger.info("포트폴리오 통계 계산 중...")
            statistics = self.calculate_portfolio_statistics(portfolio_result, request.initial_capital)
            
            # 개별 종목 수익률 (참고용)
            individual_returns = {}
            for symbol, df in portfolio_data.items():
                if len(df) > 0:
                    start_price = df['Close'].iloc[0]
                    end_price = df['Close'].iloc[-1]
                    individual_return = (end_price / start_price - 1) * 100
                    individual_returns[symbol] = {
                        'weight': weights[symbol],
                        'return': individual_return,
                        'start_price': start_price,
                        'end_price': end_price
                    }
            
            # 결과 포맷팅
            result = {
                'status': 'success',
                'data': {
                    'portfolio_statistics': statistics,
                    'individual_returns': individual_returns,
                    'portfolio_composition': [
                        {'symbol': symbol, 'weight': weight}
                        for symbol, weight in weights.items()
                    ],
                    'equity_curve': {
                        date.strftime('%Y-%m-%d'): value * request.initial_capital
                        for date, value in portfolio_result['Portfolio_Value'].items()
                    },
                    'daily_returns': {
                        date.strftime('%Y-%m-%d'): return_val * 100
                        for date, return_val in portfolio_result['Daily_Return'].items()
                    }
                }
            }
            
            logger.info(f"Buy & Hold 포트폴리오 백테스트 완료: 총 수익률 {statistics['Total_Return']:.2f}%")
            
            return recursive_serialize(result)
            
        except Exception as e:
            logger.exception("Buy & Hold 포트폴리오 백테스트 실행 중 오류 발생")
            return {
                'status': 'error',
                'error': str(e),
                'code': 'BUY_HOLD_PORTFOLIO_BACKTEST_ERROR'
            }
