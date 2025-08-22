from fastapi import APIRouter, HTTPException, status
from ..models import BacktestRequest  # small local import path placeholder
from app.services.yfinance_db import load_ticker_data
from app.services.backtest_service import backtest_service
import logging

router = APIRouter()
logger = logging.getLogger(__name__)


@router.post("/run", status_code=status.HTTP_200_OK)
async def run_backtest_v2(request: dict):
    """v2: DB에서 데이터를 읽어 백테스트를 실행합니다.

    요청 본문은 v1의 BacktestRequest와 동일한 필드를 기대합니다.
    """
    try:
        ticker = request.get('ticker')
        start = request.get('start_date')
        end = request.get('end_date')
        if not ticker or not start or not end:
            raise HTTPException(status_code=400, detail="ticker, start_date, end_date 필수")

        df = load_ticker_data(ticker, start, end)
        if df.empty:
            raise HTTPException(status_code=404, detail="DB에 해당 기간 데이터가 없습니다.")

        # delegate to existing backtest service which expects BacktestRequest model
        # build a minimal request-like object
        class R: pass
        r = R()
        r.ticker = ticker
        r.start_date = start
        r.end_date = end
        r.initial_cash = request.get('initial_cash', 10000)
        r.strategy = request.get('strategy', 'sma_crossover')
        r.strategy_params = request.get('strategy_params', {})

        result = await backtest_service.run_backtest(r)
        return result

    except HTTPException:
        raise
    except Exception as e:
        logger.exception("v2 backtest 실패")
        raise HTTPException(status_code=500, detail=str(e))
