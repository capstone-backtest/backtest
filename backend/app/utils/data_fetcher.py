"""
주식 데이터 수집 유틸리티
"""
import yfinance as yf
import pandas as pd
import numpy as np
from datetime import datetime, date
from typing import Optional
import logging
from pathlib import Path
import os

logger = logging.getLogger(__name__)


class DataFetcher:
    """주식 데이터 수집 클래스"""

    def __init__(self, cache_dir: str = ""):
        """
        Args:
            cache_dir: 데이터 캐시 디렉토리
        """
        # CSV 캐시 비활성화: 디렉토리 유지하되 사용하지 않습니다.
        # (디렉토리는 만들지 않음 — 파일 I/O를 완전히 비활성화합니다)
        self.cache_dir = Path(cache_dir) if cache_dir else None
    
    def get_stock_data(
        self,
        ticker: str,
        start_date: date,
        end_date: date,
        use_cache: bool = True,
        cache_hours: int = 24
    ) -> pd.DataFrame:
        """
        주식 데이터를 가져옵니다.
        
        Args:
            ticker: 주식 티커 심볼
            start_date: 시작 날짜
            end_date: 종료 날짜
            use_cache: 캐시 사용 여부
            cache_hours: 캐시 유효 시간 (시간)
            
        Returns:
            OHLCV 데이터프레임
        """
        try:
            # 티커를 대문자로 변환
            ticker = ticker.upper()
            
            # CSV 캐시 비활성화: 캐시 파일을 사용하지 않습니다.
            
            # Yahoo Finance에서 데이터 다운로드
            logger.info(f"Yahoo Finance에서 데이터 다운로드: {ticker}")
            
            # 날짜를 문자열로 변환 (yfinance 호환성)
            start_str = start_date.strftime('%Y-%m-%d')
            end_str = (end_date + pd.Timedelta(days=1)).strftime('%Y-%m-%d')  # 종료일 포함
            
            # yfinance 객체 생성
            stock = yf.Ticker(ticker)
            
            # 데이터 다운로드 시도
            data = None
            error_messages = []

            def _try_download(s_str, e_str, method='history'):
                nonlocal data
                try:
                    if method == 'history':
                        d = stock.history(start=s_str, end=e_str, auto_adjust=True, prepost=False)
                    else:
                        d = yf.download(ticker, start=s_str, end=e_str, auto_adjust=True, prepost=False, progress=False, threads=False)
                    if d is not None and not d.empty:
                        data = d
                        logger.info(f"{method}로 데이터 수집 성공: {ticker} ({s_str} -> {e_str})")
                        return True
                except Exception as e:
                    error_messages.append(f"{method} 실패: {e}")
                    logger.warning(f"{method} 실패: {e}")
                return False

            # 시도 1: 요청 범위
            _try_download(start_str, end_str, method='history')
            if data is None or data.empty:
                _try_download(start_str, end_str, method='download')

            # 시도 2: 범위 확장 재시도 (+/- 3일, +/-7일)
            if data is None or data.empty:
                try:
                    s_dt = datetime.strptime(start_str, '%Y-%m-%d') - pd.Timedelta(days=3)
                    e_dt = datetime.strptime(end_str, '%Y-%m-%d') + pd.Timedelta(days=3)
                    s2 = s_dt.strftime('%Y-%m-%d')
                    e2 = e_dt.strftime('%Y-%m-%d')
                    logger.info(f"데이터가 없음: 범위를 확장해 재시도 (+/-3일): {s2} -> {e2}")
                    _try_download(s2, e2, method='history') or _try_download(s2, e2, method='download')
                except Exception as e:
                    logger.warning(f"범위 확장 +/-3일 재시도 실패: {e}")

            if data is None or data.empty:
                try:
                    s_dt = datetime.strptime(start_str, '%Y-%m-%d') - pd.Timedelta(days=7)
                    e_dt = datetime.strptime(end_str, '%Y-%m-%d') + pd.Timedelta(days=7)
                    s3 = s_dt.strftime('%Y-%m-%d')
                    e3 = e_dt.strftime('%Y-%m-%d')
                    logger.info(f"데이터가 없음: 범위를 확장해 재시도 (+/-7일): {s3} -> {e3}")
                    _try_download(s3, e3, method='history') or _try_download(s3, e3, method='download')
                except Exception as e:
                    logger.warning(f"범위 확장 +/-7일 재시도 실패: {e}")
            
            # 데이터 검증
            if data is None or data.empty:
                error_msg = f"티커 '{ticker}'에 대한 데이터를 찾을 수 없습니다. 오류: {'; '.join(error_messages)}"
                raise ValueError(error_msg)
            
            # MultiIndex 컬럼 처리 (yfinance는 때때로 MultiIndex를 반환)
            logger.info(f"원본 컬럼 구조: {data.columns}, 타입: {type(data.columns)}")
            
            if isinstance(data.columns, pd.MultiIndex):
                # MultiIndex인 경우 첫 번째 레벨만 사용
                data.columns = data.columns.get_level_values(0)
                logger.info(f"MultiIndex 처리 후 컬럼: {data.columns}")
            
            # 컬럼 이름 정리 (공백 제거)
            data.columns = [str(col).replace(' ', '') for col in data.columns]
            logger.info(f"정리된 컬럼: {data.columns.tolist()}")
            
            # 필요한 컬럼 확인 및 선택
            required_columns = ['Open', 'High', 'Low', 'Close', 'Volume']
            available_columns = data.columns.tolist()
            missing_columns = [col for col in required_columns if col not in available_columns]
            
            logger.info(f"필요한 컬럼: {required_columns}")
            logger.info(f"사용 가능한 컬럼: {available_columns}")
            logger.info(f"누락된 컬럼: {missing_columns}")
            
            if missing_columns:
                logger.warning(f"누락된 컬럼: {missing_columns}")
                # 누락된 컬럼이 있어도 최소한 Close가 있으면 진행
                if 'Close' not in available_columns:
                    raise ValueError(f"필수 컬럼 'Close'가 없습니다. 사용 가능한 컬럼: {available_columns}")
                
                # 누락된 컬럼을 Close 값으로 대체
                for col in missing_columns:
                    if col in ['Open', 'High', 'Low']:
                        data[col] = data['Close']
                        logger.info(f"컬럼 '{col}'을 Close 값으로 대체")
                    elif col == 'Volume':
                        data[col] = 0
                        logger.info(f"컬럼 '{col}'을 0으로 설정")
            
            # 컬럼 순서 맞추기
            data = data[required_columns]
            
            # NaN 값 및 무한대 값 처리
            data = data.replace([np.inf, -np.inf], np.nan)
            data = data.dropna()
            
            if data.empty:
                raise ValueError(f"유효한 데이터가 없습니다: {ticker}")
            
            # 날짜 범위 확인
            if len(data) < 5:
                logger.warning(f"데이터가 너무 적습니다: {ticker}, {len(data)} 레코드")
            
            # CSV 캐시 비활성화: 파일로 저장하지 않습니다.
            
            logger.info(f"데이터 수집 완료: {ticker}, {len(data)} 레코드")
            return data
            
        except Exception as e:
            logger.error(f"데이터 수집 실패: {ticker}, {str(e)}")
            raise ValueError(f"데이터 수집 실패: {ticker} - {str(e)}")
    
    def validate_ticker(self, ticker: str) -> bool:
        """
        티커 유효성 검증
        
        Args:
            ticker: 검증할 티커
            
        Returns:
            유효성 여부
        """
        try:
            ticker = ticker.upper()
            stock = yf.Ticker(ticker)
            
            # 기본 정보 조회 시도
            info = stock.info
            
            # 최소한의 유효성 확인
            if info and (
                'regularMarketPrice' in info or 
                'previousClose' in info or
                'currentPrice' in info or
                len(info) > 5  # 기본적인 정보가 있는지 확인
            ):
                return True
                
            # 정보가 부족하면 실제 데이터 조회 시도
            hist = stock.history(period="5d")
            return not hist.empty
            
        except Exception as e:
            logger.error(f"티커 검증 실패: {ticker}, {e}")
            return False
    
    def get_ticker_info(self, ticker: str) -> dict:
        """
        티커 정보 조회
        
        Args:
            ticker: 티커 심볼
            
        Returns:
            티커 정보 딕셔너리
        """
        try:
            ticker = ticker.upper()
            stock = yf.Ticker(ticker)
            info = stock.info
            
            # 기본 정보 추출
            result = {
                'symbol': ticker,
                'company_name': info.get('longName', info.get('shortName', ticker)),
                'sector': info.get('sector', 'Unknown'),
                'industry': info.get('industry', 'Unknown'),
                'market_cap': info.get('marketCap', None),
                'current_price': info.get('regularMarketPrice', info.get('previousClose', None)),
                'currency': info.get('currency', 'USD'),
                'exchange': info.get('exchange', 'Unknown'),
                'country': info.get('country', 'Unknown')
            }
            
            return result
            
        except Exception as e:
            logger.error(f"티커 정보 조회 실패: {ticker}, {str(e)}")
            return {
                'symbol': ticker, 
                'error': str(e),
                'company_name': ticker,
                'sector': 'Unknown',
                'industry': 'Unknown'
            }
    
    def clear_cache(self, ticker: Optional[str] = None):
        """
        캐시 삭제
        
        Args:
            ticker: 특정 티커의 캐시만 삭제 (None이면 전체 삭제)
        """
    # 캐시 사용이 비활성화되어 있어 삭제할 파일이 없습니다.
    logger.info("CSV 캐시 비활성화: clear_cache는 동작하지 않습니다.")


# 글로벌 인스턴스
data_fetcher = DataFetcher() 