<script setup lang="ts">
import { useToastStore, type ToastType } from '@/stores/toast'

const toast = useToastStore()

const icon: Record<ToastType, string> = {
  info: 'ℹ',
  success: '✓',
  error: '✕',
}
</script>

<template>
  <Teleport to="body">
    <TransitionGroup name="toast" tag="div" class="toast-host" aria-live="polite">
      <div
        v-for="item in toast.items"
        :key="item.id"
        class="toast"
        :class="`toast--${item.type}`"
        role="status"
      >
        <span class="toast__icon" aria-hidden="true">{{ icon[item.type] }}</span>
        <span class="toast__msg">{{ item.message }}</span>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<style scoped>
.toast-host {
  position: fixed;
  top: var(--space-4);
  left: 50%;
  transform: translateX(-50%);
  z-index: 3000;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  max-width: 90vw;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-m);
  background: var(--c-bg-card);
  color: var(--c-text-1);
  box-shadow: var(--shadow-l);
  font-size: 14px;
  pointer-events: auto;
}

.toast__icon {
  width: 18px;
  height: 18px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-full);
  color: var(--c-text-inverse);
  font-size: 12px;
  flex-shrink: 0;
}

.toast--info .toast__icon {
  background: var(--c-text-3);
}
.toast--success .toast__icon {
  background: var(--c-success);
}
.toast--error .toast__icon {
  background: var(--c-danger);
}

.toast-enter-active,
.toast-leave-active {
  transition:
    opacity var(--dur-base) var(--ease-out),
    transform var(--dur-base) var(--ease-out);
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
