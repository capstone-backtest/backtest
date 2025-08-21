"""
Backtesting Optimization API Endpoints
"""
from fastapi import APIRouter, HTTPException, status
from typing import List, Dict, Any

from ....models.requests import OptimizationRequest
from ....models.responses import OptimizationResult
from ....services.backtest_service import BacktestService
import logging

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post(
    "/run",
    response_model=OptimizationResult,
    status_code=status.HTTP_200_OK,
    summary="Run Strategy Parameter Optimization",
    description="Optimizes the parameters of a given strategy to find the best performance."
)
async def run_optimization(
    request: OptimizationRequest
):
    """
    Strategy Parameter Optimization API
    
    - **ticker**: Stock ticker symbol (e.g., AAPL, GOOGL)
    - **start_date**: Backtest start date (YYYY-MM-DD)
    - **end_date**: Backtest end date (YYYY-MM-DD)
    - **initial_cash**: Initial investment amount
    - **strategy**: Name of the strategy to optimize
    - **param_ranges**: Parameter ranges for optimization (e.g., {"short_window": [5, 15]})
    - **method**: Optimization method ("grid" or "sambo")
    - **maximize**: Metric to maximize (default: "SQN")
    - **max_tries**: Maximum number of attempts
    """
    try:
        logger.info(f"Optimization API started: {request.ticker}, {request.strategy}")
        
        service = BacktestService()
        # Run optimization
        result = await service.optimize_strategy(request)
        
        logger.info(f"Optimization API completed: {request.ticker}")
        return result
        
    except ValueError as e:
        logger.error(f"Optimization request error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        logger.error(f"Optimization execution error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An error occurred during optimization execution."
        )


@router.get(
    "/targets",
    summary="List of Optimizable Metrics",
    description="Returns a list of performance metrics that can be used as optimization targets."
)
async def get_optimization_targets():
    """
    Optimization Target Metrics API
    
    Returns the metrics from backtest results that can be used as optimization targets.
    """
    try:
        targets = {
            "SQN": {
                "name": "System Quality Number",
                "description": "System Quality Index - indicates the overall quality of the strategy",
                "higher_better": True
            },
            "Return [%]": {
                "name": "Total Return",
                "description": "Total rate of return",
                "higher_better": True
            },
            "Sharpe Ratio": {
                "name": "Sharpe Ratio",
                "description": "Sharpe Ratio - risk-adjusted return",
                "higher_better": True
            },
            "Sortino Ratio": {
                "name": "Sortino Ratio",
                "description": "Sortino Ratio - downside risk-adjusted return",
                "higher_better": True
            },
            "Calmar Ratio": {
                "name": "Calmar Ratio",
                "description": "Calmar Ratio - annual return relative to maximum drawdown",
                "higher_better": True
            },
            "Profit Factor": {
                "name": "Profit Factor",
                "description": "Profit Factor - total profit versus total loss",
                "higher_better": True
            },
            "Win Rate [%]": {
                "name": "Win Rate",
                "description": "Win Rate - percentage of profitable trades",
                "higher_better": True
            },
            "Max. Drawdown [%]": {
                "name": "Maximum Drawdown",
                "description": "Maximum Drawdown (negative)",
                "higher_better": False
            }
        }
        
        return {
            "targets": targets,
            "default": "SQN",
            "recommended": ["SQN", "Sharpe Ratio", "Calmar Ratio"]
        }
        
    except Exception as e:
        logger.error(f"Error fetching optimization targets: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An error occurred while fetching optimization targets."
        )


@router.get(
    "/methods",
    summary="List of Available Optimization Methods",
    description="Returns a list of methods that can be used for parameter optimization."
)
async def get_optimization_methods():
    """
    Optimization Methods API
    
    Returns information about available optimization algorithms.
    """
    try:
        methods = {
            "grid": {
                "name": "Grid Search",
                "description": "Grid Search - systematically tests all parameter combinations",
                "pros": ["Exhaustive search", "Reproducible", "Easy to understand"],
                "cons": ["Computationally expensive", "Sensitive to the number of parameters"],
                "best_for": "Cases with a small number of parameters where exact results are needed"
            },
            "sambo": {
                "name": "SAMBO Optimization",
                "description": "Model-based optimization - Bayesian optimization algorithm",
                "pros": ["Fast convergence", "Efficient", "Can handle high-dimensional parameters"],
                "cons": ["Probabilistic results", "Complex algorithm"],
                "best_for": "Cases with a large number of parameters or when fast results are needed"
            }
        }
        
        return {
            "methods": methods,
            "default": "grid",
            "recommended": "sambo"
        }
        
    except Exception as e:
        logger.error(f"Error fetching optimization methods: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An error occurred while fetching optimization methods."
        )