from fastapi import APIRouter, Query, HTTPException
from typing import Optional
from app.services.stock import StockService
from app.models.schemas import StockPriceResponse, StockPriceError
from app.core.exceptions import BacktestError

router = APIRouter()

@router.get(
    "/prices/{symbol}",
    response_model=StockPriceResponse,
    responses={
        400: {"model": StockPriceError, "description": "Bad Request"},
        404: {"model": StockPriceError, "description": "Data Not Found"},
        500: {"model": StockPriceError, "description": "Internal Server Error"}
    }
)
async def get_stock_prices(
    symbol: str,
    days: Optional[int] = Query(5, ge=1, le=30, description="Number of days to retrieve (1-30 days)")
) -> StockPriceResponse:
    """Retrieves the latest N days of stock price data for a given symbol.

    Parameters
    ----------
    symbol : str
        Stock symbol (e.g., "AAPL")
    days : int, optional
        Number of days to retrieve (default: 5, max: 30)
        
    Returns
    -------
    StockPriceResponse
        Response containing stock price data and metadata
    """
    try:
        # 심볼 검증
        if not symbol.isalpha():
            raise BacktestError(
                "주식 심볼은 영문자만 사용할 수 있습니다.",
                "INVALID_SYMBOL"
            )
        
        # 데이터 조회
        result = StockService.get_recent_prices(symbol, days)
        return StockPriceResponse(**result)
        
    except BacktestError as e:
        if e.code == "INVALID_SYMBOL":
            raise HTTPException(status_code=400, detail=str(e))
        elif e.code == "NO_DATA":
            raise HTTPException(status_code=404, detail=str(e))
        else:
            raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Internal Server Error: {str(e)}"
        ) 