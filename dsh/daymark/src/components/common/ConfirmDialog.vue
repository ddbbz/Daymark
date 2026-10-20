<script setup lang="ts">
import BaseButton from '@/components/base/BaseButton.vue'
import BaseModal from '@/components/base/BaseModal.vue'

interface Props {
  open: boolean
  title?: string
  message?: string
  confirmText?: string
  cancelText?: string
  danger?: boolean
}

withDefaults(defineProps<Props>(), {
  title: '确认操作',
  message: '',
  confirmText: '确定',
  cancelText: '取消',
  danger: false,
})

const emit = defineEmits<{ (e: 'confirm'): void; (e: 'cancel'): void }>()
</script>

<template>
  <BaseModal :open="open" :title="title" @close="emit('cancel')">
    <p v-if="message" class="confirm__msg">{{ message }}</p>
    <template #footer>
      <BaseButton block variant="secondary" @click="emit('cancel')">{{ cancelText }}</BaseButton>
      <BaseButton block :variant="danger ? 'danger' : 'primary'" @click="emit('confirm')">
        {{ confirmText }}
      </BaseButton>
    </template>
  </BaseModal>
</template>

<style scoped>
.confirm__msg {
  color: var(--c-text-2);
  line-height: 1.6;
  word-break: break-word;
}
</style>
