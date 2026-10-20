/**
 * 登录态。
 * 后端 LoginVO 只返回 { jwt, role }，用户资料需另外调 GET /user/me。
 * role 沿用后端 LoginRole：0=普通用户，1=审查员。
 */
import { defineStore } from 'pinia'

import { authApi } from '@/api/modules/auth'
import { userApi } from '@/api/modules/user'
import {
  clearSession,
  getRole,
  getToken,
  isTokenExpired,
  setRole,
  setToken,
} from '@/api/http'
import { LoginRole, type LoginRoleValue } from '@/api/types/enums'
import type { UserVO } from '@/api/types/user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    role: getRole(),
    /** 当前登录用户资料，null 表示尚未加载 */
    user: null as UserVO | null,
    /** 是否已尝试过加载用户资料（避免重复请求） */
    loaded: false,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token && !isTokenExpired(state.token),
    isReviewer: (state) => state.role === LoginRole.reviewer,
    /** 当前用户数字 id（发帖/编辑/删评论时需要传给后端做归属比对） */
    userId: (state): number => state.user?.id ?? 0,
  },

  actions: {
    /** 写入登录结果（普通用户与审查员共用，后端返回结构一致） */
    applyLogin(
      data: { jwt?: string; role?: LoginRoleValue } | null,
      fallbackRole: LoginRoleValue = LoginRole.user,
    ) {
      this.token = data?.jwt ?? ''
      this.role = data?.role ?? fallbackRole
      this.user = null
      this.loaded = false
      setToken(this.token)
      setRole(this.role)
    },

    /** 普通用户登录：POST /login */
    async login(uid: string, password: string) {
      const data = await authApi.login(uid, password)
      this.applyLogin(data, LoginRole.user)
      return data
    },

    /** 审查员登录：POST /reviewerLogin */
    async reviewerLogin(uid: string, password: string) {
      const data = await authApi.reviewerLogin(uid, password)
      this.applyLogin(data, LoginRole.reviewer)
      return data
    },

    /** GET /user/me，失败时清空资料但不影响登录态 */
    async loadMe({ silent = false }: { silent?: boolean } = {}) {
      if (!this.token) return null
      try {
        this.user = await userApi.me({ silent })
        this.loaded = true
        return this.user
      } catch (e) {
        this.loaded = true
        if (!silent) throw e
        return null
      }
    },

    /** 更新资料后刷新本地缓存 */
    async refreshMe() {
      return this.loadMe({ silent: true })
    },

    logout() {
      this.token = ''
      this.role = LoginRole.user
      this.user = null
      this.loaded = false
      clearSession()
    },
  },
})
