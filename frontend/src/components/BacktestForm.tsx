import React from 'react';
import { Row, Col, Form, Button, Spinner } from 'react-bootstrap';

const BacktestForm: React.FC<any> = ({ backtestParams, setBacktestParams, runBacktest, loading }) => {
  const strategyDefaults: Record<string, any> = {
    'buy_and_hold': {},
    'sma_crossover': { short_window: 10, long_window: 20 },
    'rsi_strategy': { rsi_period: 14, rsi_upper: 70, rsi_lower: 30 },
    'bollinger_bands': { period: 20, std_dev: 2.0 },
    'macd_strategy': { fast_period: 12, slow_period: 26, signal_period: 9 }
  };

  const handleParamChange = (key: string, value: any) => {
    setBacktestParams((prev: any) => ({
      ...prev,
      [key]: value,
      ...(key === 'strategy' && { strategy_params: strategyDefaults[value] || {} })
    }));
  };

  // strategy-specific param inputs are currently not rendered in this simplified form.

  return (
    <Form onSubmit={(e) => { e.preventDefault(); runBacktest(); }}>
      <Row>
        <Col md={3}>
          <Form.Group className="mb-3">
            <Form.Label>주식 티커</Form.Label>
            <Form.Control type="text" value={backtestParams.ticker} onChange={(e) => handleParamChange('ticker', e.target.value.toUpperCase())} disabled={loading} />
            <Form.Text className="text-muted">예: AAPL, GOOGL</Form.Text>
          </Form.Group>
        </Col>
        <Col md={3}>
          <Form.Group className="mb-3">
            <Form.Label>시작 날짜</Form.Label>
            <Form.Control type="date" value={backtestParams.start_date} onChange={(e) => handleParamChange('start_date', e.target.value)} disabled={loading} />
          </Form.Group>
        </Col>
        <Col md={3}>
          <Form.Group className="mb-3">
            <Form.Label>종료 날짜</Form.Label>
            <Form.Control type="date" value={backtestParams.end_date} onChange={(e) => handleParamChange('end_date', e.target.value)} disabled={loading} />
          </Form.Group>
        </Col>
        <Col md={3}>
          <Form.Group className="mb-3">
            <Form.Label>초기 투자금 ($)</Form.Label>
            <Form.Control type="number" value={backtestParams.initial_cash} onChange={(e) => handleParamChange('initial_cash', parseFloat(e.target.value) || 0)} disabled={loading} />
          </Form.Group>
        </Col>
      </Row>

      <Form.Group className="mb-3">
        <Form.Label>전략</Form.Label>
        <Form.Select value={backtestParams.strategy} onChange={(e) => handleParamChange('strategy', e.target.value)} disabled={loading}>
          <option value="buy_and_hold">Buy & Hold</option>
          <option value="sma_crossover">SMA Crossover</option>
          <option value="rsi_strategy">RSI Strategy</option>
          <option value="bollinger_bands">Bollinger Bands</option>
          <option value="macd_strategy">MACD Strategy</option>
        </Form.Select>
      </Form.Group>

      {/* strategy params slot (caller may render custom inputs) */}
      <div className="d-flex gap-2 flex-wrap">
        <Button type="submit" variant="primary" size="lg" disabled={loading}>
          {loading ? (<><Spinner animation="border" size="sm" className="me-2" />분석 중...</>) : '백테스트 실행'}
        </Button>
      </div>
    </Form>
  );
};

export default BacktestForm;
