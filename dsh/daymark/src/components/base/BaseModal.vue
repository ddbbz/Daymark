<script setup lang="ts">
interface Props {
  open: boolean
  title?: string
  closeable?: boolean
}

withDefaults(defineProps<Props>(), {
  title: '',
  closeable: true,
})

const emit = defineEmits<{ (e: 'close'): void }>()

function onOverlay() {
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="open" class="modal-overlay" @click.self="onOverlay">
        <div class="modal-panel" role="dialog" aria-modal="true">
          <header class="modal-header">
            <slot name="header">
              <h2 class="modal-title">{{ title }}</h2>
            </slot>
            <button
              v-if="closeable"
              class="modal-close"
              type="button"
              aria-label="关闭"
              @click="emit('close')"
            >
              ✕
            </button>
          </header>
          <div class="modal-body">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  background: var(--c-bg-mask);
}

.modal-panel {
  width: 100%;
  max-width: 360px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-l);
  background: var(--c-bg-card);
  box-shadow: var(--shadow-l);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
}

.modal-close {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-full);
  color: var(--c-text-3);
  font-size: 14px;
}
.modal-close:hover {
  background: var(--c-bg-page);
  color: var(--c-text-1);
}

.modal-body {
  padding: 0 var(--space-4) var(--space-4);
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4);
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity var(--dur-base) var(--ease-out);
}
.modal-enter-active .modal-panel,
.modal-leave-active .modal-panel {
  transition: transform var(--dur-base) var(--ease-out);
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .modal-panel,
.modal-leave-to .modal-panel {
  transform: scale(0.95);
}
</style>
