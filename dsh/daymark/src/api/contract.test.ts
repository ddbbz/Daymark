import { afterEach, describe, expect, it, vi } from 'vitest'

import { clearSession } from './http'
import { authApi } from './modules/auth'
import { commentApi } from './modules/comment'
import { postApi } from './modules/post'
import { registerDefaultVoSchema, userVoSchema } from './schemas'
import { parseLocalDateTime } from '@/utils/format'

interface Captured {
  url: string
  init?: RequestInit
}

/** 拦截 fetch，记录请求，返回 code===1 的空成功 */
function mockFetch(): Captured {
  const state: Captured = { url: '' }
  vi.stubGlobal(
    'fetch',
    vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      state.url = String(input)
      state.init = init
      return new Response(JSON.stringify({ code: 1, message: null, data: null }), { status: 200 })
    }),
  )
  return state
}

function bodyOf(state: Captured): Record<string, unknown> {
  return JSON.parse(String(state.init?.body)) as Record<string, unknown>
}

afterEach(() => {
  vi.unstubAllGlobals()
  clearSession()
})

describe('字段名契约（后端静默丢字段，必须锁死）', () => {
  it('发布帖子的图片字段名是单数 image（不是 images）', async () => {
    const state = mockFetch()
    await postApi.publish({ title: 't', content: 'c', image: ['a.jpg'] })
    const body = bodyOf(state)
    expect(body).toEqual({ title: 't', content: 'c', image: ['a.jpg'] })
    expect(body).not.toHaveProperty('images')
  })

  it('编辑帖子必带 userId（后端原始 int，缺省反序列化为 0）', async () => {
    const state = mockFetch()
    await postApi.update({ id: 1, userId: 7, title: 't', content: 'c', image: [] })
    expect(bodyOf(state).userId).toBe(7)
  })

  it('删除评论 body 必须含 id + userId + postId 三件套', async () => {
    const state = mockFetch()
    await commentApi.remove({ id: 9, userId: 3, postId: 5 })
    expect(bodyOf(state)).toEqual({ id: 9, userId: 3, postId: 5 })
  })

  it('上传字段名：头像 avatar（单数）、配图 avatars（复数）', async () => {
    const f = new File(['x'], 'a.png', { type: 'image/png' })

    const s1 = mockFetch()
    await authApi.uploadRegisterAvatar(f)
    const form1 = s1.init?.body as FormData
    expect(form1.has('avatar')).toBe(true)
    expect(form1.has('avatars')).toBe(false)

    const s2 = mockFetch()
    await postApi.uploadImages([f])
    const form2 = s2.init?.body as FormData
    expect(form2.has('avatars')).toBe(true)
    expect(form2.has('avatar')).toBe(false)
  })
})

describe('运行时校验与安全', () => {
  it('UserVO 白名单剥离：丢弃混入的 password 字段', () => {
    const parsed = userVoSchema.parse({
      id: 1,
      uid: 'u1',
      nickname: 'n',
      bio: null,
      avatar: null,
      password: '$2a$10$should-be-stripped',
    })
    expect(parsed).not.toHaveProperty('password')
    expect(parsed.id).toBe(1)
    expect(parsed.uid).toBe('u1')
  })

  it('默认注册信息允许 uid 为 null（后端兜底不充分，前端本地兜底）', () => {
    const parsed = registerDefaultVoSchema.parse({ uid: null, avatarUrl: null })
    expect(parsed.uid).toBeNull()
  })

  it('无时区 LocalDateTime 按本地时间解析', () => {
    const d = parseLocalDateTime('2026-09-15T10:00:00')
    expect(d).not.toBeNull()
    expect(d?.getFullYear()).toBe(2026)
    expect(d?.getMonth()).toBe(8) // 月份 0 基
    expect(d?.getDate()).toBe(15)
    expect(d?.getHours()).toBe(10)
    expect(d?.getMinutes()).toBe(0)
  })
})
