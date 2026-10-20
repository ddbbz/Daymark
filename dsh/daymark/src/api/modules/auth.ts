/**
 * 认证模块 —— LoginController / RegisterController / ReviewerLoginController / ReviewerRegisterController
 */
import { request } from '../http'
import type {
  LoginBody,
  LoginVO,
  RegisterBody,
  RegisterDefaultVO,
  ReviewerRegisterBody,
} from '../types/user'

export const authApi = {
  /** POST /login  body{uid,password} → LoginVO{jwt,role}（role=0） */
  login: (uid: string, password: string) =>
    request<LoginVO>('/login', { method: 'POST', body: { uid, password } satisfies LoginBody }),

  /** GET /register → RegisterDefaultVO{uid,avatarUrl}（uid 可能为 null） */
  registerDefaults: () => request<RegisterDefaultVO>('/register', { method: 'GET' }),

  /** POST /register  body{uid,password,nickname,bio,avatar} → 提示串 */
  register: (payload: RegisterBody) => request<string>('/register', { method: 'POST', body: payload }),

  /** POST /register/avatar  multipart `avatar`（单数，无需 token）→ URL 串 */
  uploadRegisterAvatar: (file: File) => {
    const form = new FormData()
    form.append('avatar', file)
    return request<string>('/register/avatar', { method: 'POST', form })
  },

  /** POST /reviewerLogin  body{uid,password} → LoginVO{jwt,role}（role=1） */
  reviewerLogin: (uid: string, password: string) =>
    request<LoginVO>('/reviewerLogin', {
      method: 'POST',
      body: { uid, password } satisfies LoginBody,
    }),

  /** POST /reviewerRegister  body{uid,password,inviteCode} → null */
  reviewerRegister: (payload: ReviewerRegisterBody) =>
    request<null>('/reviewerRegister', { method: 'POST', body: payload }),
}
