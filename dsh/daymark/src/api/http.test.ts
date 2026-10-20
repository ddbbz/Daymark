import { afterEach, describe, expect, it, vi } from 'vitest'

import { ApiError, AuthError, NetworkError } from './errors'
import { clearSession, request, setToken } from './http'

function stubFetch(impl: (url: RequestInfo | URL, init?: RequestInit) => Promise<Response>) {
  vi.stubGlobal('fetch', vi.fn(impl))
}

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

afterEach(() => {
  vi.unstubAllGlobals()
  clearSession()
})

describe('request 契约（后端实测行为）', () => {
  it('401 且空 body → 抛 AuthError，而不是 JSON 解析错误', async () => {
    stubFetch(async () => new Response('', { status: 401 }))
    await expect(request('/x')).rejects.toBeInstanceOf(AuthError)
  })

  it('code === 1 → 返回 data（成功）', async () => {
    stubFetch(async () => jsonResponse({ code: 1, message: null, data: { ok: true } }))
    await expect(request<{ ok: boolean }>('/x')).resolves.toEqual({ ok: true })
  })

  it('code === 0 → 抛 ApiError 且 message 透传', async () => {
    stubFetch(async () => jsonResponse({ code: 0, message: '用户名或密码错误', data: null }))
    await expect(request('/x')).rejects.toMatchObject({
      name: 'ApiError',
      message: '用户名或密码错误',
    })
  })

  it('业务失败返回 HTTP 200 也判失败（不靠状态码）', async () => {
    stubFetch(async () => jsonResponse({ code: 0, message: '失败', data: null }, 200))
    await expect(request('/x')).rejects.toBeInstanceOf(ApiError)
  })

  it('fetch 网络异常 → NetworkError', async () => {
    stubFetch(async () => {
      throw new TypeError('fetch failed')
    })
    await expect(request('/x')).rejects.toBeInstanceOf(NetworkError)
  })

  it('鉴权请求头名是 token（无 Bearer 前缀）', async () => {
    setToken('jwt-token')
    let captured: RequestInit | undefined
    stubFetch(async (_url, init) => {
      captured = init
      return jsonResponse({ code: 1, message: null, data: null })
    })
    await request('/x')
    const headers = (captured?.headers ?? {}) as Record<string, string>
    expect(headers.token).toBe('jwt-token')
    expect(headers.Authorization).toBeUndefined()
  })
})
