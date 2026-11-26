import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';

// En développement, le serveur Vite (port 5173) relaie l'API vers le backend Spring Boot (port 8080).
// En production, le build est embarqué dans le jar et servi par Spring Boot (ADR-008) : même origine, pas de CORS.
const BACKEND_URL = process.env.PODWATCH_BACKEND_URL ?? 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': BACKEND_URL,
      '/actuator': BACKEND_URL,
      '/ws': { target: BACKEND_URL, ws: true },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    css: false,
  },
});
