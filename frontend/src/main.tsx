import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router';

import { App } from './app/App';
import { AppProviders, createQueryClient } from './app/providers';

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Élément #root introuvable dans index.html');
}

createRoot(rootElement).render(
  <StrictMode>
    <AppProviders queryClient={createQueryClient()}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </AppProviders>
  </StrictMode>,
);
