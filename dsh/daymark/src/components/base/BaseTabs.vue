<script setup lang="ts">
interface TabItem {
  key: string
  label: string
}

interface Props {
  modelValue: string
  items: TabItem[]
}

defineProps<Props>()

const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

function select(key: string) {
  emit('update:modelValue', key)
}
</script>

<template>
  <div class="tabs" role="tablist">
    <button
      v-for="item in items"
      :key="item.key"
      class="tab"
      :class="{ 'tab--active': item.key === modelValue }"
      type="button"
      role="tab"
      :aria-selected="item.key === modelValue"
      @click="select(item.key)"
    >
      {{ item.label }}
    </button>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: var(--space-1);
  padding: var(--space-1);
  background: var(--c-bg-page);
  border-radius: var(--radius-m);
}

.tab {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-s);
  color: var(--c-text-2);
  font-size: 14px;
  transition:
    background-color var(--dur-fast) var(--ease-out),
    color var(--dur-fast) var(--ease-out);
}

.tab--active {
  background: var(--c-bg-card);
  color: var(--c-text-1);
  font-weight: 500;
  box-shadow: var(--shadow-s);
}
</style>
