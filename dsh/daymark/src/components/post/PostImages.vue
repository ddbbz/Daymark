<script setup lang="ts">
import { ref } from 'vue'

import ImageLightbox from '@/components/common/ImageLightbox.vue'
import { resolveAssetUrl } from '@/utils/assetUrl'

const props = defineProps<{ images: string[] | null }>()

const open = ref(false)
const index = ref(0)

function show(i: number) {
  index.value = i
  open.value = true
}
</script>

<template>
  <div v-if="props.images && props.images.length" class="post-images" :class="`post-images--${Math.min(props.images.length, 9)}`">
    <img
      v-for="(img, i) in props.images.slice(0, 9)"
      :key="i"
      class="post-images__item"
      :src="resolveAssetUrl(img)"
      alt=""
      loading="lazy"
      @click.stop="show(i)"
    />
    <ImageLightbox v-model:index="index" :open="open" :images="props.images" @close="open = false" />
  </div>
</template>

<style scoped>
.post-images {
  display: grid;
  gap: 4px;
  margin-top: var(--space-3);
}

.post-images--1 {
  grid-template-columns: 1fr;
}
.post-images--2,
.post-images--4 {
  grid-template-columns: repeat(2, 1fr);
}
.post-images--3,
.post-images--5,
.post-images--6,
.post-images--7,
.post-images--8,
.post-images--9 {
  grid-template-columns: repeat(3, 1fr);
}

.post-images__item {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: var(--radius-s);
  cursor: zoom-in;
  background: var(--c-bg-page);
}

.post-images--1 .post-images__item {
  aspect-ratio: 16 / 9;
}
</style>
