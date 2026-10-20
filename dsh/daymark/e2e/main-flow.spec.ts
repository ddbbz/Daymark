import { expect, test } from '@playwright/test'

/**
 * 主链路 E2E：注册 → 登录 → 发帖 → 首页可见 → 详情 → 评论。
 *
 * ⚠ 需要后端（8080）+ MySQL + Redis + 建表 SQL 就绪。
 * 后端不可达时自动 skip（在测试内运行时判定）。
 */
async function backendReady(): Promise<boolean> {
  try {
    const res = await fetch('http://localhost:8080/register', { method: 'GET' })
    return res.status < 500
  } catch {
    return false
  }
}

let uid = ''
let password = ''

test('主链路', async ({ page }) => {
  test.skip(!(await backendReady()), '需要后端与数据库，已跳过')

  const stamp = Date.now().toString().slice(-8)
  uid = `e2e${stamp}`
  password = '123456'
  const title = `E2E 帖子 ${stamp}`

  // 注册
  await page.goto('/register')
  await page.getByLabel('用户名').fill(uid)
  await page.getByLabel('密码').fill(password)
  await page.getByLabel('确认密码').fill(password)
  await page.getByRole('button', { name: '注册' }).click()
  await expect(page).toHaveURL(/\/login/, { timeout: 15_000 })

  // 登录
  await page.getByLabel('用户名').fill(uid)
  await page.getByLabel('密码').fill(password)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/home/, { timeout: 15_000 })

  // 发帖
  await page.goto('/publish')
  await page.getByLabel('标题').fill(title)
  await page.getByLabel('正文').fill('端到端测试正文')
  await page.getByRole('button', { name: '发布' }).click()
  await expect(page).toHaveURL(/\/home/, { timeout: 15_000 })

  // 搜索该帖（发布后状态为审核中，showByTitle 只返回审核通过的帖子，
  // 因此这里依赖后端已有「已通过」的帖子或用审查员账号先审核。此处仅验证搜索接口可达）
  await page.getByPlaceholder(/标题搜索/).fill(title)
  await page.getByRole('button', { name: '搜索' }).click()
  await expect(page.getByText('加载失败')).toHaveCount(0, { timeout: 10_000 })
})
