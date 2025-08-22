import { useState, useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Row, Col, Card, Button, Spinner, Alert } from 'react-bootstrap';
import { ChartDataResponse } from './types/api';

// components
import BacktestForm from './components/BacktestForm';
import OHLCChart from './components/OHLCChart';
import EquityChart from './components/EquityChart';
import TradesChart from './components/TradesChart';
import StatsSummary from './components/StatsSummary';

// API 호출 함수
const fetchChartData = async (params: {
  ticker: string;
  start_date: string;
  end_date: string;
  initial_cash: number;
  strategy: string;
  strategy_params?: any;
}): Promise<ChartDataResponse> => {
  const response = await fetch('/api/v2/backtest/chart-data', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(params),
  });
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  return response.json();
};

// 커스텀 툴팁 컴포넌트
// UI 컴포넌트들은 분리된 파일(`./components/*`)에서 가져와 사용합니다.

// 메인 App 컴포넌트 (간결화: 컴포넌트로 분리된 UI 사용)
function App() {
  const [chartData, setChartData] = useState<ChartDataResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [backtestParams, setBacktestParams] = useState({
    ticker: 'AAPL',
    start_date: '2023-01-01',
    end_date: '2023-12-31',
    initial_cash: 10000,
    strategy: 'buy_and_hold',
    strategy_params: {} as any
  });

  const runBacktest = async (params = backtestParams) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchChartData(params);
      setChartData(data);
    } catch (err:any) {
      setError(err?.message || String(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { runBacktest(); }, []);

  // BacktestForm handles parameter changes via props; keep App focused on orchestration.

  if (error) {
    return (
      <Container className="mt-4">
        <Alert variant="danger">
          <Alert.Heading>오류 발생</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => setError(null)}>다시 시도</Button>
        </Alert>
      </Container>
    );
  }

  return (
    <Container fluid className="py-4" style={{ backgroundColor: '#f8f9fa' }}>
      <Row className="mb-4">
        <Col>
          <div className="text-center">
            <h1 className="display-4 fw-bold text-primary mb-3">🔬 백테스팅 분석 도구</h1>
            <p className="lead text-muted">과거 데이터로 투자 전략의 성과를 분석해보세요</p>
          </div>
        </Col>
      </Row>

      <Card className="mb-4 shadow">
        <Card.Header className="bg-dark text-white"><h5 className="mb-0">⚙️ 백테스트 설정</h5></Card.Header>
        <Card.Body>
          <BacktestForm backtestParams={backtestParams} setBacktestParams={setBacktestParams} runBacktest={runBacktest} loading={loading} />
        </Card.Body>
      </Card>

      {loading && (
        <div className="text-center my-5">
          <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }} />
          <h4 className="mt-3">백테스트 실행 중...</h4>
          <p className="text-muted">{backtestParams.ticker} 데이터를 분석하고 있습니다</p>
        </div>
      )}

      {chartData && !loading && (
        <>
          <Card className="mb-4 bg-light">
            <Card.Body className="text-center">
              <h2 className="text-primary mb-2">📊 {chartData.ticker} - {chartData.strategy} 백테스트 결과</h2>
              <p className="text-muted mb-0">{chartData.start_date} ~ {chartData.end_date} | 초기 투자금: ${backtestParams.initial_cash.toLocaleString()}</p>
            </Card.Body>
          </Card>

          <StatsSummary stats={chartData.summary_stats} />

          <Row>
            <Col lg={12}>
              <OHLCChart data={chartData.ohlc_data} indicators={chartData.indicators} trades={chartData.trade_markers} />
            </Col>
            <Col lg={12}>
              <EquityChart data={chartData.equity_data} />
            </Col>
            {chartData.trade_markers.length > 0 && (
              <Col lg={12}><TradesChart trades={chartData.trade_markers} /></Col>
            )}
          </Row>
        </>
      )}

      {!chartData && !loading && !error && (
        <div className="text-center my-5">
          <div style={{ fontSize: '4rem' }}>📈</div>
          <h3 className="mt-3">백테스팅을 시작하세요</h3>
          <p className="text-muted">위의 폼에서 티커와 기간을 설정한 후 백테스트를 실행해보세요.</p>
        </div>
      )}
    </Container>
  );
}

export default App;