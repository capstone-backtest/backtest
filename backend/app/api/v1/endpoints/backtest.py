"""
백테스팅 API
"""
from fastapi import APIRouter, HTTPException, status
from ....models.requests import BacktestRequest
from ....models.responses import BacktestResult, ErrorResponse, ChartDataResponse
from ....models.schemas import PortfolioBacktestRequest
from ....services.backtest_service import backtest_service
from ....services.portfolio_service import PortfolioBacktestService
import logging

logger = logging.getLogger(__name__)
router = APIRouter()
portfolio_service = PortfolioBacktestService()


@router.post(
    "/run",
    response_model=BacktestResult,
    status_code=status.HTTP_200_OK,
    summary="백테스트 실행",
    description="주어진 전략과 파라미터로 백테스트를 실행합니다."
)
async def run_backtest(request: BacktestRequest):
    """
    백테스트 실행 API
    
    - **ticker**: 주식 티커 심볼 (예: AAPL, GOOGL)
    - **start_date**: 백테스트 시작 날짜 (YYYY-MM-DD)
    - **end_date**: 백테스트 종료 날짜 (YYYY-MM-DD)
    - **initial_cash**: 초기 투자금액
    - **strategy**: 사용할 전략명
    - **strategy_params**: 전략별 파라미터 (선택사항)
    - **commission**: 거래 수수료 (기본값: 0.002)
    """
    try:
        # 요청 유효성 검증
        backtest_service.validate_backtest_request(request)
        
        # 백테스트 실행
        result = await backtest_service.run_backtest(request)
        
        logger.info(f"백테스트 API 완료: {request.ticker}")
        return result
        
    except ValueError as e:
        logger.error(f"백테스트 요청 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        logger.error(f"백테스트 실행 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="백테스트 실행 중 오류가 발생했습니다."
        )


@router.get(
    "/health",
    summary="백테스트 서비스 상태 확인",
    description="백테스트 서비스의 상태를 확인합니다."
)
async def backtest_health():
    """백테스트 서비스 헬스체크"""
    try:
        # 간단한 검증 로직
        from ....utils.data_fetcher import data_fetcher
        
        # 샘플 티커로 간단 검증
        is_healthy = data_fetcher.validate_ticker("AAPL")
        
        if is_healthy:
            return {
                "status": "healthy",
                "message": "백테스트 서비스가 정상 작동 중입니다.",
                "data_source": "Yahoo Finance 연결 정상"
            }
        else:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="데이터 소스 연결에 문제가 있습니다."
            )
            
    except Exception as e:
        logger.error(f"헬스체크 실패: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="백테스트 서비스 상태 확인 실패"
        )


@router.post(
    "/chart-data",
    response_model=ChartDataResponse,
    status_code=status.HTTP_200_OK,
    summary="백테스트 차트 데이터",
    description="백테스트 결과를 Recharts용 차트 데이터로 반환합니다."
)
async def get_chart_data(request: BacktestRequest):
    """
    백테스트 차트 데이터 API
    
    백테스트를 실행하고 결과를 Recharts 라이브러리에서 사용할 수 있는 
    JSON 형태의 차트 데이터로 반환합니다.
    
    **반환 데이터:**
    - **ohlc_data**: 캔들스틱 차트용 OHLC 데이터
    - **equity_data**: 자산 곡선 데이터
    - **trade_markers**: 거래 진입/청산 마커
    - **indicators**: 기술 지표 데이터
    - **summary_stats**: 주요 성과 지표
    
    **사용 예시 (React + Recharts):**
    ```javascript
    // 캔들스틱 차트
    <ComposedChart data={chartData.ohlc_data}>
      <XAxis dataKey="date" />
      <YAxis />
      <Bar dataKey="volume" />
      <Line dataKey="close" />
    </ComposedChart>
    
    // 자산 곡선
    <LineChart data={chartData.equity_data}>
      <Line dataKey="return_pct" stroke="#8884d8" />
      <Line dataKey="drawdown_pct" stroke="#ff0000" />
    </LineChart>
    ```
    """
    try:
        # 요청 유효성 검증
        backtest_service.validate_backtest_request(request)
        
        # 차트 데이터 생성
        chart_data = await backtest_service.generate_chart_data(request)
        
        logger.info(f"차트 데이터 API 완료: {request.ticker}, 데이터 포인트: {len(chart_data.ohlc_data)}")
        return chart_data
        
    except ValueError as e:
        logger.error(f"차트 데이터 요청 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        logger.error(f"차트 데이터 생성 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="차트 데이터 생성 중 오류가 발생했습니다."
        )


@router.post(
    "/portfolio",
    status_code=status.HTTP_200_OK,
    summary="포트폴리오 백테스트 실행",
    description="여러 종목으로 구성된 포트폴리오의 백테스트를 실행합니다."
)
async def run_portfolio_backtest(request: PortfolioBacktestRequest):
    """
    포트폴리오 백테스트 실행 API
    
    - **portfolio**: 포트폴리오 구성 (종목과 비중)
    - **start_date**: 백테스트 시작 날짜 (YYYY-MM-DD)
    - **end_date**: 백테스트 종료 날짜 (YYYY-MM-DD)
    - **cash**: 초기 투자금액
    - **commission**: 거래 수수료 (기본값: 0.002)
    - **rebalance_frequency**: 리밸런싱 주기 (monthly, quarterly, yearly)
    
    **사용 예시:**
    ```json
    {
        "portfolio": [
            {"symbol": "AAPL", "weight": 0.4},
            {"symbol": "GOOGL", "weight": 0.3},
            {"symbol": "MSFT", "weight": 0.3}
        ],
        "start_date": "2023-01-01",
        "end_date": "2023-12-31",
        "cash": 10000,
        "commission": 0.002,
        "rebalance_frequency": "monthly"
    }
    ```
    """
    try:
        # 포트폴리오 백테스트 실행
        result = await portfolio_service.run_portfolio_backtest(request)
        
        if result['status'] == 'error':
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=result['error']
            )
        
        logger.info(f"포트폴리오 백테스트 API 완료: {len(request.portfolio)} 종목")
        return result
        
    except HTTPException:
        raise
    except ValueError as e:
        logger.error(f"포트폴리오 백테스트 요청 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        logger.error(f"포트폴리오 백테스트 실행 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="포트폴리오 백테스트 실행 중 오류가 발생했습니다."
        ) 