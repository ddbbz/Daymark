<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'

import { resolveAssetUrl } from '@/utils/assetUrl'

interface Props {
  open: boolean
  images: string[]
  index: number
}

const props = withDefaults(defineProps<Props>(), { index: 0 })

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update:index', v: number): void
}>()

const current = computed(() => props.images[props.index] ?? '')
const hasMany = computed(() => props.images.length > 1)

function prev() {
  if (!props.images.length) return
  emit('update:index', (props.index - 1 + props.images.length) % props.images.length)
}

function next() {
  if (!props.images.length) return
  emit('update:index', (props.index + 1) % props.images.length)
}

function onKeydown(e: KeyboardEvent) {
  if (!props.open) return
  if (e.key === 'Escape') emit('close')
  else if (e.key === 'ArrowLeft') prev()
  else if (e.key === 'ArrowRight') next()
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Teleport to="body">
    <Transition name="lightbox">
      <div v-if="open" class="lightbox" @click.self="emit('close')">
        <button class="lightbox__close" type="button" aria-label="关闭" @click="emit('close')">✕</button>

        <button
          v-if="hasMany"
          class="lightbox__nav lightbox__nav--prev"
          type="button"
          aria-label="上一张"
          @click.stop="prev"
        >
          ‹
        </button>

        <img class="lightbox__img" :src="resolveAssetUrl(current)" alt="" />

        <button
          v-if="hasMany"
          class="lightbox__nav lightbox__nav--next"
          type="button"
          aria-label="下一张"
          @click.stop="next"
        >
          ›
        </button>

        <div v-if="hasMany" class="lightbox__counter">{{ index + 1 }} / {{ images.length }}</div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.lightbox {
  position: fixed;
  inset: 0;
  z-index: 2500;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.92);
}

.lightbox__img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.lightbox__close {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 18px;
}

.lightbox__nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 30px;
  line-height: 1;
}
.lightbox__nav--prev {
  left: var(--space-4);
}
.lightbox__nav--next {
  right: var(--space-4);
}

.lightbox__counter {
  position: absolute;
  bottom: var(--space-6);
  left: 50%;
  transform: translateX(-50%);
  color: #fff;
  font-size: 14px;
}

.lightbox-enter-active,
.lightbox-leave-active {
  transition: opacity var(--dur-base) var(--ease-out);
}
.lightbox-enter-from,
.lightbox-leave-to {
  opacity: 0;
}
</style>
