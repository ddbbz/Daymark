import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import { clearSession, getRole, getToken, isTokenExpired } from '@/api/http'
import { LoginRole } from '@/api/types/enums'

declare module 'vue-router' {
  interface RouteMeta {
    /** 仅未登录可访问（登录/注册） */
    guest?: boolean
    /** 需普通用户登录 */
    user?: boolean
    /** 需审查员登录 */
    reviewer?: boolean
    /** 公开页（任何登录态都可访问，如组件演示页） */
    public?: boolean
    /** 显示顶栏与底部标签栏 */
    chrome?: boolean
    title?: string
  }
}

/**
 * 路由表。history 模式（部署需 nginx `try_files $uri $uri/ /index.html`）。
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: () => (getRole() === LoginRole.reviewer ? { name: 'review' } : { name: 'home' }),
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guest: true, title: '登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { guest: true, title: '注册' },
  },
  {
    path: '/reviewer-register',
    name: 'reviewer-register',
    component: () => import('@/views/ReviewerRegisterView.vue'),
    meta: { guest: true, title: '审查员注册' },
  },
  {
    path: '/home',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta: { user: true, chrome: true, title: '发现' },
  },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('@/views/PublishView.vue'),
    meta: { user: true, chrome: true, title: '发布' },
  },
  {
    path: '/me',
    name: 'me',
    component: () => import('@/views/MeView.vue'),
    meta: { user: true, chrome: true, title: '我的' },
  },
  {
    path: '/post/:id',
    name: 'post-detail',
    component: () => import('@/views/PostDetailView.vue'),
    meta: { user: true, chrome: true, title: '帖子详情' },
  },
  {
    path: '/review',
    name: 'review',
    component: () => import('@/views/ReviewerView.vue'),
    meta: { reviewer: true, title: '审核台' },
  },
  {
    path: '/demo',
    name: 'demo',
    component: () => import('@/views/DemoView.vue'),
    meta: { public: true, title: '组件演示' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

/**
 * 全局前置守卫：登录态 + 角色隔离 + 令牌过期兜底。
 * 后端对用户/审查员用同一套 JWT 校验，role 隔离是前端职责。
 */
router.beforeEach((to) => {
  // 公开页（组件演示等）任何登录态都可访问
  if (to.meta.public) return true

  // 启动时先淘汰已过期的令牌
  if (getToken() && isTokenExpired()) clearSession()

  const loggedIn = !!getToken()
  const role = getRole()

  if (!loggedIn && !to.meta.guest) {
    return { name: 'login', query: to.fullPath === '/' ? undefined : { redirect: to.fullPath } }
  }
  if (loggedIn && to.meta.guest) {
    return { name: role === LoginRole.reviewer ? 'review' : 'home' }
  }
  if (loggedIn && role === LoginRole.reviewer && to.meta.user) {
    return { name: 'review' }
  }
  if (loggedIn && role === LoginRole.user && to.meta.reviewer) {
    return { name: 'home' }
  }
  return true
})

router.afterEach((to) => {
  const base = 'Daymark'
  document.title = to.meta.title ? `${to.meta.title} · ${base}` : base
})

export default router
