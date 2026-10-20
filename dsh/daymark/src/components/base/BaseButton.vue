<script setup lang="ts">
interface Props {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'text'
  size?: 'sm' | 'md' | 'lg'
  block?: boolean
  loading?: boolean
  disabled?: boolean
}

withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  block: false,
  loading: false,
  disabled: false,
})

defineEmits<{ (e: 'click', ev: MouseEvent): void }>()
</script>

<template>
  <button
    class="btn"
    :class="[`btn--${variant}`, `btn--${size}`, { 'btn--block': block }]"
    :disabled="disabled || loading"
    type="button"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="btn__spinner" aria-hidden="true"></span>
    <span class="btn__label"><slot /></span>
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border-radius: var(--radius-m);
  font-weight: 500;
  line-height: 1;
  transition:
    background-color var(--dur-fast) var(--ease-out),
    opacity var(--dur-fast) var(--ease-out);
  user-select: none;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn--block {
  width: 100%;
}

/* 尺寸 */
.btn--sm {
  padding: var(--space-2) var(--space-3);
  font-size: 13px;
}
.btn--md {
  padding: var(--space-3) var(--space-4);
  font-size: 14px;
}
.btn--lg {
  padding: var(--space-3) var(--space-5);
  font-size: 16px;
}

/* 变体 */
.btn--primary {
  background: var(--c-brand);
  color: var(--c-text-inverse);
}
.btn--primary:not(:disabled):hover {
  background: var(--c-brand-hover);
}

.btn--secondary {
  background: var(--c-bg-card);
  color: var(--c-text-1);
  border: 1px solid var(--c-border);
}
.btn--secondary:not(:disabled):hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.btn--ghost {
  background: transparent;
  color: var(--c-text-2);
}
.btn--ghost:not(:disabled):hover {
  color: var(--c-text-1);
}

.btn--danger {
  background: var(--c-danger);
  color: var(--c-text-inverse);
}

.btn--text {
  background: transparent;
  color: var(--c-brand);
  padding-inline: 0;
}

/* loading 指示器 */
.btn__spinner {
  width: 14px;
  height: 14px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: var(--radius-full);
  animation: btn-spin 0.7s linear infinite;
}

@keyframes btn-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
