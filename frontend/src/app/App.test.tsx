import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { App } from './App';
import { AppProviders, createQueryClient } from './providers';

function renderApp(path = '/') {
  const queryClient = createQueryClient();
  queryClient.setDefaultOptions({ queries: { retry: false } });
  return render(
    <AppProviders queryClient={queryClient}>
      <MemoryRouter initialEntries={[path]}>
        <App />
      </MemoryRouter>
    </AppProviders>,
  );
}

/** Une nouvelle Response à chaque appel : le corps d'une Response ne peut être lu qu'une fois. */
function mockFetch(createResponse: () => Response) {
  return vi.spyOn(globalThis, 'fetch').mockImplementation(() => Promise.resolve(createResponse()));
}

const okSystemInfo = () =>
  new Response(JSON.stringify({ name: 'podwatch', version: '0.1.0' }), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
  });

describe('App', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('affiche le tableau de bord et la version du backend', async () => {
    const fetchSpy = mockFetch(okSystemInfo);

    renderApp();

    expect(screen.getByRole('heading', { name: 'Tableau de bord' })).toBeInTheDocument();
    expect(await screen.findByText('Backend connecté — podwatch v0.1.0')).toBeInTheDocument();
    expect(fetchSpy).toHaveBeenCalledWith('/api/v1/system/info', expect.anything());
  });

  it('signale un backend injoignable', async () => {
    mockFetch(() => new Response(null, { status: 503 }));

    renderApp();

    expect(await screen.findByText('Backend injoignable')).toBeInTheDocument();
  });

  it('affiche une page 404 pour une route inconnue', async () => {
    mockFetch(okSystemInfo);

    renderApp('/route-inconnue');

    expect(await screen.findByText('Page introuvable')).toBeInTheDocument();
  });
});
