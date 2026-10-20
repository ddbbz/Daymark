/**
 * 用户模块 —— UserInformationController（映射路径 /user/me）
 */
import { request } from '../http'
import { userVoSchema, type UserVoSchema } from '../schemas'
import type { UpdatePasswordBody, UpdateProfileBody, UserVO } from '../types/user'

export const userApi = {
  /**
   * GET /user/me → UserVO（需登录）
   * 用 zod 白名单剥离，防止后端缓存混入的 password 字段泄漏到前端。
   */
  me: async (options?: { silent?: boolean }): Promise<UserVO> => {
    const raw = await request<unknown>('/user/me', { method: 'GET', silent: options?.silent })
    return userVoSchema.parse(raw)
  },

  /** PUT /user/me/update/information  body{nickname,bio,avatar} */
  updateProfile: (payload: UpdateProfileBody) =>
    request<string>('/user/me/update/information', { method: 'PUT', body: payload }),

  /** PUT /user/me/update/password  body{password,newPassword} */
  updatePassword: (password: string, newPassword: string) =>
    request<string>('/user/me/update/password', {
      method: 'PUT',
      body: { password, newPassword } satisfies UpdatePasswordBody,
    }),

  /** POST /user/me/avatar  multipart `avatar`（单数，⚠ 需 token）→ URL 串 */
  uploadAvatar: (file: File) => {
    const form = new FormData()
    form.append('avatar', file)
    return request<string>('/user/me/avatar', { method: 'POST', form })
  },
}

export type { UserVoSchema }
