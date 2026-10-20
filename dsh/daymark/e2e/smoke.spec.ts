import { expect, test } from '@playwright/test'

/**
 * 冒烟测试：不依赖后端（登录页挂载时不发请求）。
 */
test('登录页可访问', async ({ page }) => {
  await page.goto('/login')
  await expect(page.locator('h1')).toHaveText('Daymark')
  await expect(page.getByRole('button', { name: '登录' })).toBeVisible()
})

test('未登录访问首页重定向到登录', async ({ page }) => {
  await page.goto('/home')
  await expect(page).toHaveURL(/\/login/)
})
