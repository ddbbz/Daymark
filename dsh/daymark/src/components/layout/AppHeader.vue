<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'

import UserAvatar from '@/components/common/UserAvatar.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()

const nav = [
  { name: 'home', label: '发现', to: '/home' },
  { name: 'publish', label: '发布', to: '/publish' },
  { name: 'me', label: '我的', to: '/me' },
]
</script>

<template>
  <header class="header">
    <RouterLink class="header__brand" to="/home">Daymark</RouterLink>

    <nav class="header__nav">
      <RouterLink
        v-for="item in nav"
        :key="item.name"
        class="header__nav-link"
        :class="{ 'header__nav-link--active': route.name === item.name }"
        :to="item.to"
      >
        {{ item.label }}
      </RouterLink>
    </nav>

    <RouterLink v-if="auth.isLoggedIn" class="header__me" to="/me">
      <UserAvatar :src="auth.user?.avatar" :name="auth.user?.nickname ?? undefined" :size="28" />
    </RouterLink>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: var(--header-h);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: 0 var(--space-4);
  background: var(--c-bg-card);
  border-bottom: 1px solid var(--c-border);
}

.header__brand {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-brand);
}

.header__nav {
  display: none;
}

.header__me {
  margin-left: auto;
  display: grid;
  place-items: center;
}

/* 桌面端：顶栏显示导航，底部标签栏隐藏 */
@media (min-width: 768px) {
  .header__nav {
    display: flex;
    gap: var(--space-4);
  }

  .header__nav-link {
    color: var(--c-text-2);
    font-size: 14px;
  }
  .header__nav-link--active {
    color: var(--c-brand);
    font-weight: 600;
  }
}
</style>
