<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { firstError, loginSchema } from '@/utils/validate'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const mode = ref<'user' | 'reviewer'>('user')
const uid = ref('')
const password = ref('')
const loading = ref(false)

async function submit() {
  const parsed = loginSchema.safeParse({ uid: uid.value, password: password.value })
  if (!parsed.success) {
    toast.error(firstError(parsed) ?? '输入有误')
    return
  }

  loading.value = true
  try {
    if (mode.value === 'reviewer') {
      await auth.reviewerLogin(uid.value, password.value)
    } else {
      await auth.login(uid.value, password.value)
    }
    toast.success('登录成功')

    const redirect = route.query.redirect
    if (typeof redirect === 'string' && redirect) {
      router.replace(redirect)
    } else {
      router.replace({ name: mode.value === 'reviewer' ? 'review' : 'home' })
    }
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login">
    <h1 class="login__brand">Daymark</h1>

    <div class="login__segment" role="tablist">
      <button
        class="segment"
        :class="{ 'segment--active': mode === 'user' }"
        type="button"
        @click="mode = 'user'"
      >
        用户登录
      </button>
      <button
        class="segment"
        :class="{ 'segment--active': mode === 'reviewer' }"
        type="button"
        @click="mode = 'reviewer'"
      >
        审查员登录
      </button>
    </div>

    <BaseInput v-model="uid" label="用户名" placeholder="3~20 位字母数字下划线" @enter="submit" />
    <BaseInput
      v-model="password"
      label="密码"
      type="password"
      placeholder="6~32 位"
      @enter="submit"
    />

    <BaseButton block size="lg" :loading="loading" @click="submit">登录</BaseButton>

    <p v-if="mode === 'user'" class="login__links">
      还没有账号？<RouterLink class="link" to="/register">去注册</RouterLink>
    </p>
    <p v-else class="login__links">
      还没有审查员账号？<RouterLink class="link" to="/reviewer-register">申请注册</RouterLink>
    </p>
  </main>
</template>

<style scoped>
.login {
  max-width: 400px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.login__brand {
  text-align: center;
  font-size: 28px;
  font-weight: 800;
  color: var(--c-brand);
  margin-bottom: var(--space-4);
}

.login__segment {
  display: flex;
  padding: var(--space-1);
  background: var(--c-bg-page);
  border-radius: var(--radius-m);
}

.segment {
  flex: 1;
  padding: var(--space-2);
  border-radius: var(--radius-s);
  color: var(--c-text-2);
}

.segment--active {
  background: var(--c-bg-card);
  color: var(--c-text-1);
  font-weight: 500;
  box-shadow: var(--shadow-s);
}

.login__links {
  text-align: center;
  font-size: 13px;
  color: var(--c-text-2);
}

.link {
  color: var(--c-brand);
}
</style>
