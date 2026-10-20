import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('@/api/modules/auth', () => ({
  authApi: {
    login: vi.fn(),
    reviewerLogin: vi.fn(),
    register: vi.fn(),
    registerDefaults: vi.fn(),
    uploadRegisterAvatar: vi.fn(),
    reviewerRegister: vi.fn(),
  },
}))

vi.mock('@/api/modules/user', () => ({
  userApi: {
    me: vi.fn(),
    updateProfile: vi.fn(),
    updatePassword: vi.fn(),
    uploadAvatar: vi.fn(),
  },
}))

import { authApi } from '@/api/modules/auth'
import { userApi } from '@/api/modules/user'
import { useAuthStore } from './auth'

beforeEach(() => {
  setActivePinia(createPinia())
})

describe('auth store', () => {
  it('login 写入 token 与 role=0', async () => {
    vi.mocked(authApi.login).mockResolvedValue({ jwt: 'jwt-1', role: 0 })
    const store = useAuthStore()
    await store.login('abc', '123456')

    expect(store.token).toBe('jwt-1')
    expect(store.role).toBe(0)
    expect(store.isLoggedIn).toBe(true)
    expect(store.isReviewer).toBe(false)
  })

  it('reviewerLogin 写入 role=1', async () => {
    vi.mocked(authApi.reviewerLogin).mockResolvedValue({ jwt: 'jwt-2', role: 1 })
    const store = useAuthStore()
    await store.reviewerLogin('admin', '123456')

    expect(store.isReviewer).toBe(true)
  })

  it('loadMe 缓存用户资料并提供 userId', async () => {
    vi.mocked(userApi.me).mockResolvedValue({
      id: 7,
      uid: 'abc',
      nickname: '小红',
      bio: null,
      avatar: null,
    })
    const store = useAuthStore()
    store.applyLogin({ jwt: 'jwt-3', role: 0 })

    const user = await store.loadMe()
    expect(user?.id).toBe(7)
    expect(store.userId).toBe(7)
    expect(store.user?.nickname).toBe('小红')
  })

  it('logout 清空登录态', async () => {
    vi.mocked(authApi.login).mockResolvedValue({ jwt: 'jwt-4', role: 0 })
    const store = useAuthStore()
    await store.login('abc', '123456')
    store.logout()

    expect(store.token).toBe('')
    expect(store.isLoggedIn).toBe(false)
  })
})
