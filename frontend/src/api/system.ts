import { useQuery } from '@tanstack/react-query';

import { getJson } from './client';

/** Miroir du record Java {@code SystemInfoController.SystemInfo}. */
export interface SystemInfo {
  name: string;
  version: string;
}

export function useSystemInfo() {
  return useQuery({
    queryKey: ['system', 'info'],
    queryFn: () => getJson<SystemInfo>('/api/v1/system/info'),
    staleTime: 5 * 60 * 1000,
  });
}
