"""
포트폴리오 백테스팅 API 테스트 스크립트
"""
import requests
import json

# 포트폴리오 백테스트 요청 데이터
portfolio_request = {
    "portfolio": [
        {
            "symbol": "AAPL",
            "weight": 0.4
        },
        {
            "symbol": "GOOGL",
            "weight": 0.3
        },
        {
            "symbol": "TSLA",
            "weight": 0.3
        }
    ],
    "start_date": "2023-01-01",
    "end_date": "2023-12-31",
    "initial_capital": 100000,
    "rebalance_frequency": "monthly"
}

def test_portfolio_backtest():
    """포트폴리오 백테스트 API 테스트"""
    try:
        url = "http://localhost:8000/api/v1/backtest/portfolio"
        
        print("포트폴리오 백테스트 API 테스트 시작...")
        print(f"요청 URL: {url}")
        print(f"요청 데이터: {json.dumps(portfolio_request, indent=2)}")
        
        response = requests.post(url, json=portfolio_request)
        
        print(f"\n응답 상태 코드: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("포트폴리오 백테스트 성공!")
            print(f"포트폴리오 구성: {result.get('portfolio_composition', 'N/A')}")
            print(f"총 수익률: {result.get('total_return_pct', 'N/A'):.2f}%")
            print(f"연간 수익률: {result.get('annual_return_pct', 'N/A'):.2f}%")
            print(f"변동성: {result.get('volatility_pct', 'N/A'):.2f}%")
            print(f"샤프 비율: {result.get('sharpe_ratio', 'N/A'):.2f}")
            print(f"최대 낙폭: {result.get('max_drawdown_pct', 'N/A'):.2f}%")
        else:
            print(f"오류 발생: {response.text}")
            
    except Exception as e:
        print(f"테스트 중 예외 발생: {e}")

if __name__ == "__main__":
    test_portfolio_backtest()
