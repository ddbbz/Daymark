<script setup lang="ts">
import { onMounted, ref } from 'vue'

import BaseButton from '@/components/base/BaseButton.vue'
import BaseEmpty from '@/components/base/BaseEmpty.vue'
import BaseSkeleton from '@/components/base/BaseSkeleton.vue'
import PostImages from '@/components/post/PostImages.vue'
import { reviewApi } from '@/api/modules/review'
import { PostStatus, type PostStatusValue } from '@/api/types/enums'
import type { PostVO } from '@/api/types/post'
import { useToastStore } from '@/stores/toast'
import { fmtRelative } from '@/utils/format'

const toast = useToastStore()

const list = ref<PostVO[]>([])
const loading = ref(false)
const decidingId = ref<number | null>(null)

onMounted(load)

async function load() {
  loading.value = true
  try {
    list.value = await reviewApi.pending()
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '加载待审帖子失败')
  } finally {
    loading.value = false
  }
}

async function decide(post: PostVO, status: PostStatusValue) {
  decidingId.value = post.id
  try {
    await reviewApi.decide(post.id, status)
    toast.success(status === PostStatus.success ? '已通过' : '已驳回')
    list.value = list.value.filter((p) => p.id !== post.id)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    decidingId.value = null
  }
}
</script>

<template>
  <main class="review">
    <h1 class="review__title">审核台</h1>

    <BaseSkeleton v-if="loading" variant="text" :rows="3" />

    <template v-else-if="list.length">
      <article v-for="post in list" :key="post.id" class="review-item">
        <div class="review-item__head">
          <span class="review-item__author">用户 {{ post.userId }}</span>
          <span class="review-item__time">{{ fmtRelative(post.createdTime) }}</span>
        </div>
        <h2 v-if="post.title" class="review-item__title">{{ post.title }}</h2>
        <p v-if="post.content" class="review-item__content line-clamp-3">{{ post.content }}</p>
        <PostImages :images="post.images" />
        <!-- 后端 GET /review 不填计数（恒为 0），故不显示点赞/评论/收藏数 -->
        <div class="review-item__actions">
          <BaseButton
            size="sm"
            variant="secondary"
            :loading="decidingId === post.id"
            @click="decide(post, PostStatus.failed)"
          >
            驳回
          </BaseButton>
          <BaseButton
            size="sm"
            :loading="decidingId === post.id"
            @click="decide(post, PostStatus.success)"
          >
            通过
          </BaseButton>
        </div>
      </article>
    </template>

    <BaseEmpty v-else text="没有待审核的帖子" />
  </main>
</template>

<style scoped>
.review {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.review__title {
  font-size: 20px;
  font-weight: 700;
}

.review-item {
  padding: var(--space-4);
  background: var(--c-bg-card);
  border-radius: var(--radius-l);
  box-shadow: var(--shadow-s);
}

.review-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.review-item__author {
  font-size: 13px;
  color: var(--c-text-2);
}

.review-item__time {
  font-size: 12px;
  color: var(--c-text-3);
}

.review-item__title {
  margin-top: var(--space-2);
  font-size: 16px;
  font-weight: 600;
}

.review-item__content {
  margin-top: var(--space-1);
  color: var(--c-text-2);
  font-size: 14px;
}

.review-item__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  margin-top: var(--space-3);
}
</style>
