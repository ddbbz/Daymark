<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'

import AppHeader from '@/components/layout/AppHeader.vue'
import TabBar from '@/components/layout/TabBar.vue'
import ToastHost from '@/components/common/ToastHost.vue'

const route = useRoute()

// 仅主页面（发现/发布/我的/详情）显示顶栏与底部标签栏；登录/注册等 guest 页隐藏
const showChrome = computed(() => route.meta.chrome === true)
</script>

<template>
  <div class="app-shell">
    <AppHeader v-if="showChrome" />
    <main class="app-main" :class="{ 'app-main--chrome': showChrome }">
      <RouterView />
    </main>
    <TabBar v-if="showChrome" />
    <ToastHost />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.app-main--chrome {
  padding-bottom: calc(var(--tabbar-h) + var(--space-4));
}

@media (min-width: 768px) {
  .app-main--chrome {
    padding-bottom: var(--space-4);
  }
}
</style>
