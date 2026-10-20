<script setup lang="ts">
interface Props {
  variant?: 'text' | 'card' | 'avatar' | 'image'
  /** text 变体的行数 */
  rows?: number
  /** 自定义宽度（如 120px / 60%） */
  width?: string
}

withDefaults(defineProps<Props>(), {
  variant: 'text',
  rows: 3,
  width: undefined,
})
</script>

<template>
  <div class="skeleton" :class="`skeleton--${variant}`" :style="width ? { width } : undefined">
    <template v-if="variant === 'text'">
      <div v-for="i in rows" :key="i" class="skeleton__line" :style="{ width: `${100 - (i % 3) * 12}%` }"></div>
    </template>
  </div>
</template>

<style scoped>
.skeleton {
  position: relative;
  overflow: hidden;
  background: #ececf0;
}

.skeleton::after {
  content: '';
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  animation: skeleton-shimmer 1.4s infinite;
}

@keyframes skeleton-shimmer {
  100% {
    transform: translateX(100%);
  }
}

.skeleton--text {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
}
.skeleton__line {
  height: 14px;
  border-radius: var(--radius-s);
  background: #e2e2e7;
}

.skeleton--card {
  height: 180px;
  border-radius: var(--radius-l);
}

.skeleton--avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
}

.skeleton--image {
  aspect-ratio: 1;
  border-radius: var(--radius-m);
}
</style>
