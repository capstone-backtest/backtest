"""
API v1 router integration
"""
from fastapi import APIRouter
from .endpoints import backtest, strategies, optimize

api_router = APIRouter()

# Include each endpoint router in the main API router
api_router.include_router(
    backtest.router,
    prefix="/backtest",
    tags=["Backtesting"]
)

api_router.include_router(
    strategies.router,
    prefix="/strategies",
    tags=["Strategy Management"]
)

api_router.include_router(
    optimize.router,
    prefix="/optimize",
    tags=["Optimization"]
) 