<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import { postApi } from '@/api/modules/post'
import { useImageUpload } from '@/composables/useUpload'
import { useAuthStore } from '@/stores/auth'
import { usePostDetailStore } from '@/stores/postDetail'
import { useToastStore } from '@/stores/toast'
import { resolveAssetUrl } from '@/utils/assetUrl'
import { MAX_POST_IMAGES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const postDetail = usePostDetailStore()
const toast = useToastStore()
const { validateList } = useImageUpload()

const editId = computed<number | null>(() => {
  const v = route.query.edit
  return typeof v === 'string' && v ? Number(v) : null
})
const editing = computed(() => (editId.value != null ? postDetail.byId(editId.value) : null))

const title = ref('')
const content = ref('')
const images = ref<string[]>([])
const uploading = ref(false)
const submitting = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

onMounted(() => {
  const p = editing.value
  if (p) {
    title.value = p.title ?? ''
    content.value = p.content ?? ''
    images.value = p.images ?? []
  }
})

function pick() {
  fileInput.value?.click()
}

async function onFiles(e: Event) {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  input.value = ''
  if (!files.length) return

  const err = validateList(files)
  if (err) {
    toast.error(err)
    return
  }

  const remaining = MAX_POST_IMAGES - images.value.length
  const toUpload = files.slice(0, remaining)
  if (toUpload.length !== files.length) toast.info(`最多 ${MAX_POST_IMAGES} 张，已截取`)
  if (!toUpload.length) return

  uploading.value = true
  try {
    const urls = await postApi.uploadImages(toUpload)
    images.value = [...images.value, ...urls]
  } catch (e2) {
    toast.error(e2 instanceof Error ? e2.message : '上传失败')
  } finally {
    uploading.value = false
  }
}

function removeImage(i: number) {
  images.value = images.value.filter((_, idx) => idx !== i)
}

async function submit() {
  const t = title.value.trim()
  const c = content.value.trim()
  if (!t && !c) {
    toast.error('标题和正文至少填一项')
    return
  }
  if (submitting.value) return

  // 编辑需要数字 id（PostDTO.userId 是原始 int，缺省反序列化为 0 会报「作者不匹配」）
  if (editing.value && auth.userId === 0) {
    await auth.loadMe({ silent: true })
  }

  submitting.value = true
  try {
    if (editing.value) {
      await postApi.update({
        id: editing.value.id,
        userId: auth.userId,
        title: t,
        content: c,
        image: images.value,
      })
      toast.success('已提交修改，等待重新审核')
    } else {
      await postApi.publish({ title: t, content: c, image: images.value })
      toast.success('发布成功，等待审核')
    }
    router.replace({ name: 'home' })
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="publish">
    <h1 class="publish__title">{{ editing ? '编辑帖子' : '发布帖子' }}</h1>

    <BaseInput v-model="title" label="标题" placeholder="1~50 字" :maxlength="50" />
    <BaseTextarea v-model="content" label="正文" placeholder="1~2000 字" :rows="6" :maxlength="2000" />

    <div class="publish__images">
      <div v-for="(img, i) in images" :key="i" class="publish__thumb">
        <img :src="resolveAssetUrl(img)" alt="" />
        <button class="publish__remove" type="button" aria-label="移除" @click="removeImage(i)">✕</button>
      </div>
      <button
        v-if="images.length < MAX_POST_IMAGES"
        class="publish__add"
        type="button"
        :disabled="uploading"
        @click="pick"
      >
        {{ uploading ? '上传中…' : '+ 图片' }}
      </button>
    </div>

    <input
      ref="fileInput"
      class="sr-only"
      type="file"
      accept="image/png,image/jpeg,image/webp,image/gif"
      multiple
      @change="onFiles"
    />

    <BaseButton block size="lg" :loading="submitting" @click="submit">
      {{ editing ? '保存修改' : '发布' }}
    </BaseButton>

    <p v-if="editing" class="publish__hint">编辑后帖子将重新进入审核，且点赞/评论/收藏计数会被重置。</p>
  </main>
</template>

<style scoped>
.publish {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.publish__title {
  font-size: 20px;
  font-weight: 700;
}

.publish__images {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.publish__thumb {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: var(--radius-m);
  overflow: hidden;
  border: 1px solid var(--c-border);
}
.publish__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.publish__remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-full);
  background: var(--c-bg-mask);
  color: #fff;
  font-size: 12px;
}

.publish__add {
  width: 88px;
  height: 88px;
  display: grid;
  place-items: center;
  border: 1px dashed var(--c-border);
  border-radius: var(--radius-m);
  color: var(--c-text-3);
  font-size: 13px;
}
.publish__add:disabled {
  opacity: 0.5;
}

.publish__hint {
  font-size: 12px;
  color: var(--c-text-3);
}
</style>
