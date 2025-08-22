import React from 'react';
import { Row, Col, Card, Badge, OverlayTrigger, Tooltip } from 'react-bootstrap';

const StatsSummary: React.FC<{ stats: any }> = ({ stats }) => {
  if (!stats) return null;

  const statItems = [
    { label: '총 수익률', value: `${stats.total_return_pct.toFixed(2)}%`, variant: stats.total_return_pct >= 0 ? 'success' : 'danger', description: '투자 원금 대비 총 수익률' },
    { label: '총 거래수', value: stats.total_trades.toString(), variant: 'primary', description: '전체 기간 동안 체결된 거래수' },
    { label: '승률', value: `${stats.win_rate_pct.toFixed(1)}%`, variant: 'info', description: '전체 거래 중 이익 비율' },
    { label: '최대 손실', value: `${stats.max_drawdown_pct.toFixed(2)}%`, variant: 'danger', description: '최대 Drawdown' },
    { label: '샤프', value: stats.sharpe_ratio.toFixed(3), variant: 'secondary', description: '리스크 대비 성과 지표' },
    { label: 'Profit Factor', value: stats.profit_factor.toFixed(2), variant: 'warning', description: '이익/손실 비율' }
  ];

  return (
    <div className="mb-4">
      <h4 className="mb-3">📈 백테스트 성과</h4>
      <Row>
        {statItems.map((item, index) => (
          <Col md={6} lg={4} key={index} className="mb-3">
            <OverlayTrigger placement="top" overlay={<Tooltip>{item.description}</Tooltip>}>
              <Card className="h-100 shadow-sm border-0">
                <Card.Body className="text-center">
                  <Card.Title className="fs-6 text-muted">{item.label}</Card.Title>
                  <Badge bg={item.variant} className="fs-5 px-3 py-2">{item.value}</Badge>
                </Card.Body>
              </Card>
            </OverlayTrigger>
          </Col>
        ))}
      </Row>
    </div>
  );
};

export default StatsSummary;
