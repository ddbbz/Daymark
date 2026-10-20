<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { authApi } from '@/api/modules/auth'
import { useToastStore } from '@/stores/toast'
import { generateDefaultUid } from '@/utils/uid'
import { firstError, registerSchema } from '@/utils/validate'

const router = useRouter()
const toast = useToastStore()

const uid = ref('')
const password = ref('')
const confirmPassword = ref('')
const nickname = ref('')
const bio = ref('')
const avatar = ref('')
const avatarUploading = ref(false)
const submitting = ref(false)

const avatarInput = ref<HTMLInputElement | null>(null)

onMounted(async () => {
  try {
    const defaults = await authApi.registerDefaults()
    // 后端 uid 可能为 null（撞库兜底不充分），本地兜底生成
    uid.value = defaults.uid ?? generateDefaultUid()
    avatar.value = defaults.avatarUrl ?? ''
  } catch (e) {
    uid.value = generateDefaultUid()
    toast.info(e instanceof Error ? e.message : '默认信息获取失败，已生成本地默认值')
  }
})

function pickAvatar() {
  avatarInput.value?.click()
}

async function onAvatarChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  avatarUploading.value = true
  try {
    avatar.value = await authApi.uploadRegisterAvatar(file)
    toast.success('头像上传成功')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : '头像上传失败')
  } finally {
    avatarUploading.value = false
    input.value = ''
  }
}

async function submit() {
  if (password.value !== confirmPassword.value) {
    toast.error('两次输入的密码不一致')
    return
  }
  const parsed = registerSchema.safeParse({
    uid: uid.value,
    password: password.value,
    nickname: nickname.value.trim() || undefined,
    bio: bio.value.trim() || undefined,
  })
  if (!parsed.success) {
    toast.error(firstError(parsed) ?? '输入有误')
    return
  }

  submitting.value = true
  try {
    await authApi.register({
      uid: uid.value,
      password: password.value,
      nickname: nickname.value.trim() || undefined,
      bio: bio.value.trim() || undefined,
      avatar: avatar.value || undefined,
    })
    toast.success('注册成功，请登录')
    router.replace({ name: 'login' })
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '注册失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="register">
    <h1 class="register__title">注册</h1>

    <div class="register__avatar" @click="pickAvatar">
      <UserAvatar :src="avatar || undefined" :name="nickname || undefined" :size="72" />
      <span class="register__avatar-hint">{{ avatarUploading ? '上传中…' : '点击换头像' }}</span>
      <input
        ref="avatarInput"
        class="sr-only"
        type="file"
        accept="image/png,image/jpeg,image/webp,image/gif"
        @change="onAvatarChange"
      />
    </div>

    <BaseInput v-model="uid" label="用户名" placeholder="3~20 位字母数字下划线" />
    <BaseInput v-model="password" label="密码" type="password" placeholder="6~32 位" />
    <BaseInput v-model="confirmPassword" label="确认密码" type="password" placeholder="再次输入密码" />
    <BaseInput v-model="nickname" label="昵称" placeholder="选填，默认「新用户」" />
    <BaseTextarea v-model="bio" placeholder="个人简介（选填）" :rows="3" :maxlength="200" />

    <BaseButton block size="lg" :loading="submitting" @click="submit">注册</BaseButton>

    <p class="register__links">
      已有账号？<RouterLink class="link" to="/login">去登录</RouterLink>
    </p>
  </main>
</template>

<style scoped>
.register {
  max-width: 400px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-4) var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.register__title {
  font-size: 22px;
  font-weight: 700;
}

.register__avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
}

.register__avatar-hint {
  font-size: 12px;
  color: var(--c-text-3);
}

.register__links {
  text-align: center;
  font-size: 13px;
  color: var(--c-text-2);
}

.link {
  color: var(--c-brand);
}
</style>
