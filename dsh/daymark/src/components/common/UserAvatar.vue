<script setup lang="ts">
import { computed, ref } from 'vue'

import { PLACEHOLDER_AVATAR, resolveAssetUrl } from '@/utils/assetUrl'

interface Props {
  src?: string | null
  name?: string
  size?: number
}

const props = withDefaults(defineProps<Props>(), {
  src: null,
  name: '',
  size: 40,
})

const failed = ref(false)

const showImg = computed(() => !!props.src && !failed.value)
const initial = computed(() => (props.name ? props.name.charAt(0).toUpperCase() : ''))

function onError() {
  failed.value = true
}
</script>

<template>
  <div
    class="avatar"
    :style="{ width: `${size}px`, height: `${size}px`, fontSize: `${Math.round(size * 0.4)}px` }"
  >
    <img
      v-if="showImg"
      class="avatar__img"
      :src="resolveAssetUrl(src)"
      :alt="name"
      @error="onError"
    />
    <span v-else-if="initial" class="avatar__fallback">{{ initial }}</span>
    <img v-else class="avatar__img" :src="PLACEHOLDER_AVATAR" alt="" />
  </div>
</template>

<style scoped>
.avatar {
  display: grid;
  place-items: center;
  border-radius: var(--radius-full);
  overflow: hidden;
  background: var(--c-bg-page);
  flex-shrink: 0;
}

.avatar__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar__fallback {
  color: var(--c-text-3);
  font-weight: 600;
}
</style>
