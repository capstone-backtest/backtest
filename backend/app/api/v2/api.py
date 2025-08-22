from fastapi import APIRouter
from .endpoints import backtest

api_router = APIRouter()

api_router.include_router(
    backtest.router,
    prefix="/backtest",
    tags=["백테스트 v2"]
)
