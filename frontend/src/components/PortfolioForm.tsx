import React, { useState } from 'react';
import { Form, Button, Container, Row, Col, Card, Table, Alert } from 'react-bootstrap';

interface PortfolioStock {
  symbol: string;
  weight: number;
}

interface PortfolioBacktestRequest {
  portfolio: PortfolioStock[];
  start_date: string;
  end_date: string;
  cash: number;
  commission: number;
  rebalance_frequency: string;
}

interface PortfolioFormProps {
  onSubmit: (request: PortfolioBacktestRequest) => void;
  isLoading: boolean;
}

const PortfolioForm: React.FC<PortfolioFormProps> = ({ onSubmit, isLoading }) => {
  const [portfolio, setPortfolio] = useState<PortfolioStock[]>([
    { symbol: 'AAPL', weight: 0.4 },
    { symbol: 'GOOGL', weight: 0.3 },
    { symbol: 'MSFT', weight: 0.3 }
  ]);
  
  const [formData, setFormData] = useState({
    start_date: '2023-01-01',
    end_date: '2023-12-31',
    cash: 10000,
    commission: 0.002,
    rebalance_frequency: 'monthly'
  });

  const [errors, setErrors] = useState<string[]>([]);

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

    // 가중치 검사
    const totalWeight = portfolio.reduce((sum, stock) => sum + stock.weight, 0);
    if (Math.abs(totalWeight - 1.0) > 0.001) {
      validationErrors.push(`가중치 합계는 1.0이어야 합니다. (현재: ${totalWeight.toFixed(3)})`);
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

  const updateStock = (index: number, field: keyof PortfolioStock, value: string | number) => {
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

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const validationErrors = validatePortfolio();
    setErrors(validationErrors);

    if (validationErrors.length === 0) {
      const request: PortfolioBacktestRequest = {
        portfolio: portfolio.map(stock => ({
          symbol: stock.symbol.toUpperCase(),
          weight: stock.weight
        })),
        ...formData
      };
      onSubmit(request);
    }
  };

  const totalWeight = portfolio.reduce((sum, stock) => sum + stock.weight, 0);

  return (
    <Container>
      <Card>
        <Card.Header>
          <h4>포트폴리오 백테스트</h4>
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
                <Table striped bordered hover>
                  <thead>
                    <tr>
                      <th>종목 심볼</th>
                      <th>가중치</th>
                      <th>비중 (%)</th>
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
                        <td>{(stock.weight * 100).toFixed(1)}%</td>
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
                </Table>
                
                <div className="d-flex gap-2 mb-3">
                  <Button 
                    variant="outline-primary" 
                    onClick={addStock}
                    disabled={portfolio.length >= 10}
                  >
                    종목 추가
                  </Button>
                  <Button
                    variant="outline-secondary"
                    onClick={normalizeWeights}
                    disabled={totalWeight === 0}
                  >
                    가중치 정규화
                  </Button>
                </div>
              </Col>
            </Row>

            {/* 백테스트 설정 */}
            <Row>
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

            <Row>
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
            </Row>

            <div className="d-grid">
              <Button
                type="submit"
                variant="primary"
                size="lg"
                disabled={isLoading || errors.length > 0}
              >
                {isLoading ? '백테스트 실행 중...' : '포트폴리오 백테스트 실행'}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default PortfolioForm;
