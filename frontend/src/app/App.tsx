import { Button, Result } from 'antd';
import { Route, Routes, useNavigate } from 'react-router';

import { DashboardPage } from '../features/dashboard/DashboardPage';
import { AppLayout } from './AppLayout';

function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <Result
      status="404"
      title="Page introuvable"
      extra={
        <Button type="primary" onClick={() => navigate('/')}>
          Retour au tableau de bord
        </Button>
      }
    />
  );
}

export function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<DashboardPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
