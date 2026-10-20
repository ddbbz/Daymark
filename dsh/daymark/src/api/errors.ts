import type { ResultCode } from './types/envelope'

/**
 * 业务失败（code === 0）。message 即后端 Result.message，可安全透传给用户。
 * ⚠ 后端业务失败仍返回 HTTP 200，不能靠 HTTP 状态码判断。
 */
export class ApiError extends Error {
  readonly code: ResultCode
  readonly status: number

  constructor(message: string, options: { code?: ResultCode; status?: number } = {}) {
    super(message)
    this.name = 'ApiError'
    this.code = options.code ?? 0
    this.status = options.status ?? 200
  }
}

/** 未登录 / 令牌失效（HTTP 401）。后端 401 无响应体。 */
export class AuthError extends Error {
  constructor(message = '登录已过期，请重新登录') {
    super(message)
    this.name = 'AuthError'
  }
}

/** 网络层失败（后端未启动、断网、CORS 等）。 */
export class NetworkError extends Error {
  constructor(message = '网络异常：请确认后端服务已启动') {
    super(message)
    this.name = 'NetworkError'
  }
}
