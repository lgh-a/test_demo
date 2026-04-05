import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('nostalgist')) {
            return 'emulator'
          }
          if (id.includes('naive-ui')) {
            return 'ui'
          }
          if (id.includes('node_modules')) {
            return 'vendor'
          }
        }
      }
    }
  },
  server: {
    host: '0.0.0.0',
    port: 5173
  }
})
