import React, { useState, useEffect } from 'react';
import { Form, Button, Container, Row, Col, Card, Table, Alert, Tabs, Tab } from 'react-bootstrap';

interface Stock {
  symbol: string;
  weight: number;
}

interface UnifiedBacktestRequest {
  portfolio: Stock[];
  start_date: string;
  end_date: string;
  cash: number;
  commission: number;
  rebalance_frequency: string;
  strategy: string;
  strategy_params: Record<string, any>;
}

interface UnifiedBacktestFormProps {
  onSubmit: (request: UnifiedBacktestRequest) => void;
  isLoading: boolean;
}

const UnifiedBacktestForm: React.FC<UnifiedBacktestFormProps> = ({ onSubmit, isLoading }) => {
  const [activeTab, setActiveTab] = useState<'single' | 'portfolio'>('single');
  const [portfolio, setPortfolio] = useState<Stock[]>([
    { symbol: 'AAPL', weight: 1.0 }
  ]);
  
  const [formData, setFormData] = useState({
    start_date: '2023-01-01',
    end_date: '2023-12-31',
    cash: 10000,
    commission: 0.002,
    rebalance_frequency: 'monthly',
    strategy: 'buy_and_hold',
    strategy_params: {} as Record<string, any>
  });

  const [errors, setErrors] = useState<string[]>([]);

  // 전략별 기본 파라미터
  const strategyDefaults: Record<string, any> = {
    'buy_and_hold': {},
    'sma_crossover': { short_window: 10, long_window: 20 },
    'rsi_strategy': { rsi_period: 14, rsi_upper: 70, rsi_lower: 30 },
    'bollinger_bands': { period: 20, std_dev: 2.0 },
    'macd_strategy': { fast_period: 12, slow_period: 26, signal_period: 9 }
  };

  // 탭 변경 시 포트폴리오 초기화
  useEffect(() => {
    if (activeTab === 'single') {
      setPortfolio([{ symbol: 'AAPL', weight: 1.0 }]);
    } else {
      setPortfolio([
        { symbol: 'AAPL', weight: 0.4 },
        { symbol: 'GOOGL', weight: 0.3 },
        { symbol: 'MSFT', weight: 0.3 }
      ]);
    }
  }, [activeTab]);

  // 전략 변경 시 파라미터 초기화
  useEffect(() => {
    setFormData(prev => ({
      ...prev,
      strategy_params: strategyDefaults[formData.strategy] || {}
    }));
  }, [formData.strategy]);

  const validatePortfolio = (): string[] => {
    const validationErrors: string[] = [];

    if (portfolio.length === 0) {
      validationErrors.push('포트폴리오는 최소 1개 종목을 포함해야 합니다.');
      return validationErrors;
    }

    if (portfolio.length > 10) {
      validationErrors.push('포트폴리오는 최대 10개 종목까지 포함할 수 있습니다.');
    }

    // 심볼 중복 검사
    const symbols = portfolio.map(stock => stock.symbol.toUpperCase());
    const uniqueSymbols = new Set(symbols);
    if (symbols.length !== uniqueSymbols.size) {
      validationErrors.push('중복된 종목이 있습니다.');
    }

    // 빈 심볼 검사
    const emptySymbols = portfolio.filter(stock => !stock.symbol.trim());
    if (emptySymbols.length > 0) {
      validationErrors.push('모든 종목 심볼을 입력해주세요.');
    }

    // 가중치 검사 (단일 종목은 제외)
    if (activeTab === 'portfolio') {
      const totalWeight = portfolio.reduce((sum, stock) => sum + stock.weight, 0);
      if (Math.abs(totalWeight - 1.0) > 0.001) {
        validationErrors.push(`가중치 합계는 1.0이어야 합니다. (현재: ${totalWeight.toFixed(3)})`);
      }
    }

    // 개별 가중치 검사
    const invalidWeights = portfolio.filter(stock => stock.weight <= 0 || stock.weight > 1);
    if (invalidWeights.length > 0) {
      validationErrors.push('모든 가중치는 0보다 크고 1.0 이하여야 합니다.');
    }

    return validationErrors;
  };

  const addStock = () => {
    setPortfolio([...portfolio, { symbol: '', weight: 0 }]);
  };

  const removeStock = (index: number) => {
    const newPortfolio = portfolio.filter((_, i) => i !== index);
    setPortfolio(newPortfolio);
  };

  const updateStock = (index: number, field: keyof Stock, value: string | number) => {
    const newPortfolio = [...portfolio];
    if (field === 'symbol') {
      newPortfolio[index].symbol = (value as string).toUpperCase();
    } else {
      newPortfolio[index].weight = Number(value);
    }
    setPortfolio(newPortfolio);
  };

  const normalizeWeights = () => {
    const totalWeight = portfolio.reduce((sum, stock) => sum + stock.weight, 0);
    if (totalWeight > 0) {
      const normalizedPortfolio = portfolio.map(stock => ({
        ...stock,
        weight: stock.weight / totalWeight
      }));
      setPortfolio(normalizedPortfolio);
    }
  };

  const updateStrategyParam = (key: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      strategy_params: {
        ...prev.strategy_params,
        [key]: value
      }
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const validationErrors = validatePortfolio();
    setErrors(validationErrors);

    if (validationErrors.length === 0) {
      const request: UnifiedBacktestRequest = {
        portfolio: portfolio.map(stock => ({
          symbol: stock.symbol.toUpperCase(),
          weight: stock.weight
        })),
        ...formData
      };
      onSubmit(request);
    }
  };

  const renderStrategyParams = () => {
    const params = formData.strategy_params;
    
    switch (formData.strategy) {
      case 'sma_crossover':
        return (
          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>단기 이동평균 기간</Form.Label>
                <Form.Control
                  type="number"
                  value={params.short_window || 10}
                  onChange={(e) => updateStrategyParam('short_window', parseInt(e.target.value))}
                  min="2" max="200"
                />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>장기 이동평균 기간</Form.Label>
                <Form.Control
                  type="number"
                  value={params.long_window || 20}
                  onChange={(e) => updateStrategyParam('long_window', parseInt(e.target.value))}
                  min="5" max="500"
                />
              </Form.Group>
            </Col>
          </Row>
        );
      
      case 'rsi_strategy':
        return (
          <Row>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label>RSI 기간</Form.Label>
                <Form.Control
                  type="number"
                  value={params.rsi_period || 14}
                  onChange={(e) => updateStrategyParam('rsi_period', parseInt(e.target.value))}
                  min="2" max="50"
                />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label>과매수 기준</Form.Label>
                <Form.Control
                  type="number"
                  value={params.rsi_upper || 70}
                  onChange={(e) => updateStrategyParam('rsi_upper', parseInt(e.target.value))}
                  min="50" max="90"
                />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label>과매도 기준</Form.Label>
                <Form.Control
                  type="number"
                  value={params.rsi_lower || 30}
                  onChange={(e) => updateStrategyParam('rsi_lower', parseInt(e.target.value))}
                  min="10" max="50"
                />
              </Form.Group>
            </Col>
          </Row>
        );

      case 'bollinger_bands':
        return (
          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>이동평균 기간</Form.Label>
                <Form.Control
                  type="number"
                  value={params.period || 20}
                  onChange={(e) => updateStrategyParam('period', parseInt(e.target.value))}
                  min="5" max="100"
                />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>표준편차 배수</Form.Label>
                <Form.Control
                  type="number"
                  value={params.std_dev || 2.0}
                  onChange={(e) => updateStrategyParam('std_dev', parseFloat(e.target.value))}
                  step="0.1" min="0.5" max="5.0"
                />
              </Form.Group>
            </Col>
          </Row>
        );

      case 'macd_strategy':
        return (
          <Row>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label>빠른 EMA 기간</Form.Label>
                <Form.Control
                  type="number"
                  value={params.fast_period || 12}
                  onChange={(e) => updateStrategyParam('fast_period', parseInt(e.target.value))}
                  min="5" max="50"
                />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label>느린 EMA 기간</Form.Label>
                <Form.Control
                  type="number"
                  value={params.slow_period || 26}
                  onChange={(e) => updateStrategyParam('slow_period', parseInt(e.target.value))}
                  min="10" max="100"
                />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label>시그널 기간</Form.Label>
                <Form.Control
                  type="number"
                  value={params.signal_period || 9}
                  onChange={(e) => updateStrategyParam('signal_period', parseInt(e.target.value))}
                  min="5" max="50"
                />
              </Form.Group>
            </Col>
          </Row>
        );

      default:
        return null;
    }
  };

  const totalWeight = portfolio.reduce((sum, stock) => sum + stock.weight, 0);

  return (
    <Container>
      <Card>
        <Card.Header>
          <h4>📈 백테스트 실행</h4>
        </Card.Header>
        <Card.Body>
          {errors.length > 0 && (
            <Alert variant="danger">
              <ul className="mb-0">
                {errors.map((error, index) => (
                  <li key={index}>{error}</li>
                ))}
              </ul>
            </Alert>
          )}

          <Form onSubmit={handleSubmit}>
            {/* 포트폴리오 유형 선택 */}
            <Row className="mb-4">
              <Col>
                <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k as 'single' | 'portfolio')}>
                  <Tab eventKey="single" title="📊 단일 종목">
                    <div className="mt-3">
                      <p className="text-muted">하나의 종목에 대한 백테스트를 실행합니다.</p>
                    </div>
                  </Tab>
                  <Tab eventKey="portfolio" title="📈 포트폴리오">
                    <div className="mt-3">
                      <p className="text-muted">여러 종목으로 구성된 포트폴리오 백테스트를 실행합니다.</p>
                    </div>
                  </Tab>
                </Tabs>
              </Col>
            </Row>

            {/* 포트폴리오 구성 */}
            <Row className="mb-4">
              <Col>
                <h5>{activeTab === 'single' ? '종목 선택' : '포트폴리오 구성'}</h5>
                <Table striped bordered hover>
                  <thead>
                    <tr>
                      <th>종목 심볼</th>
                      {activeTab === 'portfolio' && <th>가중치</th>}
                      {activeTab === 'portfolio' && <th>비중 (%)</th>}
                      <th>작업</th>
                    </tr>
                  </thead>
                  <tbody>
                    {portfolio.map((stock, index) => (
                      <tr key={index}>
                        <td>
                          <Form.Control
                            type="text"
                            value={stock.symbol}
                            onChange={(e) => updateStock(index, 'symbol', e.target.value)}
                            placeholder="예: AAPL"
                            maxLength={10}
                          />
                        </td>
                        {activeTab === 'portfolio' && (
                          <td>
                            <Form.Control
                              type="number"
                              value={stock.weight}
                              onChange={(e) => updateStock(index, 'weight', e.target.value)}
                              step="0.001"
                              min="0"
                              max="1"
                            />
                          </td>
                        )}
                        {activeTab === 'portfolio' && (
                          <td>{(stock.weight * 100).toFixed(1)}%</td>
                        )}
                        <td>
                          <Button
                            variant="outline-danger"
                            size="sm"
                            onClick={() => removeStock(index)}
                            disabled={portfolio.length <= 1}
                          >
                            삭제
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  {activeTab === 'portfolio' && (
                    <tfoot>
                      <tr>
                        <th>합계</th>
                        <th>{totalWeight.toFixed(3)}</th>
                        <th>{(totalWeight * 100).toFixed(1)}%</th>
                        <th>
                          {Math.abs(totalWeight - 1.0) > 0.001 && (
                            <Button
                              variant="outline-warning"
                              size="sm"
                              onClick={normalizeWeights}
                            >
                              정규화
                            </Button>
                          )}
                        </th>
                      </tr>
                    </tfoot>
                  )}
                </Table>
                
                <div className="d-flex gap-2 mb-3">
                  <Button 
                    variant="outline-primary" 
                    onClick={addStock}
                    disabled={portfolio.length >= 10}
                  >
                    종목 추가
                  </Button>
                  {activeTab === 'portfolio' && (
                    <Button
                      variant="outline-secondary"
                      onClick={normalizeWeights}
                      disabled={totalWeight === 0}
                    >
                      가중치 정규화
                    </Button>
                  )}
                </div>
              </Col>
            </Row>

            {/* 기간 및 자본금 설정 */}
            <Row className="mb-4">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>시작 날짜</Form.Label>
                  <Form.Control
                    type="date"
                    value={formData.start_date}
                    onChange={(e) => setFormData({...formData, start_date: e.target.value})}
                    required
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>종료 날짜</Form.Label>
                  <Form.Control
                    type="date"
                    value={formData.end_date}
                    onChange={(e) => setFormData({...formData, end_date: e.target.value})}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row className="mb-4">
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>초기 자본금 ($)</Form.Label>
                  <Form.Control
                    type="number"
                    value={formData.cash}
                    onChange={(e) => setFormData({...formData, cash: Number(e.target.value)})}
                    min="1"
                    required
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>수수료율</Form.Label>
                  <Form.Control
                    type="number"
                    value={formData.commission}
                    onChange={(e) => setFormData({...formData, commission: Number(e.target.value)})}
                    step="0.001"
                    min="0"
                    max="0.1"
                    required
                  />
                </Form.Group>
              </Col>
              {activeTab === 'portfolio' && (
                <Col md={4}>
                  <Form.Group className="mb-3">
                    <Form.Label>리밸런싱 주기</Form.Label>
                    <Form.Select
                      value={formData.rebalance_frequency}
                      onChange={(e) => setFormData({...formData, rebalance_frequency: e.target.value})}
                    >
                      <option value="monthly">월간</option>
                      <option value="quarterly">분기</option>
                      <option value="yearly">연간</option>
                    </Form.Select>
                  </Form.Group>
                </Col>
              )}
            </Row>

            {/* 전략 설정 */}
            <Row className="mb-4">
              <Col>
                <h5>📋 전략 설정</h5>
                <Form.Group className="mb-3">
                  <Form.Label>투자 전략</Form.Label>
                  <Form.Select
                    value={formData.strategy}
                    onChange={(e) => setFormData({...formData, strategy: e.target.value})}
                  >
                    <option value="buy_and_hold">📈 Buy & Hold</option>
                    <option value="sma_crossover">📊 SMA Crossover</option>
                    <option value="rsi_strategy">📉 RSI Strategy</option>
                    <option value="bollinger_bands">📏 Bollinger Bands</option>
                    <option value="macd_strategy">🌊 MACD Strategy</option>
                  </Form.Select>
                </Form.Group>
                
                {renderStrategyParams()}
              </Col>
            </Row>

            <div className="d-grid">
              <Button
                type="submit"
                variant="primary"
                size="lg"
                disabled={isLoading || errors.length > 0}
              >
                {isLoading ? '백테스트 실행 중...' : '🚀 백테스트 실행'}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default UnifiedBacktestForm;
