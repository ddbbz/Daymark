<script setup lang="ts">
withDefaults(defineProps<{ text?: string }>(), { text: '暂无数据' })
</script>

<template>
  <div class="empty">
    <svg class="empty__icon" viewBox="0 0 48 48" aria-hidden="true">
      <rect width="48" height="48" rx="12" fill="var(--c-bg-page)" />
      <circle cx="24" cy="19" r="7" fill="var(--c-text-3)" opacity="0.5" />
      <path
        d="M12 38c0-7 5-11 12-11s12 4 12 11"
        fill="var(--c-text-3)"
        opacity="0.5"
      />
    </svg>
    <p class="empty__text">{{ text }}</p>
    <div v-if="$slots.default" class="empty__action">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-8) var(--space-4);
}

.empty__icon {
  width: 64px;
  height: 64px;
}

.empty__text {
  color: var(--c-text-3);
  font-size: 14px;
}

.empty__action {
  margin-top: var(--space-2);
}
</style>
