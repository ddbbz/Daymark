import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * 后端契约（实测）：
 *   - 后端端口 8080，请求同源走 /api 前缀（proxy 去掉前缀）
 *   - 图片由 nginx:21001 的 /file/ 直发；开发期 nginx 未启动时，
 *     /file、/images 代理到 8080 兜底（后端 WebConfig 也映射了同一目录）
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backend = env.VITE_BACKEND_ORIGIN ?? 'http://localhost:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      port: 21000,
      proxy: {
        '/api': {
          target: backend,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
        '/file': { target: backend, changeOrigin: true },
        '/images': { target: backend, changeOrigin: true },
      },
    },
  }
})
