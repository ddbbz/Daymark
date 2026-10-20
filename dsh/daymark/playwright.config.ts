import { defineConfig, devices } from '@playwright/test'

/**
 * E2E 配置。
 * webServer 起 Vite dev（21000）；后端需另行启动（8080）。
 * 浏览器二进制首次运行需 `pnpm exec playwright install chromium`。
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  retries: 0,
  reporter: [['list']],
  use: {
    baseURL: 'http://localhost:21000',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
  webServer: {
    command: 'pnpm dev',
    url: 'http://localhost:21000',
    reuseExistingServer: true,
    timeout: 30_000,
  },
})
