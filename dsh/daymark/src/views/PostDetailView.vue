<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import BaseButton from '@/components/base/BaseButton.vue'
import BaseEmpty from '@/components/base/BaseEmpty.vue'
import CommentList from '@/components/comment/CommentList.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import PostImages from '@/components/post/PostImages.vue'
import { postApi } from '@/api/modules/post'
import { PostStatus } from '@/api/types/enums'
import { useAuthStore } from '@/stores/auth'
import { useInteractionStore } from '@/stores/interaction'
import { usePostDetailStore } from '@/stores/postDetail'
import { useToastStore } from '@/stores/toast'
import { displayName, fmtCount, fmtRelative } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const interaction = useInteractionStore()
const postDetail = usePostDetailStore()
const toast = useToastStore()

const postId = computed(() => Number(route.params.id))
const post = computed(() => postDetail.byId(postId.value))
const isOwner = computed(() => post.value != null && post.value.userId === auth.userId)

const commentCount = ref(0)
const confirmDelete = ref(false)

onMounted(() => {
  const p = post.value
  if (p) {
    commentCount.value = p.commentCount
    void interaction.ensureLike(p.id)
    void interaction.ensureFavorite(p.id)
  }
})

async function toggleLike() {
  const p = post.value
  if (!p) return
  try {
    await interaction.toggleLike(p.id)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function toggleFavorite() {
  const p = post.value
  if (!p) return
  try {
    await interaction.toggleFavorite(p.id)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '操作失败')
  }
}

function goEdit() {
  const p = post.value
  if (!p) return
  postDetail.cachePost(p)
  router.push({ name: 'publish', query: { edit: String(p.id) } })
}

async function doDelete() {
  const p = post.value
  if (!p) return
  try {
    await postApi.updateStatus(p.id, PostStatus.privacy)
    toast.success('已删除')
    confirmDelete.value = false
    router.replace({ name: 'home' })
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '删除失败')
  }
}
</script>

<template>
  <main class="detail">
    <!-- 后端无 GET /post/{id}：直接刷新/分享链接时缓存丢失，降级展示 -->
    <template v-if="post">
      <header class="detail__head">
        <span class="detail__author">{{ displayName(null, post.userId) }}</span>
        <span class="detail__time">{{ fmtRelative(post.createdTime) }}</span>
      </header>

      <h1 v-if="post.title" class="detail__title">{{ post.title }}</h1>
      <p v-if="post.content" class="detail__content">{{ post.content }}</p>
      <PostImages :images="post.images" />

      <div class="detail__actions">
        <BaseButton
          :variant="interaction.liked(post.id) ? 'primary' : 'secondary'"
          size="sm"
          :disabled="interaction.likePending(post.id)"
          @click="toggleLike"
        >
          {{ interaction.liked(post.id) ? '♥' : '♡' }}
          {{ fmtCount(post.likeCount + interaction.likeDelta(post.id)) }}
        </BaseButton>
        <BaseButton
          :variant="interaction.favorited(post.id) ? 'primary' : 'secondary'"
          size="sm"
          :disabled="interaction.favoritePending(post.id)"
          @click="toggleFavorite"
        >
          {{ interaction.favorited(post.id) ? '★' : '☆' }}
          {{ fmtCount(post.favoriteCount + interaction.favoriteDelta(post.id)) }}
        </BaseButton>
        <span class="detail__count">💬 {{ fmtCount(commentCount) }}</span>

        <span v-if="isOwner" class="detail__owner-actions">
          <BaseButton size="sm" variant="ghost" @click="goEdit">编辑</BaseButton>
          <BaseButton size="sm" variant="text" @click="confirmDelete = true">删除</BaseButton>
        </span>
      </div>

      <section class="detail__comments">
        <h2 class="detail__comments-title">评论</h2>
        <CommentList v-model:count="commentCount" :post-id="post.id" />
      </section>
    </template>

    <BaseEmpty v-else text="无法直接访问该帖子（后端无按 id 查帖接口）">
      <BaseButton @click="router.replace({ name: 'home' })">返回首页</BaseButton>
    </BaseEmpty>

    <ConfirmDialog
      :open="confirmDelete"
      title="删除帖子"
      message="删除后不可恢复，确定删除吗？"
      confirm-text="删除"
      danger
      @confirm="doDelete"
      @cancel="confirmDelete = false"
    />
  </main>
</template>

<style scoped>
.detail {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.detail__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail__author {
  font-size: 13px;
  color: var(--c-text-2);
}

.detail__time {
  font-size: 12px;
  color: var(--c-text-3);
}

.detail__title {
  font-size: 20px;
  font-weight: 700;
}

.detail__content {
  font-size: 15px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail__actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.detail__count {
  color: var(--c-text-2);
  font-size: 13px;
}

.detail__owner-actions {
  margin-left: auto;
  display: flex;
  gap: var(--space-2);
}

.detail__comments {
  margin-top: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--c-border);
}

.detail__comments-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: var(--space-3);
}
</style>
