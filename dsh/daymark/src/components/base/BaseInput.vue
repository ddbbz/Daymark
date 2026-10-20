<script setup lang="ts">
interface Props {
  modelValue: string
  type?: string
  placeholder?: string
  disabled?: boolean
  error?: string | null
  label?: string
  maxlength?: number
}

withDefaults(defineProps<Props>(), {
  type: 'text',
  placeholder: '',
  disabled: false,
  error: null,
  label: undefined,
  maxlength: undefined,
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void
  (e: 'blur'): void
  (e: 'enter'): void
}>()

function onInput(e: Event) {
  emit('update:modelValue', (e.target as HTMLInputElement).value)
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter') emit('enter')
}
</script>

<template>
  <div class="field">
    <label v-if="label" class="field__label">{{ label }}</label>
    <input
      class="field__input"
      :class="{ 'field__input--error': !!error }"
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :maxlength="maxlength"
      @input="onInput"
      @blur="$emit('blur')"
      @keydown="onKeydown"
    />
    <p v-if="error" class="field__error">{{ error }}</p>
  </div>
</template>

<style scoped>
.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.field__label {
  font-size: 13px;
  color: var(--c-text-2);
}

.field__input {
  padding: var(--space-3);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-m);
  background: var(--c-bg-card);
  color: var(--c-text-1);
  transition: border-color var(--dur-fast) var(--ease-out);
}

.field__input::placeholder {
  color: var(--c-text-3);
}

.field__input:focus {
  outline: none;
  border-color: var(--c-brand);
}

.field__input:disabled {
  background: var(--c-bg-page);
  color: var(--c-text-3);
}

.field__input--error {
  border-color: var(--c-danger);
}

.field__error {
  font-size: 12px;
  color: var(--c-danger);
}
</style>
