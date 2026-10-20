/**
 * Daymark 后端请求封装。
 *
 * 三条后端契约（实测，勿改顺序）：
 *   1. 鉴权请求头名是 `token`（无 `Bearer` 前缀）
 *   2. 401 时后端**不写任何响应体** → 必须先判断 res.status === 401，再解析 JSON
 *   3. `code === 1` 才是成功，`code === 0` 业务失败（HTTP 仍为 200）
 */
import { ApiError, AuthError, NetworkError } from './errors'
import type { Result, ResultCode } from './types/envelope'
import { LoginRole, type LoginRoleValue } from './types/enums'
import { storageGet, storageSet, storageRemove } from '@/utils/storage'

const TOKEN_KEY = 'dm_token'
const ROLE_KEY = 'dm_role'
const API_BASE = (import.meta.env.VITE_API_BASE as string | undefined) || '/api'

/* ------------------------------------------------------------------ *
 * 令牌 / 角色存取
 * ------------------------------------------------------------------ */

export function getToken(): string {
  return storageGet(TOKEN_KEY) ?? ''
}

export function setToken(token: string): void {
  storageSet(TOKEN_KEY, token)
}

export function clearToken(): void {
  storageRemove(TOKEN_KEY)
}

export function getRole(): LoginRoleValue {
  const raw = storageGet(ROLE_KEY)
  const n = raw === null ? LoginRole.user : Number(raw)
  return n === LoginRole.reviewer ? LoginRole.reviewer : LoginRole.user
}

export function setRole(role: LoginRoleValue): void {
  storageSet(ROLE_KEY, String(role))
}

export function clearRole(): void {
  storageRemove(ROLE_KEY)
}

export function clearSession(): void {
  clearToken()
  clearRole()
}

/* ------------------------------------------------------------------ *
 * JWT 解析（不校验签名，仅用于前端预判）
 * ------------------------------------------------------------------ */

export interface JwtPayload {
  sub?: string
  role?: number
  iat?: number
  exp?: number
}

/** 解出 JWT 的 payload；解析失败返回 null（不抛错，交给后端裁决） */
export function decodeJwt(token: string): JwtPayload | null {
  if (!token) return null
  const part = token.split('.')[1]
  if (!part) return null
  try {
    const base64 = part.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    const binary = atob(padded)
    const bytes = Uint8Array.from(binary, (c) => c.charCodeAt(0))
    return JSON.parse(new TextDecoder().decode(bytes)) as JwtPayload
  } catch {
    return null
  }
}

/** 令牌是否已过期（解析失败按未过期处理）。exp 单位秒，预留 10 秒余量。 */
export function isTokenExpired(token: string = getToken()): boolean {
  const payload = decodeJwt(token)
  if (!payload || typeof payload.exp !== 'number') return false
  return payload.exp * 1000 <= Date.now() + 10_000
}

/* ------------------------------------------------------------------ *
 * 401 跳转回调（由 main.ts 注入，避免 api 层反向依赖 router）
 * ------------------------------------------------------------------ */

let onUnauthorized: (() => void) | null = null

export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler
}

/* ------------------------------------------------------------------ *
 * 请求
 * ------------------------------------------------------------------ */

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  /** 查询参数（自动编码；空字符串保留——后端 @RequestParam 默认 required=true） */
  params?: Record<string, string | number | boolean | undefined | null>
  /** JSON 请求体 */
  body?: unknown
  /** FormData 请求体（文件上传，不设置 Content-Type） */
  form?: FormData
  /** 静默模式：401 不触发跳转（用于启动探测） */
  silent?: boolean
}

function buildQuery(params?: RequestOptions['params']): string {
  if (!params) return ''
  const usp = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null) continue
    usp.append(key, String(value))
  }
  const qs = usp.toString()
  return qs ? `?${qs}` : ''
}

/**
 * 统一请求入口，返回 Result.data（成功）或抛 ApiError/AuthError/NetworkError。
 */
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', params, body, form, silent = false } = options

  const headers: Record<string, string> = {}
  const token = getToken()
  if (token) headers.token = token

  let payload: BodyInit | undefined
  if (form) {
    payload = form
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
    payload = JSON.stringify(body)
  }

  let res: Response
  try {
    res = await fetch(API_BASE + path + buildQuery(params), { method, headers, body: payload })
  } catch {
    throw new NetworkError()
  }

  // 401 无响应体，必须先拦
  if (res.status === 401) {
    clearSession()
    if (!silent) onUnauthorized?.()
    throw new AuthError()
  }

  const text = await res.text()
  if (!text) {
    if (res.ok) return null as T
    throw new ApiError(`请求失败（HTTP ${res.status}）`, { status: res.status })
  }

  let result: Result<T>
  try {
    result = JSON.parse(text) as Result<T>
  } catch {
    throw new ApiError(`响应解析失败（HTTP ${res.status}），请检查后端接口地址`, {
      status: res.status,
    })
  }

  // 后端业务异常也返回 HTTP 200，靠 code 区分（1 成功 / 0 失败）
  if (result.code === 1) return result.data

  throw new ApiError(result.message || '操作失败', { code: result.code, status: res.status })
}

export type HttpMethod = NonNullable<RequestOptions['method']>

export const get = <T>(path: string, params?: RequestOptions['params'], options?: RequestOptions) =>
  request<T>(path, { ...options, method: 'GET', params })

export const post = <T>(path: string, body?: unknown, options?: RequestOptions) =>
  request<T>(path, { ...options, method: 'POST', body })

export const put = <T>(path: string, body?: unknown, options?: RequestOptions) =>
  request<T>(path, { ...options, method: 'PUT', body })

export const del = <T>(path: string, body?: unknown, options?: RequestOptions) =>
  request<T>(path, { ...options, method: 'DELETE', body })

export type { Result, ResultCode }
