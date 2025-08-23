import React, { useState, useEffect } from 'react';
import { Form, Row, Col, Button, Table, Card, Container, Alert } from 'react-bootstrap';
import { UnifiedBacktestRequest } from '../types/api';

interface Stock {
  symbol: string;
  amount: number;
  investmentType: 'lump_sum' | 'dca'; // 일시불 vs 분할매수
  dcaPeriods?: number; // 분할매수 기간 (개월)
}

interface UnifiedBacktestFormProps {
  onSubmit: (request: UnifiedBacktestRequest) => Promise<void>;
  loading?: boolean;
}

const UnifiedBacktestForm: React.FC<UnifiedBacktestFormProps> = ({ onSubmit, loading = false }) => {
  const [portfolio, setPortfolio] = useState<Stock[]>([{ 
    symbol: '', 
    amount: 10000, 
    investmentType: 'lump_sum',
    dcaPeriods: 12 
  }]);
  const [startDate, setStartDate] = useState('2023-01-01');
  const [endDate, setEndDate] = useState('2024-12-31');
  const [selectedStrategy, setSelectedStrategy] = useState('buy_and_hold');
  const [strategyParams, setStrategyParams] = useState<Record<string, any>>({});
  const [rebalanceFrequency, setRebalanceFrequency] = useState('monthly');
  const [commission, setCommission] = useState(0.002);
  const [errors, setErrors] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // 전략별 파라미터 정의
  const strategyConfigs = {
    buy_and_hold: { parameters: {} },
    sma_crossover: {
      parameters: {
        short_window: { type: 'int', default: 10, min: 5, max: 50 },
        long_window: { type: 'int', default: 20, min: 10, max: 100 }
      }
    },
    rsi_strategy: {
      parameters: {
        rsi_period: { type: 'int', default: 14, min: 5, max: 30 },
        rsi_oversold: { type: 'int', default: 30, min: 10, max: 40 },
        rsi_overbought: { type: 'int', default: 70, min: 60, max: 90 }
      }
    }
  };

  // 전략 변경 시 기본값 설정
  useEffect(() => {
    const config = strategyConfigs[selectedStrategy as keyof typeof strategyConfigs];
    if (config && config.parameters) {
      const defaultParams: Record<string, any> = {};
      Object.entries(config.parameters).forEach(([key, param]) => {
        defaultParams[key] = (param as any).default;
      });
      setStrategyParams(defaultParams);
    } else {
      setStrategyParams({});
    }
  }, [selectedStrategy]);

  const validatePortfolio = (): string[] => {
    const validationErrors: string[] = [];

    if (portfolio.length === 0) {
      validationErrors.push('포트폴리오는 최소 1개 종목을 포함해야 합니다.');
      return validationErrors;
    }

    if (portfolio.length > 10) {
      validationErrors.push('포트폴리오는 최대 10개 종목까지 포함할 수 있습니다.');
    }

    // 심볼 중복 검사 (CUSTOM 제외)
    const validSymbols = portfolio
      .filter(stock => stock.symbol && stock.symbol !== 'CUSTOM')
      .map(stock => stock.symbol.toUpperCase());
    const uniqueSymbols = new Set(validSymbols);
    if (validSymbols.length !== uniqueSymbols.size) {
      validationErrors.push('중복된 종목이 있습니다.');
    }

    // 빈 심볼 검사 (CUSTOM 선택 후 미입력 제외)
    const emptySymbols = portfolio.filter(stock => !stock.symbol.trim() || stock.symbol === 'CUSTOM');
    if (emptySymbols.length > 0) {
      validationErrors.push('모든 종목 심볼을 입력해주세요.');
    }

    // 투자 금액 검사
    const invalidAmounts = portfolio.filter(stock => stock.amount <= 0);
    if (invalidAmounts.length > 0) {
      validationErrors.push('모든 투자 금액은 0보다 커야 합니다.');
    }

    // 총 투자 금액 검사
    const totalAmount = portfolio.reduce((sum, stock) => sum + stock.amount, 0);
    if (totalAmount <= 0) {
      validationErrors.push('총 투자 금액은 0보다 커야 합니다.');
    }

    return validationErrors;
  };

  const addStock = () => {
    setPortfolio([...portfolio, { 
      symbol: '', 
      amount: 10000, 
      investmentType: 'lump_sum',
      dcaPeriods: 12 
    }]);
  };

  const removeStock = (index: number) => {
    const newPortfolio = portfolio.filter((_, i) => i !== index);
    setPortfolio(newPortfolio);
  };

  const updateStock = (index: number, field: keyof Stock, value: string | number) => {
    const newPortfolio = [...portfolio];
    if (field === 'symbol') {
      const symbolValue = (value as string).toUpperCase();
      if (symbolValue === 'CUSTOM') {
        // CUSTOM 선택 시 임시로 빈 문자열 설정
        newPortfolio[index].symbol = '';
      } else {
        newPortfolio[index].symbol = symbolValue;
      }
    } else if (field === 'amount') {
      newPortfolio[index].amount = Number(value);
    } else if (field === 'investmentType') {
      newPortfolio[index].investmentType = value as 'lump_sum' | 'dca';
    } else if (field === 'dcaPeriods') {
      newPortfolio[index].dcaPeriods = Number(value);
    }
    setPortfolio(newPortfolio);
  };

  const distributeEqually = () => {
    const totalAmount = portfolio.reduce((sum, stock) => sum + stock.amount, 0);
    const averageAmount = totalAmount / portfolio.length;
    const distributedPortfolio = portfolio.map(stock => ({
      ...stock,
      amount: averageAmount
    }));
    setPortfolio(distributedPortfolio);
  };

  const updateStrategyParam = (key: string, value: any) => {
    setStrategyParams(prev => ({
      ...prev,
      [key]: value
    }));
  };

  const generateStrategyParams = () => {
    const config = strategyConfigs[selectedStrategy as keyof typeof strategyConfigs];
    if (!config || !config.parameters) return {};

    const params: Record<string, any> = {};
    Object.entries(config.parameters).forEach(([key, paramConfig]) => {
      const value = strategyParams[key];
      if (value !== undefined) {
        params[key] = (paramConfig as any).type === 'int' ? parseInt(value) : value;
      }
    });
    return params;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setErrors([]);

    const validationErrors = validatePortfolio();
    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      setIsLoading(false);
      return;
    }

    try {
      // 포트폴리오 데이터 준비
      const portfolioData = portfolio.map(stock => ({
        symbol: stock.symbol === 'CASH' ? 'CASH' : stock.symbol.toUpperCase(),
        amount: stock.amount,
        investment_type: stock.investmentType,
        dca_periods: stock.dcaPeriods
      }));

      const params = generateStrategyParams();
      console.log('Portfolio data being sent:', portfolioData);
      console.log('Strategy params being sent:', params);

      await onSubmit({
        portfolio: portfolioData,
        start_date: startDate,
        end_date: endDate,
        strategy: selectedStrategy || 'buy_and_hold',
        strategy_params: params,
        commission: commission,
        rebalance_frequency: rebalanceFrequency
      });
    } catch (error) {
      console.error('백테스트 실행 중 오류:', error);
      setErrors(['백테스트 실행 중 오류가 발생했습니다.']);
    } finally {
      setIsLoading(false);
    }
  };

  const renderStrategyParams = () => {
    const config = strategyConfigs[selectedStrategy as keyof typeof strategyConfigs];
    if (!config || !config.parameters || Object.keys(config.parameters).length === 0) return null;

    return (
      <Row className="mb-4">
        <Col>
          <h5>전략 파라미터</h5>
          <Row>
            {Object.entries(config.parameters).map(([key, paramConfig]) => {
              const param = paramConfig as any;
              return (
                <Col md={6} key={key}>
                  <Form.Group className="mb-3">
                    <Form.Label>
                      {key === 'short_window' ? '단기 이동평균 기간' :
                       key === 'long_window' ? '장기 이동평균 기간' :
                       key === 'rsi_period' ? 'RSI 기간' :
                       key === 'rsi_oversold' ? 'RSI 과매도 기준' :
                       key === 'rsi_overbought' ? 'RSI 과매수 기준' : key}
                    </Form.Label>
                    <Form.Control
                      type="number"
                      value={strategyParams[key] || param.default}
                      onChange={(e) => updateStrategyParam(key, e.target.value)}
                      min={param.min}
                      max={param.max}
                    />
                    <Form.Text className="text-muted">
                      기본값: {param.default}, 범위: {param.min} - {param.max}
                    </Form.Text>
                  </Form.Group>
                </Col>
              );
            })}
          </Row>
        </Col>
      </Row>
    );
  };

  const totalAmount = portfolio.reduce((sum, stock) => sum + stock.amount, 0);

  return (
    <Container>
      <Card>
        <Card.Header>
          <h4> 포트폴리오 백테스트</h4>
          <p className="mb-0 text-muted">주식과 현금으로 구성된 포트폴리오 백테스트를 실행합니다. 현금(CASH)을 포함하여 리밸런싱 전략을 테스트할 수 있습니다.</p>
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
            {/* 포트폴리오 구성 */}
            <Row className="mb-4">
              <Col>
                <h5>포트폴리오 구성</h5>
                <div className="mb-3">
                  <small className="text-muted">
                    💡 <strong>현금(CASH)</strong>을 포함하여 포트폴리오를 구성할 수 있습니다. 
                    예: 현금 50%, AAPL 50% - 리밸런싱 시 이 비율을 유지합니다.<br/>
                    📈 <strong>분할 매수(DCA)</strong>: 총 투자금을 여러 개월에 나눠서 투자하는 방식입니다. 
                    예: $5,000을 12개월 → 매달 $416씩 투자
                  </small>
                </div>
                <Table striped bordered hover>
                  <thead>
                    <tr>
                      <th>종목/자산</th>
                      <th>투자 금액 ($)</th>
                      <th>투자 방식</th>
                      <th>비중 (%)</th>
                      <th>작업</th>
                    </tr>
                  </thead>
                  <tbody>
                    {portfolio.map((stock, index) => (
                      <tr key={index}>
                        <td>
                          <Form.Select
                            value={stock.symbol || ''}
                            onChange={(e) => updateStock(index, 'symbol', e.target.value)}
                          >
                            <option value="">종목 선택...</option>
                            <option value="CASH">현금 (CASH)</option>
                            <optgroup label="주요 종목">
                              <option value="AAPL">Apple (AAPL)</option>
                              <option value="MSFT">Microsoft (MSFT)</option>
                              <option value="GOOGL">Alphabet (GOOGL)</option>
                              <option value="AMZN">Amazon (AMZN)</option>
                              <option value="TSLA">Tesla (TSLA)</option>
                              <option value="NVDA">NVIDIA (NVDA)</option>
                              <option value="META">Meta (META)</option>
                              <option value="SPY">S&P 500 ETF (SPY)</option>
                              <option value="QQQ">NASDAQ ETF (QQQ)</option>
                            </optgroup>
                            <optgroup label="직접 입력">
                              <option value="CUSTOM">직접 입력...</option>
                            </optgroup>
                          </Form.Select>
                          {stock.symbol === 'CUSTOM' && (
                            <Form.Control
                              type="text"
                              value=""
                              onChange={(e) => updateStock(index, 'symbol', e.target.value.toUpperCase())}
                              placeholder="종목 심볼 입력 (예: AAPL)"
                              maxLength={10}
                              className="mt-2"
                            />
                          )}
                        </td>
                        <td>
                          <Form.Control
                            type="number"
                            value={stock.amount}
                            onChange={(e) => updateStock(index, 'amount', e.target.value)}
                            step="1"
                            min="1"
                          />
                        </td>
                        <td>
                          {stock.symbol === 'CASH' ? (
                            <small className="text-muted">현금 보유</small>
                          ) : (
                            <div>
                              <Form.Select
                                value={stock.investmentType}
                                onChange={(e) => updateStock(index, 'investmentType', e.target.value)}
                                size="sm"
                              >
                                <option value="lump_sum">일시불 투자</option>
                                <option value="dca">분할 매수 (DCA)</option>
                              </Form.Select>
                              {stock.investmentType === 'dca' && (
                                <Form.Control
                                  type="number"
                                  value={stock.dcaPeriods || 12}
                                  onChange={(e) => updateStock(index, 'dcaPeriods', e.target.value)}
                                  min="1"
                                  max="60"
                                  size="sm"
                                  className="mt-1"
                                  placeholder="개월"
                                />
                              )}
                              {stock.investmentType === 'dca' && (
                                <small className="text-muted">
                                  월 ${(stock.amount / (stock.dcaPeriods || 12)).toLocaleString('en-US', {
                                    minimumFractionDigits: 0,
                                    maximumFractionDigits: 0
                                  })}씩 {stock.dcaPeriods || 12}개월
                                </small>
                              )}
                            </div>
                          )}
                        </td>
                        <td>
                          {totalAmount > 0 ? ((stock.amount / totalAmount) * 100).toFixed(1) : 0}%
                          {stock.symbol === 'CASH' && (
                            <span className="text-muted ms-1">(현금)</span>
                          )}
                        </td>
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
                  <tfoot>
                    <tr>
                      <th>합계</th>
                      <th>${totalAmount.toLocaleString()}</th>
                      <th>-</th>
                      <th>100.0%</th>
                      <th></th>
                    </tr>
                  </tfoot>
                </Table>

                <div className="d-flex gap-2 mb-3">
                  <Button variant="outline-primary" onClick={addStock} disabled={portfolio.length >= 10}>
                    + 종목 추가
                  </Button>
                  {portfolio.length > 1 && (
                    <Button variant="outline-secondary" onClick={distributeEqually}>
                      균등 분배
                    </Button>
                  )}
                </div>
              </Col>
            </Row>

            {/* 백테스트 설정 */}
            <Row className="mb-4">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>시작 날짜</Form.Label>
                  <Form.Control
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>종료 날짜</Form.Label>
                  <Form.Control
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row className="mb-4">
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>투자 전략</Form.Label>
                  <Form.Select
                    value={selectedStrategy}
                    onChange={(e) => setSelectedStrategy(e.target.value)}
                  >
                    <option value="buy_and_hold">매수 후 보유</option>
                    <option value="sma_crossover">단순이동평균 교차</option>
                    <option value="rsi_strategy">RSI 전략</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>리밸런싱 주기</Form.Label>
                  <Form.Select
                    value={rebalanceFrequency}
                    onChange={(e) => setRebalanceFrequency(e.target.value)}
                  >
                    <option value="never">리밸런싱 안함</option>
                    <option value="monthly">매월</option>
                    <option value="quarterly">분기별</option>
                    <option value="yearly">연간</option>
                  </Form.Select>
                  <Form.Text className="text-muted">
                    포트폴리오 비중을 다시 맞추는 주기
                  </Form.Text>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>거래 수수료 (%)</Form.Label>
                  <Form.Control
                    type="number"
                    value={commission * 100}
                    onChange={(e) => setCommission(Number(e.target.value) / 100)}
                    step="0.01"
                    min="0"
                    max="1"
                  />
                  <Form.Text className="text-muted">
                    기본값: 0.2% (거래당)
                  </Form.Text>
                </Form.Group>
              </Col>
            </Row>

            {/* 전략 파라미터 */}
            {renderStrategyParams()}

            {/* 실행 버튼 */}
            <Row>
              <Col>
                <Button
                  type="submit"
                  variant="primary"
                  size="lg"
                  disabled={loading || isLoading}
                  className="w-100"
                >
                  {loading || isLoading ? '백테스트 실행 중...' : '백테스트 실행'}
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default UnifiedBacktestForm;
