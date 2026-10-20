<script setup lang="ts">
import { useRouter } from 'vue-router'

import PostImages from '@/components/post/PostImages.vue'
import PostStatusBadge from '@/components/post/PostStatusBadge.vue'
import { PostStatus } from '@/api/types/enums'
import type { PostVO } from '@/api/types/post'
import { useInteractionStore } from '@/stores/interaction'
import { usePostDetailStore } from '@/stores/postDetail'
import { useToastStore } from '@/stores/toast'
import { displayName, fmtCount, fmtRelative } from '@/utils/format'

const props = defineProps<{ post: PostVO }>()

const router = useRouter()
const interaction = useInteractionStore()
const postDetail = usePostDetailStore()
const toast = useToastStore()

function open() {
  postDetail.cachePost(props.post)
  router.push({ name: 'post-detail', params: { id: String(props.post.id) } })
}

async function toggleLike() {
  try {
    await interaction.toggleLike(props.post.id)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function toggleFavorite() {
  try {
    await interaction.toggleFavorite(props.post.id)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '操作失败')
  }
}
</script>

<template>
  <article class="post-card" @click="open">
    <div class="post-card__head">
      <span class="post-card__author">{{ displayName(null, post.userId) }}</span>
      <PostStatusBadge v-if="post.status !== PostStatus.success" :status="post.status" />
    </div>

    <h3 v-if="post.title" class="post-card__title line-clamp-2">{{ post.title }}</h3>
    <p v-if="post.content" class="post-card__content line-clamp-3">{{ post.content }}</p>

    <PostImages :images="post.images" />

    <div class="post-card__meta">
      <span class="post-card__time">{{ fmtRelative(post.createdTime) }}</span>
      <button
        class="post-card__action"
        :class="{ 'post-card__action--active': interaction.liked(post.id) }"
        type="button"
        :disabled="interaction.likePending(post.id)"
        @click.stop="toggleLike"
      >
        {{ interaction.liked(post.id) ? '♥' : '♡' }}
        {{ fmtCount(post.likeCount + interaction.likeDelta(post.id)) }}
      </button>
      <button
        class="post-card__action"
        :class="{ 'post-card__action--active': interaction.favorited(post.id) }"
        type="button"
        :disabled="interaction.favoritePending(post.id)"
        @click.stop="toggleFavorite"
      >
        {{ interaction.favorited(post.id) ? '★' : '☆' }}
        {{ fmtCount(post.favoriteCount + interaction.favoriteDelta(post.id)) }}
      </button>
      <span class="post-card__action post-card__action--static">💬 {{ fmtCount(post.commentCount) }}</span>
    </div>
  </article>
</template>

<style scoped>
.post-card {
  padding: var(--space-4);
  background: var(--c-bg-card);
  border-radius: var(--radius-l);
  box-shadow: var(--shadow-s);
  cursor: pointer;
}

.post-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}

.post-card__author {
  font-size: 13px;
  color: var(--c-text-2);
}

.post-card__title {
  margin-top: var(--space-2);
  font-size: 16px;
  font-weight: 600;
}

.post-card__content {
  margin-top: var(--space-1);
  color: var(--c-text-2);
  font-size: 14px;
}

.post-card__meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-top: var(--space-3);
  color: var(--c-text-3);
  font-size: 13px;
}

.post-card__time {
  margin-right: auto;
}

.post-card__action {
  color: var(--c-text-3);
  font-size: 13px;
}
.post-card__action--active {
  color: var(--c-brand);
}
.post-card__action--static {
  cursor: default;
}
</style>
