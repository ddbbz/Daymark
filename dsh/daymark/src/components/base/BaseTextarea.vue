<script setup lang="ts">
interface Props {
  modelValue: string
  placeholder?: string
  rows?: number
  maxlength?: number
  disabled?: boolean
  label?: string
  error?: string | null
}

withDefaults(defineProps<Props>(), {
  placeholder: '',
  rows: 4,
  maxlength: undefined,
  disabled: false,
  label: undefined,
  error: null,
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

function onInput(e: Event) {
  emit('update:modelValue', (e.target as HTMLTextAreaElement).value)
}
</script>

<template>
  <div class="field">
    <label v-if="label" class="field__label">{{ label }}</label>
    <textarea
      class="textarea"
      :class="{ 'textarea--error': !!error }"
      :value="modelValue"
      :placeholder="placeholder"
      :rows="rows"
      :maxlength="maxlength"
      :disabled="disabled"
      @input="onInput"
    ></textarea>
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

.textarea {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-m);
  background: var(--c-bg-card);
  color: var(--c-text-1);
  resize: vertical;
  line-height: 1.6;
  transition: border-color var(--dur-fast) var(--ease-out);
}

.textarea::placeholder {
  color: var(--c-text-3);
}

.textarea:focus {
  outline: none;
  border-color: var(--c-brand);
}

.textarea:disabled {
  background: var(--c-bg-page);
  color: var(--c-text-3);
}

.textarea--error {
  border-color: var(--c-danger);
}

.field__error {
  font-size: 12px;
  color: var(--c-danger);
}
</style>
