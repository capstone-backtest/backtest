"""
사용자 친화적 에러 메시지 매핑
"""

ERROR_MESSAGES = {
    "NO_DATA": {
        "ko": "선택한 기간에 대한 데이터가 없습니다. 다른 기간을 선택해주세요.",
        "en": "No data available for the selected period. Please choose a different period."
    },
    "INVALID_SYMBOL": {
        "ko": "존재하지 않는 종목 심볼입니다. 올바른 심볼을 입력해주세요.",
        "en": "Invalid stock symbol. Please enter a valid symbol."
    },
    "RATE_LIMIT": {
        "ko": "잠시 후 다시 시도해주세요. (요청이 너무 많습니다)",
        "en": "Please try again later. (Too many requests)"
    },
    "DATE_RANGE_ERROR": {
        "ko": "시작 날짜는 종료 날짜보다 빨라야 합니다.",
        "en": "Start date must be earlier than end date."
    },
    "PORTFOLIO_EMPTY": {
        "ko": "포트폴리오에 최소 1개 종목을 추가해주세요.",
        "en": "Please add at least one stock to your portfolio."
    },
    "AMOUNT_INVALID": {
        "ko": "투자 금액은 0보다 커야 합니다.",
        "en": "Investment amount must be greater than 0."
    }
}

def get_user_friendly_message(error_code: str, language: str = "ko") -> str:
    """사용자 친화적 에러 메시지 반환"""
    if error_code in ERROR_MESSAGES:
        return ERROR_MESSAGES[error_code].get(language, ERROR_MESSAGES[error_code]["ko"])
    return "알 수 없는 오류가 발생했습니다."

# 백테스트 결과 해석 도우미
BACKTEST_TIPS = {
    "high_return": "🎉 훌륭한 수익률입니다! 하지만 과거 성과가 미래를 보장하지는 않습니다.",
    "high_volatility": "⚠️ 변동성이 높습니다. 리스크 관리를 고려해보세요.",
    "low_sharpe": "📊 샤프 비율이 낮습니다. 위험 대비 수익이 좋지 않을 수 있습니다.",
    "high_drawdown": "📉 최대 낙폭이 큽니다. 심리적 부담을 고려해보세요."
}
