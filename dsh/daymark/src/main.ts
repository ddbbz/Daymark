import './styles/tokens.css'
import './styles/reset.css'
import './styles/base.css'
import './styles/utilities.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// api 层不反向依赖 router：把 401 的跳转行为注入进来，避免循环引用
setUnauthorizedHandler(() => {
  const auth = useAuthStore(pinia)
  auth.logout()
  const current = router.currentRoute.value
  if (current.name !== 'login') {
    router.replace({ name: 'login', query: { redirect: current.fullPath } })
  }
})

// 启动时预热当前用户资料（后端 LoginVO 只有 { jwt, role }，需补一次 GET /user/me）
router.isReady().then(() => {
  const auth = useAuthStore(pinia)
  if (auth.token && !auth.isReviewer) {
    void auth.loadMe({ silent: true })
  }
})

app.mount('#app')
