import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vitest/config'

export default defineConfig({
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  test: {
    // 契约层测试不需要 DOM；组件测试（后续阶段）用注释 `// @vitest-environment happy-dom` 覆盖
    environment: 'node',
    include: ['src/**/*.test.ts'],
  },
})
