<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()

const tabs = [
  { key: 'home', label: '发现', to: '/home' },
  { key: 'publish', label: '发布', to: '/publish' },
  { key: 'me', label: '我的', to: '/me' },
]

function isActive(key: string): boolean {
  return String(route.name) === key
}
</script>

<template>
  <nav class="tabbar">
    <RouterLink
      v-for="tab in tabs"
      :key="tab.key"
      class="tabbar__item"
      :class="{ 'tabbar__item--active': isActive(tab.key) }"
      :to="tab.to"
    >
      <span class="tabbar__label">{{ tab.label }}</span>
    </RouterLink>
  </nav>
</template>

<style scoped>
.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--tabbar-h);
  display: flex;
  background: var(--c-bg-card);
  border-top: 1px solid var(--c-border);
}

.tabbar__item {
  flex: 1;
  display: grid;
  place-items: center;
  color: var(--c-text-3);
}

.tabbar__item--active {
  color: var(--c-brand);
  font-weight: 600;
}

.tabbar__label {
  font-size: 13px;
}

/* 桌面端隐藏底部标签栏（导航移到顶栏） */
@media (min-width: 768px) {
  .tabbar {
    display: none;
  }
}
</style>
