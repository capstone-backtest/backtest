"""
Backtesting API Endpoints
"""
from fastapi import APIRouter, HTTPException, status
from ....models.requests import BacktestRequest
from ....models.responses import BacktestResult, ErrorResponse, ChartDataResponse
from ....services.backtest_service import BacktestService
import logging

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post(
    "/run",
    response_model=BacktestResult,
    status_code=status.HTTP_200_OK,
    summary="Run Backtest",
    description="Executes a backtest with the given strategy and parameters."
)
async def run_backtest(request: BacktestRequest):
    """Backtest execution API

    - **ticker**: Stock ticker symbol (e.g., AAPL, GOOGL)
    - **start_date**: Backtest start date (YYYY-MM-DD)
    - **end_date**: Backtest end date (YYYY-MM-DD)
    - **initial_cash**: Initial investment amount
    - **strategy**: Name of the strategy to use
    - **strategy_params**: Strategy-specific parameters (optional)
    - **commission**: Transaction fee (default: 0.002)
    """
    try:
        backtest_service = BacktestService()
        # Validate request
        backtest_service.validate_backtest_request(request)
        
        # Run backtest
        result = await backtest_service.run_backtest(request)
        
        logger.info(f"Backtest API completed: {request.ticker}")
        return result
        
    except ValueError as e:
        logger.error(f"Backtest request error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        logger.error(f"Backtest execution error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An error occurred during backtest execution."
        )


@router.get(
    "/health",
    summary="Check Backtest Service Status",
    description="Checks the status of the backtest service."
)
async def backtest_health():
    """Backtest service health check"""
    try:
        # Simple validation logic
        from ....utils.data_fetcher import data_fetcher
        
        # Simple validation with a sample ticker
        is_healthy = data_fetcher.validate_ticker("AAPL")
        
        if is_healthy:
            return {
                "status": "healthy",
                "message": "Backtest service is operating normally.",
                "data_source": "Yahoo Finance connection successful"
            }
        else:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="There is an issue with the data source connection."
            )
            
    except Exception as e:
        logger.error(f"Health check failed: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Failed to check backtest service status"
        )


@router.post(
    "/chart-data",
    response_model=ChartDataResponse,
    status_code=status.HTTP_200_OK,
    summary="Backtest Chart Data",
    description="Returns backtest results as chart data for Recharts."
)
async def get_chart_data(request: BacktestRequest):
    """Backtest Chart Data API
    
    Executes a backtest and returns the results as chart data that can be used by the Recharts library 
    in JSON format.
    
    **Returned Data:**
    - **ohlc_data**: OHLC data for candlestick charts
    - **equity_data**: Equity curve data
    - **trade_markers**: Trade entry/exit markers
    - **indicators**: Technical indicator data
    - **summary_stats**: Key performance metrics
    
    **Usage Example (React + Recharts):**
    ```javascript
    // Candlestick Chart
    <ComposedChart data={chartData.ohlc_data}>
      <XAxis dataKey="date" />
      <YAxis />
      <Bar dataKey="volume" />
      <Line dataKey="close" />
    </ComposedChart>
    
    // Equity Curve
    <LineChart data={chartData.equity_data}>
      <Line dataKey="return_pct" stroke="#8884d8" />
      <Line dataKey="drawdown_pct" stroke="#ff0000" />
    </LineChart>
    ```
    """
    try:
        backtest_service = BacktestService()
        # Validate request
        backtest_service.validate_backtest_request(request)
        
        # Generate chart data
        chart_data = await backtest_service.generate_chart_data(request)
        
        logger.info(f"Chart data API completed: {request.ticker}, data points: {len(chart_data.ohlc_data)}")
        return chart_data
        
    except ValueError as e:
        logger.error(f"Chart data request error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        logger.error(f"Chart data generation error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An error occurred during chart data generation."
        ) 