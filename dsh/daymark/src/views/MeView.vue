<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseEmpty from '@/components/base/BaseEmpty.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSkeleton from '@/components/base/BaseSkeleton.vue'
import BaseTabs from '@/components/base/BaseTabs.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { postApi } from '@/api/modules/post'
import { userApi } from '@/api/modules/user'
import type { PostVO } from '@/api/types/post'
import type { UserVO } from '@/api/types/user'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { fmtRelative, postStatusMeta } from '@/utils/format'
import { firstError, passwordSchema } from '@/utils/validate'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const tabs = [
  { key: 'profile', label: '资料' },
  { key: 'password', label: '改密' },
  { key: 'posts', label: '我的帖子' },
]
const activeTab = ref('profile')

// 资料
const nickname = ref('')
const bio = ref('')
const avatar = ref('')
const profileSaving = ref(false)
const avatarUploading = ref(false)
const avatarInput = ref<HTMLInputElement | null>(null)

// 改密
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const passwordSaving = ref(false)

// 我的帖子
const posts = ref<PostVO[]>([])
const postsLoading = ref(false)

const myUid = computed(() => auth.user?.uid ?? '')

onMounted(loadMe)

watch(activeTab, (tab) => {
  if (tab === 'posts') loadPosts()
})

async function loadMe() {
  try {
    const user = await auth.loadMe({ silent: true })
    if (user) syncProfile(user)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '获取用户信息失败')
  }
}

function syncProfile(user: UserVO) {
  nickname.value = user.nickname ?? ''
  bio.value = user.bio ?? ''
  avatar.value = user.avatar ?? ''
}

function pickAvatar() {
  avatarInput.value?.click()
}

async function onAvatarChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  avatarUploading.value = true
  try {
    avatar.value = await userApi.uploadAvatar(file)
    toast.success('头像上传成功，记得保存资料')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : '头像上传失败')
  } finally {
    avatarUploading.value = false
    input.value = ''
  }
}

async function saveProfile() {
  profileSaving.value = true
  try {
    await userApi.updateProfile({
      nickname: nickname.value.trim() || undefined,
      bio: bio.value.trim() || undefined,
      avatar: avatar.value || undefined,
    })
    toast.success('资料已保存')
    await auth.refreshMe()
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    profileSaving.value = false
  }
}

async function changePassword() {
  if (newPassword.value !== confirmPassword.value) {
    toast.error('两次输入的新密码不一致')
    return
  }
  const parsed = passwordSchema.safeParse({
    password: oldPassword.value,
    newPassword: newPassword.value,
  })
  if (!parsed.success) {
    toast.error(firstError(parsed) ?? '输入有误')
    return
  }

  passwordSaving.value = true
  try {
    await userApi.updatePassword(oldPassword.value, newPassword.value)
    toast.success('密码已修改，请重新登录')
    auth.logout()
    router.replace({ name: 'login' })
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '修改密码失败')
  } finally {
    passwordSaving.value = false
  }
}

async function loadPosts() {
  if (!myUid.value || postsLoading.value) return
  postsLoading.value = true
  try {
    posts.value = await postApi.byUid(myUid.value)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '获取帖子失败')
  } finally {
    postsLoading.value = false
  }
}

function logout() {
  auth.logout()
  router.replace({ name: 'login' })
}
</script>

<template>
  <main class="me">
    <header class="me__head">
      <UserAvatar :src="avatar || undefined" :name="nickname || undefined" :size="56" />
      <div class="me__head-info">
        <p class="me__name">{{ nickname || '未设置昵称' }}</p>
        <p class="me__uid">UID：{{ auth.user?.uid ?? '—' }}</p>
      </div>
      <BaseButton size="sm" variant="ghost" @click="logout">退出</BaseButton>
    </header>

    <BaseTabs v-model="activeTab" :items="tabs" />

    <section v-if="activeTab === 'profile'" class="me__panel">
      <div class="me__avatar" @click="pickAvatar">
        <UserAvatar :src="avatar || undefined" :name="nickname || undefined" :size="72" />
        <span class="me__avatar-hint">{{ avatarUploading ? '上传中…' : '更换头像' }}</span>
        <input
          ref="avatarInput"
          class="sr-only"
          type="file"
          accept="image/png,image/jpeg,image/webp,image/gif"
          @change="onAvatarChange"
        />
      </div>
      <BaseInput v-model="nickname" label="昵称" placeholder="1~20 字" />
      <BaseTextarea v-model="bio" label="简介" placeholder="介绍一下自己" :rows="3" :maxlength="200" />
      <BaseButton block :loading="profileSaving" @click="saveProfile">保存资料</BaseButton>
    </section>

    <section v-else-if="activeTab === 'password'" class="me__panel">
      <BaseInput v-model="oldPassword" label="原密码" type="password" placeholder="6~32 位" />
      <BaseInput v-model="newPassword" label="新密码" type="password" placeholder="6~32 位" />
      <BaseInput
        v-model="confirmPassword"
        label="确认新密码"
        type="password"
        placeholder="再次输入新密码"
      />
      <BaseButton block :loading="passwordSaving" @click="changePassword">修改密码</BaseButton>
    </section>

    <section v-else class="me__panel">
      <BaseSkeleton v-if="postsLoading" variant="text" :rows="3" />
      <template v-else-if="posts.length">
        <ul class="me__posts">
          <li v-for="post in posts" :key="post.id" class="post-item">
            <div class="post-item__main">
              <p class="post-item__title line-clamp-1">{{ post.title || '（无标题）' }}</p>
              <p class="post-item__time">{{ fmtRelative(post.createdTime) }}</p>
            </div>
            <BaseBadge :tone="postStatusMeta[post.status].tone">
              {{ postStatusMeta[post.status].label }}
            </BaseBadge>
          </li>
        </ul>
      </template>
      <BaseEmpty v-else text="还没有发布过帖子" />
    </section>
  </main>
</template>

<style scoped>
.me {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.me__head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.me__head-info {
  flex: 1;
  min-width: 0;
}

.me__name {
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.me__uid {
  font-size: 12px;
  color: var(--c-text-3);
}

.me__panel {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.me__avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
}

.me__avatar-hint {
  font-size: 12px;
  color: var(--c-text-3);
}

.me__posts {
  display: flex;
  flex-direction: column;
}

.post-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--c-border);
}

.post-item__main {
  flex: 1;
  min-width: 0;
}

.post-item__title {
  font-size: 14px;
}

.post-item__time {
  font-size: 12px;
  color: var(--c-text-3);
  margin-top: 2px;
}
</style>
