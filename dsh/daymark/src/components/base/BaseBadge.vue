<script setup lang="ts">
interface Props {
  tone?: 'default' | 'success' | 'warning' | 'danger'
}

withDefaults(defineProps<Props>(), { tone: 'default' })
</script>

<template>
  <span class="badge" :class="`badge--${tone}`"><slot /></span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  padding: 1px 8px;
  border-radius: var(--radius-full);
  font-size: 12px;
  line-height: 1.6;
}

.badge--default {
  background: var(--c-bg-page);
  color: var(--c-text-2);
}
.badge--success {
  background: var(--c-brand-soft);
  color: var(--c-brand);
}
.badge--warning {
  background: #fff6e8;
  color: var(--c-warning);
}
.badge--danger {
  background: #ffebee;
  color: var(--c-danger);
}
</style>
