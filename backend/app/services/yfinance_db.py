"""
MySQL 저장 유틸리티 (간단한 SQLAlchemy 사용)
"""
import os
import json
import logging
from typing import Optional
from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine
import pandas as pd
from datetime import datetime

logger = logging.getLogger(__name__)


def _get_engine() -> Engine:
    db_url = os.getenv("DATABASE_URL")
    if not db_url:
        # 기본 로컬 연결 (환경에 맞게 수정하세요)
        db_url = "mysql+pymysql://root:password@127.0.0.1:3306/stock_data_cache?charset=utf8mb4"
    engine = create_engine(db_url, pool_pre_ping=True, future=True)
    return engine


def save_ticker_data(ticker: str, df: pd.DataFrame) -> int:
    """stocks 테이블에 티커 등록 및 daily_prices에 행을 upsert 합니다.

    Returns: 저장된 행 수
    """
    engine = _get_engine()
    conn = engine.connect()
    trans = conn.begin()
    try:
        # ensure stock exists
        info = {}
        try:
            from app.utils.data_fetcher import data_fetcher
            info = data_fetcher.get_ticker_info(ticker)
        except Exception:
            logger.warning("티커 info 조회 실패")

        # insert or update stocks
        insert_stock = text(
            """
            INSERT INTO stocks (ticker, name, exchange, sector, industry, summary, info_json, last_info_update)
            VALUES (:ticker, :name, :exchange, :sector, :industry, :summary, :info_json, :now)
            ON DUPLICATE KEY UPDATE name=VALUES(name), exchange=VALUES(exchange), sector=VALUES(sector),
              industry=VALUES(industry), summary=VALUES(summary), info_json=VALUES(info_json), last_info_update=VALUES(last_info_update)
            """
        )
        now = datetime.utcnow()
        conn.execute(insert_stock, {
            "ticker": ticker,
            "name": info.get("company_name"),
            "exchange": info.get("exchange"),
            "sector": info.get("sector"),
            "industry": info.get("industry"),
            "summary": None,
            "info_json": json.dumps(info),
            "now": now
        })

        # get stock_id
        stock_id_row = conn.execute(text("SELECT id FROM stocks WHERE ticker = :t"), {"t": ticker}).fetchone()
        if not stock_id_row:
            raise RuntimeError("stock_id를 찾을 수 없습니다.")
        stock_id = stock_id_row[0]

        # prepare daily prices upsert
        rows = []
        df_proc = df.copy()
        if 'Date' in df_proc.columns:
            df_proc['date'] = pd.to_datetime(df_proc['Date']).dt.date
        else:
            df_proc = df_proc.reset_index()
            df_proc['date'] = pd.to_datetime(df_proc[df_proc.columns[0]]).dt.date

        for _, r in df_proc.iterrows():
            o = None if pd.isna(r.get('Open')) else float(r.get('Open'))
            h = None if pd.isna(r.get('High')) else float(r.get('High'))
            l = None if pd.isna(r.get('Low')) else float(r.get('Low'))
            c = None if pd.isna(r.get('Close')) else float(r.get('Close'))
            ac = None
            if 'Adj Close' in r or 'AdjClose' in r or 'Adj_Close' in r:
                ac = r.get('Adj Close') or r.get('AdjClose') or r.get('Adj_Close')
                ac = None if pd.isna(ac) else float(ac)
            vol = 0 if pd.isna(r.get('Volume')) else int(r.get('Volume'))
            rows.append({
                'stock_id': stock_id,
                'date': r['date'].isoformat(),
                'open': o,
                'high': h,
                'low': l,
                'close': c,
                'adj_close': ac,
                'volume': vol
            })

        if rows:
            # batch upsert
            insert_stmt = text(
                """
                INSERT INTO daily_prices (stock_id, date, open, high, low, close, adj_close, volume)
                VALUES (:stock_id, :date, :open, :high, :low, :close, :adj_close, :volume)
                ON DUPLICATE KEY UPDATE open=VALUES(open), high=VALUES(high), low=VALUES(low), close=VALUES(close), adj_close=VALUES(adj_close), volume=VALUES(volume)
                """
            )
            # chunking
            chunk_size = 500
            total = 0
            for i in range(0, len(rows), chunk_size):
                batch = rows[i:i+chunk_size]
                conn.execute(insert_stmt, batch)
                total += len(batch)

        trans.commit()
        return len(rows)

    except Exception as e:
        trans.rollback()
        logger.exception("save_ticker_data 실패")
        raise
    finally:
        conn.close()


def load_ticker_data(ticker: str, start_date=None, end_date=None) -> pd.DataFrame:
    """DB에서 ticker의 daily_prices를 조회해 pandas DataFrame으로 반환합니다.

    start_date/end_date는 date 또는 문자열(YYYY-MM-DD)을 받을 수 있습니다.
    반환 DataFrame은 DatetimeIndex(날짜)와 컬럼 ['Open','High','Low','Close','Adj_Close','Volume']를 가집니다.
    """
    engine = _get_engine()
    conn = engine.connect()
    try:
        # find stock_id
        row = conn.execute(text("SELECT id FROM stocks WHERE ticker = :t"), {"t": ticker}).fetchone()
        if not row:
            raise ValueError(f"티커 '{ticker}'이(가) DB에 없습니다.")
        stock_id = row[0]

        # build query
        q = "SELECT date, open, high, low, close, adj_close, volume FROM daily_prices WHERE stock_id = :sid"
        params = {"sid": stock_id}
        if start_date:
            q += " AND date >= :start"
            params["start"] = str(start_date)
        if end_date:
            q += " AND date <= :end"
            params["end"] = str(end_date)
        q += " ORDER BY date ASC"

        res = conn.execute(text(q), params)
        rows = res.fetchall()
        if not rows:
            return pd.DataFrame()

        df = pd.DataFrame(rows, columns=["date", "open", "high", "low", "close", "adj_close", "volume"])
        df['date'] = pd.to_datetime(df['date'])
        df = df.set_index('date')
        # normalize column names to expected ones
        df = df.rename(columns={
            'open': 'Open', 'high': 'High', 'low': 'Low', 'close': 'Close', 'adj_close': 'Adj Close', 'volume': 'Volume'
        })
        # ensure types
        for col in ['Open','High','Low','Close','Adj Close']:
            if col in df.columns:
                df[col] = pd.to_numeric(df[col], errors='coerce')
        if 'Volume' in df.columns:
            df['Volume'] = pd.to_numeric(df['Volume'], errors='coerce').fillna(0).astype('int64')

        return df
    finally:
        conn.close()
