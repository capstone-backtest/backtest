"""
포트폴리오 백테스트 서비스
"""
import pandas as pd
import numpy as np
from typing import List, Dict, Any, Tuple
from datetime import datetime, timedelta
import logging

from app.models.schemas import PortfolioBacktestRequest, PortfolioStock
from app.models.requests import BacktestRequest
from app.services.yfinance_db import load_ticker_data
from app.services.backtest_service import backtest_service
from app.utils.serializers import recursive_serialize

logger = logging.getLogger(__name__)


class PortfolioBacktestService:
    """포트폴리오 백테스트 서비스"""
    
    @staticmethod
    def calculate_dca_portfolio_returns(
        portfolio_data: Dict[str, pd.DataFrame],
        amounts: Dict[str, float],
        dca_info: Dict[str, Dict],
        start_date: str,
        end_date: str,
        rebalance_frequency: str = "monthly"
    ) -> pd.DataFrame:
        """
        분할 매수(DCA)를 고려한 포트폴리오 수익률을 계산합니다.
        
        Args:
            portfolio_data: 각 종목의 가격 데이터 {symbol: DataFrame}
            amounts: 각 종목의 총 투자 금액 {symbol: amount}
            dca_info: 분할 매수 정보 {symbol: {investment_type, dca_periods, monthly_amount}}
            start_date: 시작 날짜
            end_date: 종료 날짜
            rebalance_frequency: 리밸런싱 주기
            
        Returns:
            포트폴리오 가치와 수익률이 포함된 DataFrame
        """
        from datetime import datetime, timedelta
        import pandas as pd
        
        # 현금 처리: CASH 심볼은 수익률 0%로 처리
        cash_amount = amounts.get('CASH', 0)
        stock_amounts = {k: v for k, v in amounts.items() if k != 'CASH'}
        
        # 모든 주식 종목의 날짜 범위를 통합
        all_dates = set()
        for symbol, df in portfolio_data.items():
            if symbol != 'CASH':  # 현금 제외
                all_dates.update(df.index)
        
        if not all_dates and cash_amount == 0:
            raise ValueError("유효한 데이터가 없습니다.")
        
        # 현금만 있는 경우 처리
        if not all_dates and cash_amount > 0:
            # 기본 날짜 범위 생성 (1일)
            from datetime import datetime
            today = datetime.now().date()
            date_range = pd.DatetimeIndex([today])
        else:
            date_range = pd.DatetimeIndex(sorted(all_dates))
        
        # 총 투자 금액 계산
        total_amount = sum(amounts.values())
        
        # 시작/종료 날짜 파싱
        start_date_obj = datetime.strptime(start_date, '%Y-%m-%d')
        end_date_obj = datetime.strptime(end_date, '%Y-%m-%d')
        
        # 포트폴리오 가치 시뮬레이션
        portfolio_values = []
        daily_returns = []
        prev_portfolio_value = 0
        
        for current_date in date_range:
            if current_date.date() < start_date_obj.date():
                continue
            if current_date.date() > end_date_obj.date():
                break
                
            current_portfolio_value = cash_amount  # 현금부터 시작
            
            # 각 포트폴리오 항목의 현재 가치 계산 (중복 종목 지원)
            for unique_key, amount in amounts.items():
                if unique_key.endswith('_CASH') or unique_key.split('_')[0] == 'CASH':
                    continue
                    
                symbol = dca_info[unique_key]['symbol']
                info = dca_info[unique_key]
                investment_type = info['investment_type']
                
                if symbol not in portfolio_data:
                    continue
                    
                df = portfolio_data[symbol]
                
                try:
                    # 해당 날짜의 가격 찾기
                    price_data = df[df.index.date <= current_date.date()]
                    if price_data.empty:
                        continue
                        
                    current_price = price_data['Close'].iloc[-1]
                    
                    if investment_type == 'lump_sum':
                        # 일시불 투자: 시작일에 전액 투자
                        if current_date.date() == start_date_obj.date():
                            # 시작 가격으로 주식 수량 계산
                            start_price = price_data['Close'].iloc[0] if not price_data.empty else current_price
                            shares = amount / start_price
                        else:
                            # 이전에 계산된 주식 수량 유지 (간단히 첫날 기준으로 계산)
                            first_price_data = df[df.index.date >= start_date_obj.date()]
                            if not first_price_data.empty:
                                start_price = first_price_data['Close'].iloc[0]
                                shares = amount / start_price
                            else:
                                shares = 0
                        
                        stock_value = shares * current_price
                        
                    else:  # DCA
                        # 분할 매수: 매월 일정 금액씩 투자
                        dca_periods = info['dca_periods']
                        monthly_amount = info['monthly_amount']
                        
                        # 현재까지 투자한 개월 수 계산
                        months_passed = (current_date.year - start_date_obj.year) * 12 + (current_date.month - start_date_obj.month)
                        months_invested = min(months_passed + 1, dca_periods)  # 시작월 포함
                        
                        # 매월 투자한 주식의 누적 가치 계산
                        total_shares = 0
                        for month in range(months_invested):
                            # 각 월의 첫 거래일 가격으로 매수
                            investment_date = start_date_obj + timedelta(days=30 * month)
                            month_price_data = df[df.index.date >= investment_date.date()]
                            
                            if not month_price_data.empty:
                                month_price = month_price_data['Close'].iloc[0]
                                shares_bought = monthly_amount / month_price
                                total_shares += shares_bought
                        
                        stock_value = total_shares * current_price
                    
                    current_portfolio_value += stock_value
                    
                except Exception as e:
                    logger.warning(f"포트폴리오 항목 {unique_key} (종목: {symbol}) 가치 계산 오류 ({current_date}): {e}")
                    continue
            
            # 일일 수익률 계산
            if prev_portfolio_value > 0:
                daily_return = (current_portfolio_value - prev_portfolio_value) / prev_portfolio_value
            else:
                daily_return = 0.0
            
            portfolio_values.append(current_portfolio_value / total_amount)  # 정규화된 가치
            daily_returns.append(daily_return)
            prev_portfolio_value = current_portfolio_value
        
        # 결과 DataFrame 생성
        valid_dates = [d for d in date_range if start_date_obj.date() <= d.date() <= end_date_obj.date()]
        
        if len(portfolio_values) != len(valid_dates):
            # 길이가 맞지 않으면 기본값으로 채움
            portfolio_values = [1.0] * len(valid_dates)
            daily_returns = [0.0] * len(valid_dates)
        
        result = pd.DataFrame({
            'Date': valid_dates,
            'Portfolio_Value': portfolio_values,
            'Daily_Return': daily_returns,
            'Cumulative_Return': [(v - 1) * 100 for v in portfolio_values]
        })
        result.set_index('Date', inplace=True)
        
        return result

    @staticmethod
    def calculate_portfolio_returns(
        portfolio_data: Dict[str, pd.DataFrame],
        amounts: Dict[str, float],
        rebalance_frequency: str = "monthly"
    ) -> pd.DataFrame:
        """
        포트폴리오 수익률을 계산합니다.
        
        Args:
            portfolio_data: 각 종목의 가격 데이터 {symbol: DataFrame}
            amounts: 각 종목의 투자 금액 {symbol: amount}
            rebalance_frequency: 리밸런싱 주기
            
        Returns:
            포트폴리오 가치와 수익률이 포함된 DataFrame
        """
        # 현금 처리: CASH 심볼은 수익률 0%로 처리
        cash_amount = amounts.get('CASH', 0)
        stock_amounts = {k: v for k, v in amounts.items() if k != 'CASH'}
        
        # 모든 주식 종목의 날짜 범위를 통합
        all_dates = set()
        for symbol, df in portfolio_data.items():
            if symbol != 'CASH':  # 현금 제외
                all_dates.update(df.index)
        
        if not all_dates and cash_amount == 0:
            raise ValueError("유효한 데이터가 없습니다.")
        
        # 현금만 있는 경우 처리
        if not all_dates and cash_amount > 0:
            # 기본 날짜 범위 생성 (1일)
            from datetime import datetime
            today = datetime.now().date()
            date_range = pd.DatetimeIndex([today])
        else:
            date_range = pd.DatetimeIndex(sorted(all_dates))
        
        # 총 투자 금액 계산
        total_amount = sum(amounts.values())
        
        # 각 종목의 수익률 계산
        returns_data = {}
        for symbol, df in portfolio_data.items():
            if symbol == 'CASH':
                continue  # 현금은 별도 처리
            if len(df) == 0:
                continue
            # 종목별 일일 수익률 계산
            daily_returns = df['Close'].pct_change().fillna(0)
            returns_data[symbol] = daily_returns.reindex(date_range, fill_value=0)
        
        # 현금 수익률 추가 (항상 0%)
        if cash_amount > 0:
            returns_data['CASH'] = pd.Series(0.0, index=date_range)
        
        if not returns_data:
            raise ValueError("유효한 데이터가 없습니다.")
        
        # 포트폴리오 수익률 계산 (투자 금액 기준 가중 평균)
        portfolio_returns = pd.Series(0.0, index=date_range)
        
        for symbol, amount in amounts.items():
            if symbol in returns_data:
                weight = amount / total_amount  # 투자 금액 비율로 가중치 계산
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
    def calculate_portfolio_returns(
        portfolio_data: Dict[str, pd.DataFrame],
        amounts: Dict[str, float],
        rebalance_frequency: str = "monthly"
    ) -> pd.DataFrame:
        """
        포트폴리오 수익률을 계산합니다.
        
        Args:
            portfolio_data: 각 종목의 가격 데이터 {symbol: DataFrame}
            amounts: 각 종목의 투자 금액 {symbol: amount}
            rebalance_frequency: 리밸런싱 주기
            
        Returns:
            포트폴리오 가치와 수익률이 포함된 DataFrame
        """
        # 현금 처리: CASH 심볼은 수익률 0%로 처리
        cash_amount = amounts.get('CASH', 0)
        stock_amounts = {k: v for k, v in amounts.items() if k != 'CASH'}
        
        # 모든 주식 종목의 날짜 범위를 통합
        all_dates = set()
        for symbol, df in portfolio_data.items():
            if symbol != 'CASH':  # 현금 제외
                all_dates.update(df.index)
        
        if not all_dates and cash_amount == 0:
            raise ValueError("유효한 데이터가 없습니다.")
        
        # 현금만 있는 경우 처리
        if not all_dates and cash_amount > 0:
            # 기본 날짜 범위 생성 (1일)
            from datetime import datetime
            today = datetime.now().date()
            date_range = pd.DatetimeIndex([today])
        else:
            date_range = pd.DatetimeIndex(sorted(all_dates))
        
        # 총 투자 금액 계산
        total_amount = sum(amounts.values())
        
        # 각 종목의 수익률 계산
        returns_data = {}
        for symbol, df in portfolio_data.items():
            if symbol == 'CASH':
                continue  # 현금은 별도 처리
            if len(df) == 0:
                continue
            # 종목별 일일 수익률 계산
            daily_returns = df['Close'].pct_change().fillna(0)
            returns_data[symbol] = daily_returns.reindex(date_range, fill_value=0)
        
        # 현금 수익률 추가 (항상 0%)
        if cash_amount > 0:
            returns_data['CASH'] = pd.Series(0.0, index=date_range)
        
        if not returns_data:
            raise ValueError("유효한 데이터가 없습니다.")
        
        # 포트폴리오 수익률 계산 (투자 금액 기준 가중 평균)
        portfolio_returns = pd.Series(0.0, index=date_range)
        
        for symbol, amount in amounts.items():
            if symbol in returns_data:
                weight = amount / total_amount  # 투자 금액 비율로 가중치 계산
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
    def calculate_portfolio_statistics(portfolio_data: pd.DataFrame, total_amount: float) -> Dict[str, Any]:
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
            'Initial_Value': total_amount,
            'Final_Value': final_value * total_amount,
            'Peak_Value': peak_value * total_amount,
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
    
    async def _calculate_realistic_equity_curve(self, request: PortfolioBacktestRequest, 
                                              portfolio_results: Dict, total_amount: float) -> Tuple[Dict, Dict]:
        """
        실제 종목 데이터를 기반으로 포트폴리오 equity curve 계산
        """
        from datetime import datetime
        import pandas as pd
        
        # 각 종목의 실제 가격 데이터 로드
        portfolio_data = {}
        for symbol in portfolio_results.keys():
            df = load_ticker_data(symbol, request.start_date, request.end_date)
            if df is not None and not df.empty:
                portfolio_data[symbol] = df
        
        if not portfolio_data:
            # 데이터가 없으면 기본 선형 계산으로 fallback
            return self._fallback_equity_curve(request, portfolio_results, total_amount)
        
        # 모든 데이터의 공통 날짜 범위 찾기
        all_dates = set()
        for df in portfolio_data.values():
            all_dates.update(df.index.strftime('%Y-%m-%d'))
        
        date_range = sorted(all_dates)
        
        equity_curve = {}
        daily_returns = {}
        prev_portfolio_value = total_amount
        
        for i, date_str in enumerate(date_range):
            date_obj = datetime.strptime(date_str, '%Y-%m-%d').date()
            portfolio_value = 0
            
            # 각 종목의 해당 날짜 가치 계산
            for symbol, result in portfolio_results.items():
                if symbol in portfolio_data:
                    df = portfolio_data[symbol]
                    try:
                        # 해당 날짜의 가격 찾기
                        price_data = df[df.index.strftime('%Y-%m-%d') == date_str]
                        if not price_data.empty:
                            current_price = price_data['Close'].iloc[0]
                            initial_price = df['Close'].iloc[0]
                            
                            # 해당 종목의 투자 금액 기준 현재 가치
                            stock_value = result['amount'] * (current_price / initial_price)
                            portfolio_value += stock_value
                    except:
                        # 데이터가 없으면 초기값 유지
                        portfolio_value += result['amount']
            
            # 일일 수익률 계산
            if i == 0:
                daily_return = 0.0
            else:
                daily_return = (portfolio_value - prev_portfolio_value) / prev_portfolio_value * 100 if prev_portfolio_value > 0 else 0.0
            
            equity_curve[date_str] = portfolio_value
            daily_returns[date_str] = daily_return
            prev_portfolio_value = portfolio_value
        
        return equity_curve, daily_returns
    
    def _fallback_equity_curve(self, request: PortfolioBacktestRequest, 
                              portfolio_results: Dict, total_amount: float) -> Tuple[Dict, Dict]:
        """
        데이터가 없을 때 사용하는 기본 equity curve (선형)
        """
        from datetime import datetime
        import pandas as pd
        
        start_date_obj = datetime.strptime(request.start_date, '%Y-%m-%d')
        end_date_obj = datetime.strptime(request.end_date, '%Y-%m-%d')
        date_range = pd.date_range(start=start_date_obj, end=end_date_obj, freq='D')
        
        # 포트폴리오 최종 가치 계산
        final_portfolio_value = sum(result['final_value'] for result in portfolio_results.values())
        growth_rate = (final_portfolio_value / total_amount - 1)
        
        equity_curve = {}
        daily_returns = {}
        prev_equity = total_amount
        
        for i, date in enumerate(date_range):
            if i == 0:
                daily_return = 0.0
                equity_value = total_amount
            else:
                # 선형 성장 가정
                progress = i / (len(date_range) - 1) if len(date_range) > 1 else 1
                equity_value = total_amount * (1 + growth_rate * progress)
                daily_return = (equity_value - prev_equity) / prev_equity * 100 if prev_equity > 0 else 0.0
            
            equity_curve[date.strftime('%Y-%m-%d')] = equity_value
            daily_returns[date.strftime('%Y-%m-%d')] = daily_return
            prev_equity = equity_value
        
        return equity_curve, daily_returns
    
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
        각 종목에 동일한 전략을 적용하고 투자 금액으로 결합
        """
        try:
            portfolio_results = {}
            individual_returns = {}
            total_portfolio_value = 0
            total_amount = sum(item.amount for item in request.portfolio)
            
            logger.info(f"전략 기반 백테스트: {request.strategy}, 총 투자금액: ${total_amount:,.2f}")
            
            # 각 종목별로 전략 백테스트 실행 (중복 종목 지원)
            for idx, item in enumerate(request.portfolio):
                symbol = item.symbol
                amount = item.amount
                weight = amount / total_amount  # 가중치는 투자 금액 비율로 계산
                
                # 중복 종목을 위한 고유 키 생성
                unique_key = f"{symbol}_{idx}"
                
                # 현금 처리 (수익률 0%, 전략 적용 안함)
                if symbol == 'CASH':
                    logger.info(f"현금 {symbol} 처리 (투자금액: ${amount:,.2f}, 비중: {weight:.3f})")
                    
                    portfolio_results[unique_key] = {
                        'symbol': symbol,
                        'initial_value': amount,
                        'final_value': amount,  # 현금은 변동 없음
                        'return_pct': 0.0,  # 현금 수익률 0%
                        'weight': weight,
                        'amount': amount,
                        'strategy_stats': {
                            'total_trades': 0,
                            'win_rate_pct': 0.0,
                            'max_drawdown_pct': 0.0,
                            'sharpe_ratio': 0.0,
                            'final_equity': amount
                        }
                    }
                    
                    individual_returns[unique_key] = {
                        'symbol': symbol,
                        'weight': weight,
                        'amount': amount,
                        'return': 0.0,
                        'initial_value': amount,
                        'final_value': amount,
                        'trades': 0,
                        'win_rate': 0.0
                    }
                    
                    total_portfolio_value += amount
                    logger.info(f"현금 {symbol} 완료: 0.00% 수익률")
                    continue
                
                logger.info(f"종목 {symbol} (#{idx+1}) 전략 백테스트 실행 (투자금액: ${amount:,.2f}, 비중: {weight:.3f})")
                
                # 개별 종목 백테스트 요청 생성
                backtest_req = BacktestRequest(
                    ticker=symbol,
                    start_date=request.start_date,
                    end_date=request.end_date,
                    initial_cash=amount,
                    strategy=request.strategy,
                    strategy_params=request.strategy_params or {}
                )
                
                try:
                    # 개별 종목 백테스트 실행
                    result = await backtest_service.run_backtest(backtest_req)
                    
                    if result and hasattr(result, 'final_equity'):
                        final_value = result.final_equity
                        initial_value = amount
                        stock_return = (final_value / initial_value - 1) * 100
                        
                        portfolio_results[unique_key] = {
                            'symbol': symbol,
                            'initial_value': initial_value,
                            'final_value': final_value,
                            'return_pct': stock_return,
                            'weight': weight,
                            'amount': amount,
                            'strategy_stats': result.__dict__  # 객체를 딕셔너리로 변환
                        }
                        
                        individual_returns[unique_key] = {
                            'symbol': symbol,
                            'weight': weight,
                            'amount': amount,
                            'return': stock_return,
                            'initial_value': initial_value,
                            'final_value': final_value,
                            'trades': getattr(result, 'total_trades', 0),
                            'win_rate': getattr(result, 'win_rate_pct', 0)
                        }
                        
                        total_portfolio_value += final_value
                        
                        logger.info(f"종목 {symbol} (#{idx+1}) 완료: {stock_return:.2f}% 수익률, 거래수: {getattr(result, 'total_trades', 0)}")
                    else:
                        logger.warning(f"종목 {symbol} 백테스트 실패: 결과가 없거나 final_equity 속성이 없음")
                        
                except Exception as e:
                    logger.error(f"종목 {symbol} 백테스트 오류: {str(e)}")
                    continue
            
            if not portfolio_results:
                raise ValueError("모든 종목의 백테스트가 실패했습니다.")
            
            # 포트폴리오 전체 통계 계산
            portfolio_return = (total_portfolio_value / total_amount - 1) * 100
            total_trades = sum(result.get('strategy_stats', {}).get('total_trades', 0) 
                             for result in portfolio_results.values())
            
            # 가중 평균 승률 계산
            weighted_win_rate = sum(
                result['weight'] * result.get('strategy_stats', {}).get('win_rate_pct', 0)
                for result in portfolio_results.values()
            )
            
            # 가중 평균 최대 드로우다운 계산
            weighted_max_drawdown = sum(
                result['weight'] * abs(result.get('strategy_stats', {}).get('max_drawdown_pct', 0))
                for result in portfolio_results.values()
            )
            
            # 가중 평균 샤프 비율 계산
            weighted_sharpe_ratio = sum(
                result['weight'] * result.get('strategy_stats', {}).get('sharpe_ratio', 0)
                for result in portfolio_results.values()
            )
            
            # 백테스트 기간 계산
            from datetime import datetime
            start_date_obj = datetime.strptime(request.start_date, '%Y-%m-%d')
            end_date_obj = datetime.strptime(request.end_date, '%Y-%m-%d')
            duration_days = (end_date_obj - start_date_obj).days
            
            # 연간 수익률 계산
            annual_return = ((total_portfolio_value / total_amount) ** (365.25 / duration_days) - 1) * 100 if duration_days > 0 else 0
            
            # 포트폴리오 통계 (프론트엔드 호환)
            portfolio_statistics = {
                'Start': request.start_date,
                'End': request.end_date,
                'Duration': f'{duration_days} days',
                'Initial_Value': total_amount,
                'Final_Value': total_portfolio_value,
                'Peak_Value': total_portfolio_value,  # 전략 기반에서는 최종값과 동일하게 가정
                'Total_Return': portfolio_return,
                'Annual_Return': annual_return,
                'Annual_Volatility': 0.0,  # 전략 기반에서는 계산 복잡하므로 0으로 설정
                'Sharpe_Ratio': weighted_sharpe_ratio,
                'Max_Drawdown': -weighted_max_drawdown,  # 음수로 표시
                'Avg_Drawdown': -weighted_max_drawdown / 2,  # 평균 드로우다운 추정
                'Max_Consecutive_Gains': 0,  # 전략 기반에서는 계산 복잡
                'Max_Consecutive_Losses': 0,  # 전략 기반에서는 계산 복잡
                'Total_Trading_Days': duration_days,
                'Positive_Days': 0,  # 전략 기반에서는 계산 복잡
                'Negative_Days': 0,  # 전략 기반에서는 계산 복잡
                'Win_Rate': weighted_win_rate
            }
            
            # 실제 포트폴리오 equity curve 생성
            # 각 종목의 실제 가격 데이터를 기반으로 일일 포트폴리오 가치 계산
            equity_curve, daily_returns = await self._calculate_realistic_equity_curve(
                request, portfolio_results, total_amount
            )

            result = {
                'status': 'success',
                'data': {
                    'portfolio_statistics': portfolio_statistics,
                    'individual_returns': individual_returns,
                    'portfolio_composition': [
                        {'symbol': symbol, 'weight': result['weight'], 'amount': result['amount']}
                        for symbol, result in portfolio_results.items()
                    ],
                    'strategy_details': {
                        symbol: result['strategy_stats']
                        for symbol, result in portfolio_results.items()
                    },
                    'equity_curve': equity_curve,
                    'daily_returns': daily_returns
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
        Buy & Hold 포트폴리오 백테스트 실행 (투자 금액 기반)
        현금(CASH)과 주식을 함께 처리, 분할 매수(DCA) 지원
        """
        try:
            # 각 종목의 데이터 수집 (중복 종목 지원)
            portfolio_data = {}
            amounts = {}
            total_amount = sum(item.amount for item in request.portfolio)
            cash_amount = 0
            
            # 분할 매수 정보 수집 (중복 종목 지원)
            dca_info = {}
            
            for idx, item in enumerate(request.portfolio):
                symbol = item.symbol
                amount = item.amount
                investment_type = getattr(item, 'investment_type', 'lump_sum')
                dca_periods = getattr(item, 'dca_periods', 12)
                
                # 중복 종목을 위한 고유 키 생성
                unique_key = f"{symbol}_{idx}"
                
                # 분할 매수 정보 저장
                dca_info[unique_key] = {
                    'symbol': symbol,
                    'investment_type': investment_type,
                    'dca_periods': dca_periods,
                    'monthly_amount': amount / dca_periods if investment_type == 'dca' else amount
                }
                
                if symbol == 'CASH':
                    # 현금 처리
                    cash_amount += amount  # 중복 현금은 합산
                    amounts[unique_key] = amount
                    logger.info(f"현금 {symbol} (#{idx+1}) 추가 (금액: ${amount:,.2f})")
                    continue
                
                logger.info(f"종목 {symbol} (#{idx+1}) 데이터 로드 중 (투자금액: ${amount:,.2f}, 방식: {investment_type})")
                
                if investment_type == 'dca':
                    logger.info(f"분할 매수: ${amount:,.2f}을 {dca_periods}개월에 걸쳐 매달 ${amount/dca_periods:,.2f}씩")
                
                # DB에서 데이터 로드 (동일 종목은 한 번만 로드)
                if symbol not in portfolio_data:
                    df = load_ticker_data(symbol, request.start_date, request.end_date)
                    
                    if df is None or df.empty:
                        logger.warning(f"종목 {symbol}의 데이터가 없습니다.")
                        continue
                    
                    portfolio_data[symbol] = df
                    logger.info(f"종목 {symbol} 데이터 로드 완료: {len(df)} 행")
                
                amounts[unique_key] = amount
            
            # 현금만 있는 경우 처리
            if not portfolio_data and cash_amount > 0:
                logger.info("현금만 있는 포트폴리오로 백테스트 실행")
                
                # 현금 전용 결과 생성
                from datetime import datetime
                start_date_obj = datetime.strptime(request.start_date, '%Y-%m-%d')
                end_date_obj = datetime.strptime(request.end_date, '%Y-%m-%d')
                duration_days = (end_date_obj - start_date_obj).days
                
                # 현금은 수익률 0%
                statistics = {
                    'Start': request.start_date,
                    'End': request.end_date,
                    'Duration': f'{duration_days} days',
                    'Initial_Value': cash_amount,
                    'Final_Value': cash_amount,
                    'Peak_Value': cash_amount,
                    'Total_Return': 0.0,
                    'Annual_Return': 0.0,
                    'Annual_Volatility': 0.0,
                    'Sharpe_Ratio': 0.0,
                    'Max_Drawdown': 0.0,
                    'Avg_Drawdown': 0.0,
                    'Max_Consecutive_Gains': 0,
                    'Max_Consecutive_Losses': 0,
                    'Total_Trading_Days': duration_days,
                    'Positive_Days': 0,
                    'Negative_Days': 0,
                    'Win_Rate': 0.0
                }
                
                individual_returns = {
                    'CASH': {
                        'weight': 1.0,
                        'amount': cash_amount,
                        'return': 0.0,
                        'start_price': 1.0,
                        'end_price': 1.0,
                        'investment_type': 'lump_sum'
                    }
                }
                
                # 기본 equity curve (현금은 변동 없음)
                date_range = pd.date_range(start=start_date_obj, end=end_date_obj, freq='D')
                equity_curve = {
                    date.strftime('%Y-%m-%d'): cash_amount
                    for date in date_range
                }
                daily_returns = {
                    date.strftime('%Y-%m-%d'): 0.0
                    for date in date_range
                }
                
                result = {
                    'status': 'success',
                    'data': {
                        'portfolio_statistics': statistics,
                        'individual_returns': individual_returns,
                        'portfolio_composition': [
                            {'symbol': 'CASH', 'weight': 1.0, 'amount': cash_amount, 'investment_type': 'lump_sum'}
                        ],
                        'equity_curve': equity_curve,
                        'daily_returns': daily_returns
                    }
                }
                
                return recursive_serialize(result)
            
            # 주식과 현금이 모두 없는 경우
            if not portfolio_data and cash_amount == 0:
                raise ValueError("포트폴리오의 어떤 종목도 데이터를 가져올 수 없습니다.")
            
            # 분할 매수를 고려한 포트폴리오 수익률 계산
            logger.info("분할 매수를 고려한 포트폴리오 수익률 계산 중...")
            portfolio_result = self.calculate_dca_portfolio_returns(
                portfolio_data, amounts, dca_info, request.start_date, request.end_date, request.rebalance_frequency
            )
            
            # 통계 계산
            logger.info("포트폴리오 통계 계산 중...")
            statistics = self.calculate_portfolio_statistics(portfolio_result, total_amount)
            
            # 개별 종목 수익률 (참고용, 현금 포함)
            individual_returns = {}
            
            # 현금 수익률 추가
            if cash_amount > 0:
                individual_returns['CASH'] = {
                    'weight': cash_amount / total_amount,
                    'amount': cash_amount,
                    'return': 0.0,  # 현금 수익률은 0%
                    'start_price': 1.0,
                    'end_price': 1.0,
                    'investment_type': 'lump_sum'
                }
            
            # 주식 수익률 추가 (중복 종목 지원)
            for unique_key, amount in amounts.items():
                if unique_key.endswith('_CASH') or unique_key.split('_')[0] == 'CASH':
                    continue
                    
                symbol = dca_info[unique_key]['symbol']
                
                if symbol in portfolio_data:
                    df = portfolio_data[symbol]
                    if len(df) > 0:
                        investment_type = dca_info[unique_key]['investment_type']
                        weight = amount / total_amount
                        
                        if investment_type == 'lump_sum':
                            # 일시불: 시작가 대비 종료가로 수익률 계산
                            start_price = df['Close'].iloc[0]
                            end_price = df['Close'].iloc[-1]
                            individual_return = (end_price / start_price - 1) * 100
                            
                            individual_returns[unique_key] = {
                                'symbol': symbol,
                                'weight': weight,
                                'amount': amount,
                                'return': individual_return,
                                'start_price': start_price,
                                'end_price': end_price,
                                'investment_type': investment_type,
                                'dca_periods': None
                            }
                            
                        else:  # DCA
                            # 분할매수: 실제 매수한 주식의 평균 단가 대비 종료가로 수익률 계산
                            dca_periods = dca_info[unique_key]['dca_periods']
                            monthly_amount = dca_info[unique_key]['monthly_amount']
                            
                            # DCA 수익률 계산을 위한 시뮬레이션
                            from datetime import datetime, timedelta
                            start_date_obj = datetime.strptime(request.start_date, '%Y-%m-%d')
                            
                            total_shares = 0
                            total_invested = 0
                            
                            for month in range(dca_periods):
                                # 각 월의 첫 거래일 가격으로 매수
                                investment_date = start_date_obj + timedelta(days=30 * month)
                                
                                # 해당 날짜 이후의 첫 거래일 찾기
                                month_price_data = df[df.index.date >= investment_date.date()]
                                
                                if not month_price_data.empty:
                                    month_price = month_price_data['Close'].iloc[0]
                                    shares_bought = monthly_amount / month_price
                                    total_shares += shares_bought
                                    total_invested += monthly_amount
                            
                            if total_shares > 0:
                                average_price = total_invested / total_shares  # 평균 매수 단가
                                end_price = df['Close'].iloc[-1]
                                individual_return = (end_price / average_price - 1) * 100
                            else:
                                average_price = 0
                                end_price = df['Close'].iloc[-1]
                                individual_return = 0
                            
                            individual_returns[unique_key] = {
                                'symbol': symbol,
                                'weight': weight,
                                'amount': amount,
                                'return': individual_return,
                                'start_price': average_price,  # DCA의 경우 평균 매수 단가
                                'end_price': end_price,
                                'investment_type': investment_type,
                                'dca_periods': dca_periods
                            }
            
            # 결과 포맷팅
            result = {
                'status': 'success',
                'data': {
                    'portfolio_statistics': statistics,
                    'individual_returns': individual_returns,
                    'portfolio_composition': [
                        {
                            'symbol': symbol, 
                            'weight': amount / total_amount, 
                            'amount': amount,
                            'investment_type': dca_info[symbol]['investment_type'],
                            'dca_periods': dca_info[symbol]['dca_periods'] if dca_info[symbol]['investment_type'] == 'dca' else None
                        }
                        for symbol, amount in amounts.items()
                    ],
                    'equity_curve': {
                        date.strftime('%Y-%m-%d'): value * total_amount
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
