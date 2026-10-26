import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// O navegador chama /api/... no próprio endereço do front (mesma origem).
// O Vite repassa para a API Spring. Assim os cookies SESSION e XSRF-TOKEN
// funcionam sem configurar CORS no back-end.
//   Local (padrão):  VITE_API_TARGET=http://localhost:8080
//   Deploy da API:   VITE_API_TARGET=https://api-kjea.onrender.com
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: env.VITE_API_TARGET || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },
  };
});
