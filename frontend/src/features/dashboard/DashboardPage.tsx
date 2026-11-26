import { Alert, Card, Space, Typography } from 'antd';

import { useSystemInfo } from '../../api/system';

/**
 * Tableau de bord — squelette de la Phase 1.
 * Les indicateurs (nodes, pods, statuts, events…) arrivent en Phase 3.
 */
export function DashboardPage() {
  const systemInfo = useSystemInfo();

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={3} style={{ margin: 0 }}>
        Tableau de bord
      </Typography.Title>

      {systemInfo.isPending && <Alert type="info" showIcon message="Connexion au backend…" />}
      {systemInfo.isError && (
        <Alert
          type="error"
          showIcon
          message="Backend injoignable"
          description="Vérifiez que le backend Spring Boot est démarré (voir README)."
        />
      )}
      {systemInfo.isSuccess && (
        <Alert
          type="success"
          showIcon
          message={`Backend connecté — ${systemInfo.data.name} v${systemInfo.data.version}`}
        />
      )}

      <Card title="Clusters">
        <Typography.Paragraph type="secondary" style={{ margin: 0 }}>
          Aucun cluster n'est encore configuré. La connexion aux clusters PREPROD et PROD arrive en Phase 2.
        </Typography.Paragraph>
      </Card>
    </Space>
  );
}
