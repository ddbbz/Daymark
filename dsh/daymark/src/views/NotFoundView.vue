<template>
  <main class="page">
    <h1>404</h1>
    <p class="muted">页面不存在。</p>
    <RouterLink class="link" to="/">返回首页</RouterLink>
  </main>
</template>

<style scoped>
.page {
  padding: var(--space-6);
  text-align: center;
}
.muted {
  color: var(--c-text-2);
}
.link {
  color: var(--c-brand);
}
</style>
