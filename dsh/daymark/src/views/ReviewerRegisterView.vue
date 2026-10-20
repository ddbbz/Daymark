<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import { authApi } from '@/api/modules/auth'
import { useToastStore } from '@/stores/toast'
import { firstError, reviewerRegisterSchema } from '@/utils/validate'

const router = useRouter()
const toast = useToastStore()

const uid = ref('')
const password = ref('')
const inviteCode = ref('')
const submitting = ref(false)

async function submit() {
  const parsed = reviewerRegisterSchema.safeParse({
    uid: uid.value,
    password: password.value,
    inviteCode: inviteCode.value,
  })
  if (!parsed.success) {
    toast.error(firstError(parsed) ?? '输入有误')
    return
  }

  submitting.value = true
  try {
    await authApi.reviewerRegister({
      uid: uid.value,
      password: password.value,
      inviteCode: inviteCode.value,
    })
    toast.success('审查员账号已创建，请登录')
    router.replace({ name: 'login' })
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '注册失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="reviewer-register">
    <h1 class="reviewer-register__title">审查员注册</h1>

    <BaseInput v-model="uid" label="用户名" placeholder="3~20 位字母数字下划线" />
    <BaseInput v-model="password" label="密码" type="password" placeholder="6~32 位" />
    <BaseInput v-model="inviteCode" label="邀请码" placeholder="请输入审查员邀请码" />

    <BaseButton block size="lg" :loading="submitting" @click="submit">提交申请</BaseButton>

    <p class="reviewer-register__links">
      返回<RouterLink class="link" to="/login">登录</RouterLink>
    </p>
  </main>
</template>

<style scoped>
.reviewer-register {
  max-width: 400px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-4) var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.reviewer-register__title {
  font-size: 22px;
  font-weight: 700;
}

.reviewer-register__links {
  text-align: center;
  font-size: 13px;
  color: var(--c-text-2);
}

.link {
  color: var(--c-brand);
}
</style>
