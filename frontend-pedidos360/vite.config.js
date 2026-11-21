import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    proxy: {
      '/api/catalog': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
      '/api/orders': {
        target: 'http://localhost:8087',
        changeOrigin: true,
      },
    },
  },
})
