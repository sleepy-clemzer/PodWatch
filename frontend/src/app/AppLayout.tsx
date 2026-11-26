import {
  AlertOutlined,
  AppstoreOutlined,
  DashboardOutlined,
  FileTextOutlined,
  LineChartOutlined,
  ToolOutlined,
} from '@ant-design/icons';
import { Layout, Menu, Segmented, Select, Space, Tag, Typography, theme } from 'antd';
import type { MenuProps } from 'antd';
import { Outlet, useLocation, useNavigate } from 'react-router';

import { useSystemInfo } from '../api/system';

const { Header, Sider, Content, Footer } = Layout;

/** Les entrées désactivées seront activées au fil des phases (voir docs/ROADMAP.md). */
const MENU_ITEMS: MenuProps['items'] = [
  { key: '/', icon: <DashboardOutlined />, label: 'Tableau de bord' },
  { key: '/resources', icon: <AppstoreOutlined />, label: 'Ressources', disabled: true },
  { key: '/logs', icon: <FileTextOutlined />, label: 'Logs', disabled: true },
  { key: '/monitoring', icon: <LineChartOutlined />, label: 'Monitoring', disabled: true },
  { key: '/alerts', icon: <AlertOutlined />, label: 'Alertes', disabled: true },
  { key: '/run', icon: <ToolOutlined />, label: 'Run', disabled: true },
];

export function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = theme.useToken();
  const systemInfo = useSystemInfo();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsible width={220}>
        <div
          style={{
            height: 56,
            display: 'flex',
            alignItems: 'center',
            paddingInline: 20,
            color: '#fff',
            fontWeight: 600,
            fontSize: 18,
          }}
        >
          PodWatch
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={MENU_ITEMS}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: token.colorBgContainer,
            paddingInline: 24,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
          }}
        >
          {/* Sélecteurs branchés en Phase 2 (registre multi-cluster). */}
          <Space size="middle">
            <Segmented
              aria-label="Environnement"
              options={['Tous', 'PREPROD', 'PROD']}
              defaultValue="Tous"
              disabled
            />
            <Select
              aria-label="Cluster"
              style={{ width: 240 }}
              placeholder="Aucun cluster configuré"
              disabled
              options={[]}
            />
          </Space>
        </Header>
        <Content style={{ margin: 24 }}>
          <Outlet />
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          <Typography.Text type="secondary">
            PodWatch{' '}
            {systemInfo.isSuccess ? (
              <Tag>{`v${systemInfo.data.version}`}</Tag>
            ) : systemInfo.isError ? (
              <Tag color="error">backend injoignable</Tag>
            ) : null}
          </Typography.Text>
        </Footer>
      </Layout>
    </Layout>
  );
}
