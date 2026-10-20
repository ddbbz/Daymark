<script setup lang="ts">
import { onMounted, ref } from 'vue'

import BaseButton from '@/components/base/BaseButton.vue'
import BaseEmpty from '@/components/base/BaseEmpty.vue'
import BaseSkeleton from '@/components/base/BaseSkeleton.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { commentApi } from '@/api/modules/comment'
import type { CommentVO } from '@/api/types/interaction'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { displayName, fmtRelative } from '@/utils/format'

const props = defineProps<{ postId: number; count: number }>()
const emit = defineEmits<{ (e: 'update:count', v: number): void }>()

const auth = useAuthStore()
const toast = useToastStore()

const items = ref<CommentVO[]>([])
const loading = ref(false)
const draft = ref('')
const submitting = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    items.value = await commentApi.list(props.postId)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '加载评论失败')
  } finally {
    loading.value = false
  }
}

async function submit() {
  const content = draft.value.trim()
  if (!content || submitting.value) return
  submitting.value = true
  try {
    await commentApi.create({ postId: props.postId, content, commentObjectIsPost: true })
    draft.value = ''
    await load()
    emit('update:count', props.count + 1)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '评论失败')
  } finally {
    submitting.value = false
  }
}

async function remove(item: CommentVO) {
  try {
    await commentApi.remove({ id: item.id, userId: item.userId, postId: props.postId })
    items.value = items.value.filter((c) => c.id !== item.id)
    emit('update:count', Math.max(0, props.count - 1))
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '删除失败')
  }
}

function canDelete(item: CommentVO): boolean {
  // 后端只比对 body 的 userId（不校验令牌），UI 层只给自己评论显示删除按钮作为第一道防线
  return item.userId === auth.userId
}
</script>

<template>
  <div class="comment">
    <div class="comment__editor">
      <UserAvatar :src="auth.user?.avatar" :name="auth.user?.nickname ?? undefined" :size="32" />
      <textarea
        v-model="draft"
        class="comment__input"
        placeholder="写下你的评论…"
        rows="2"
        maxlength="500"
      ></textarea>
      <BaseButton size="sm" :loading="submitting" @click="submit">发表</BaseButton>
    </div>

    <BaseSkeleton v-if="loading" variant="text" :rows="2" />

    <template v-else-if="items.length">
      <ul class="comment__list">
        <li v-for="item in items" :key="item.id" class="comment-item">
          <UserAvatar :size="32" />
          <div class="comment-item__main">
            <div class="comment-item__head">
              <span class="comment-item__author">{{ displayName(null, item.userId) }}</span>
              <button
                v-if="canDelete(item)"
                class="comment-item__del"
                type="button"
                @click="remove(item)"
              >
                删除
              </button>
            </div>
            <p class="comment-item__content">{{ item.content }}</p>
            <p class="comment-item__time">{{ fmtRelative(item.createdTime) }}</p>
          </div>
        </li>
      </ul>
    </template>

    <BaseEmpty v-else text="还没有评论，来抢沙发" />
  </div>
</template>

<style scoped>
.comment {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.comment__editor {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
}

.comment__input {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-m);
  background: var(--c-bg-page);
  resize: vertical;
  line-height: 1.5;
}
.comment__input:focus {
  outline: none;
  border-color: var(--c-brand);
  background: var(--c-bg-card);
}

.comment__list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--c-border);
}

.comment-item__main {
  flex: 1;
  min-width: 0;
}

.comment-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-item__author {
  font-size: 13px;
  color: var(--c-text-2);
}

.comment-item__del {
  font-size: 12px;
  color: var(--c-text-3);
}
.comment-item__del:hover {
  color: var(--c-danger);
}

.comment-item__content {
  margin-top: 2px;
  font-size: 14px;
  word-break: break-word;
}

.comment-item__time {
  margin-top: 2px;
  font-size: 12px;
  color: var(--c-text-3);
}
</style>
