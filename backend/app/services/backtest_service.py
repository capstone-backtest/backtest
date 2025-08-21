from datetime import datetime
from typing import Any, Dict, Optional
from backtesting import Backtest
import pandas as pd
import asyncio

from ..models.requests import BacktestRequest, OptimizationRequest
from ..models.responses import BacktestResult, OptimizationResult, ChartDataResponse, ChartDataPoint, EquityPoint, TradeMarker, IndicatorData
from ..utils.data_fetcher import data_fetcher
from ..services.strategy_service import StrategyService

class BacktestService:
    def __init__(self):
        self.strategy_service = StrategyService()

    def validate_backtest_request(self, request: BacktestRequest):
        """Validates the backtest request."""
        if not data_fetcher.validate_ticker(request.ticker):
            raise ValueError(f"Invalid or delisted ticker: {request.ticker}")
        
        self.strategy_service.get_strategy_class(request.strategy)
        
        if request.strategy_params:
            self.strategy_service.validate_and_apply_defaults(request.strategy, request.strategy_params)

    async def run_backtest(self, request: BacktestRequest) -> BacktestResult:
        """
        Runs a backtest without saving to the database.
        This is an async wrapper around the synchronous backtesting library.
        """
        loop = asyncio.get_event_loop()
        stats = await loop.run_in_executor(
            None, self._execute_backtest, request
        )
        return self._format_backtest_result(stats, request)

    def _execute_backtest(self, request: BacktestRequest) -> pd.Series:
        """The actual synchronous backtest execution."""
        strategy_class = self.strategy_service.get_strategy_class(request.strategy)
        
        params = request.strategy_params if request.strategy_params else {}
        strategy_with_params = self.strategy_service.update_strategy_params(strategy_class, params)

        data = data_fetcher.get_stock_data(request.ticker, request.start_date, request.end_date)
        if data.empty:
            raise ValueError("Could not fetch stock data for the given period.")

        bt = Backtest(data, strategy_with_params, cash=request.initial_cash, commission=request.commission)
        stats = bt.run()
        return stats

    async def generate_chart_data(self, request: BacktestRequest) -> ChartDataResponse:
        """
        Generates chart data from a backtest result.
        """
        loop = asyncio.get_event_loop()
        stats = await loop.run_in_executor(
            None, self._execute_backtest, request
        )
        
        equity_curve = stats['_equity_curve']
        trades = stats['_trades']
        ohlc_data = stats['_ohlc']

        # 1. OHLC Data
        chart_ohlc = [
            ChartDataPoint(
                timestamp=str(idx),
                date=idx.strftime('%Y-%m-%d'),
                open=row['Open'],
                high=row['High'],
                low=row['Low'],
                close=row['Close'],
                volume=int(row['Volume'])
            ) for idx, row in ohlc_data.iterrows()
        ]

        # 2. Equity Curve Data
        equity_points = [
            EquityPoint(
                timestamp=str(idx),
                date=idx.strftime('%Y-%m-%d'),
                equity=row['Equity'],
                return_pct=row['ReturnPct'],
                drawdown_pct=row['DrawdownPct']
            ) for idx, row in equity_curve.iterrows()
        ]

        # 3. Trade Markers
        trade_markers = [
            TradeMarker(
                timestamp=str(row['ExitTime']),
                date=row['ExitTime'].strftime('%Y-%m-%d'),
                price=row['ExitPrice'],
                type='exit',
                side='sell' if row['Size'] > 0 else 'buy',
                size=abs(row['Size']),
                pnl_pct=row['ReturnPct'] * 100
            ) for _, row in trades.iterrows()
        ]
        trade_markers.extend([
            TradeMarker(
                timestamp=str(row['EntryTime']),
                date=row['EntryTime'].strftime('%Y-%m-%d'),
                price=row['EntryPrice'],
                type='entry',
                side='buy' if row['Size'] > 0 else 'sell',
                size=abs(row['Size']),
                pnl_pct=None
            ) for _, row in trades.iterrows()
        ])
        
        # 4. Indicators
        # This part needs to be implemented based on the strategy's indicators
        indicators = []

        # 5. Summary Stats
        summary = self._format_backtest_result(stats, request)

        return ChartDataResponse(
            ticker=request.ticker,
            strategy=str(request.strategy.value),
            start_date=request.start_date,
            end_date=request.end_date,
            ohlc_data=chart_ohlc,
            equity_data=equity_points,
            trade_markers=trade_markers,
            indicators=indicators,
            summary_stats=summary.dict()
        )

    def _format_backtest_result(self, stats: pd.Series, request: BacktestRequest) -> BacktestResult:
        """Formats the backtesting stats into a BacktestResult model."""
        start_date = datetime.strptime(request.start_date, '%Y-%m-%d')
        end_date = datetime.strptime(request.end_date, '%Y-%m-%d')
        
        return BacktestResult(
            ticker=request.ticker,
            strategy=str(request.strategy.value),
            start_date=request.start_date,
            end_date=request.end_date,
            duration_days=(end_date - start_date).days,
            initial_cash=self.safe_float(stats.get('Start Value', request.initial_cash)),
            final_equity=self.safe_float(stats.get('Equity Final [$]', 0)),
            total_return_pct=self.safe_float(stats.get('Return [%]', 0)),
            annualized_return_pct=self.safe_float(stats.get('Return (Ann.) [%]', 0)),
            buy_and_hold_return_pct=self.safe_float(stats.get('Buy & Hold Return [%]', 0)),
            cagr_pct=self.safe_float(stats.get('Return (Ann.) [%]', 0)), # Using Ann. Return as approx.
            volatility_pct=self.safe_float(stats.get('Volatility (Ann.) [%]', 0)),
            sharpe_ratio=self.safe_float(stats.get('Sharpe Ratio', 0)),
            sortino_ratio=self.safe_float(stats.get('Sortino Ratio', 0)),
            calmar_ratio=self.safe_float(stats.get('Calmar Ratio', 0)),
            max_drawdown_pct=abs(self.safe_float(stats.get('Max. Drawdown [%]', 0))),
            avg_drawdown_pct=abs(self.safe_float(stats.get('Avg. Drawdown [%]', 0))),
            total_trades=self.safe_int(stats.get('# Trades', 0)),
            win_rate_pct=self.safe_float(stats.get('Win Rate [%]', 0)),
            profit_factor=self.safe_float(stats.get('Profit Factor', 0)),
            avg_trade_pct=self.safe_float(stats.get('Avg. Trade [%]', 0)),
            best_trade_pct=self.safe_float(stats.get('Best Trade [%]', 0)),
            worst_trade_pct=self.safe_float(stats.get('Worst Trade [%]', 0)),
            sqn=self.safe_float(stats.get('SQN', 0)),
            execution_time_seconds=self.safe_float(stats.get('Duration', '0s').replace('s','')),
            timestamp=datetime.now(),
        )

    def safe_float(self, value: Any, default: float = 0.0) -> float:
        try:
            return float(value)
        except (ValueError, TypeError, AttributeError):
            return default

    def safe_int(self, value: Any, default: int = 0) -> int:
        try:
            return int(value)
        except (ValueError, TypeError, AttributeError):
            return default