import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: path => path
      },
      '/graduate': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: path => path
      }
    },
    allowedHosts: [
      '0b203823dc33.ngrok-free.app'
    ]
  }
})
