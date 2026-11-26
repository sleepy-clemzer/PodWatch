import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App as AntdApp, ConfigProvider } from 'antd';
import frFR from 'antd/locale/fr_FR';
import type { ReactNode } from 'react';

export function createQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { refetchOnWindowFocus: false, retry: 1 },
    },
  });
}

/** Fournisseurs globaux : cache des données serveur (TanStack Query) et thème/locale Ant Design. */
export function AppProviders({ children, queryClient }: { children: ReactNode; queryClient: QueryClient }) {
  return (
    <QueryClientProvider client={queryClient}>
      <ConfigProvider locale={frFR} theme={{ token: { colorPrimary: '#1f6fb2' } }}>
        <AntdApp>{children}</AntdApp>
      </ConfigProvider>
    </QueryClientProvider>
  );
}
