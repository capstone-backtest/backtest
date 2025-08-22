from fastapi import APIRouter, HTTPException, status
from ....models.requests import BacktestRequest
from ....models.responses import BacktestResult, ChartDataResponse
from ....services.backtest_service import backtest_service
from ....services.yfinance_db import load_ticker_data
from ....utils.data_fetcher import data_fetcher
import logging

router = APIRouter()
logger = logging.getLogger(__name__)


@router.post(
    "/run",
    response_model=BacktestResult,
    status_code=status.HTTP_200_OK,
    summary="백테스트 실행 (v2, DB 소스)",
    description="v1의 요청/응답 형식을 그대로 사용하되, 내부 데이터 소스는 DB를 읽습니다."
)
async def run_backtest_v2(request: BacktestRequest):
    """
    v2: DB에서 데이터를 읽어 기존 backtest 로직을 실행합니다.
    요청/응답 모델은 v1과 호환됩니다.
    """
    try:
        # load OHLC data from DB
        df = load_ticker_data(request.ticker, request.start_date, request.end_date)
        if df is None or df.empty:
            raise HTTPException(status_code=404, detail=f"No data for {request.ticker} in DB for the given range")

        # Temporarily override data_fetcher.get_stock_data so existing backtest_service uses our DataFrame
        original_get = data_fetcher.get_stock_data

        def _get_stock_data_override(*args, **kwargs):
            # Accept any positional/keyword args (e.g. start_date, end_date) and return the preloaded DataFrame
            return df

        data_fetcher.get_stock_data = _get_stock_data_override

        try:
            result = await backtest_service.run_backtest(request)
            return result
        finally:
            data_fetcher.get_stock_data = original_get

    except HTTPException:
        raise
    except Exception as e:
        logger.exception("v2 backtest execution error")
        raise HTTPException(status_code=500, detail=str(e))


@router.post(
    "/chart-data",
    response_model=ChartDataResponse,
    status_code=status.HTTP_200_OK,
    summary="차트 데이터 조회 (v2, DB 소스)",
    description="v1의 chart-data 응답 형식을 그대로 반환합니다. 내부 데이터는 DB에서 로드됩니다."
)
async def get_chart_data_v2(request: BacktestRequest):
    try:
        df = load_ticker_data(request.ticker, request.start_date, request.end_date)
        if df is None or df.empty:
            raise HTTPException(status_code=404, detail=f"No data for {request.ticker} in DB for the given range")

        original_get = data_fetcher.get_stock_data

        def _get_stock_data_override(*args, **kwargs):
            # Accept any positional/keyword args (e.g. start_date, end_date) and return the preloaded DataFrame
            return df

        data_fetcher.get_stock_data = _get_stock_data_override

        try:
            chart_data = await backtest_service.generate_chart_data(request)
            return chart_data
        finally:
            data_fetcher.get_stock_data = original_get

    except HTTPException:
        raise
    except Exception as e:
        logger.exception("v2 chart-data error")
        raise HTTPException(status_code=500, detail=str(e))
