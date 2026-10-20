<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import BaseButton from '@/components/base/BaseButton.vue'
import BaseEmpty from '@/components/base/BaseEmpty.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSkeleton from '@/components/base/BaseSkeleton.vue'
import PostCard from '@/components/post/PostCard.vue'
import { usePaginationShim } from '@/composables/usePaginationShim'
import { usePostListStore, type SearchMode } from '@/stores/postList'
import { useToastStore } from '@/stores/toast'

const list = usePostListStore()
const toast = useToastStore()

const mode = ref<SearchMode>('title')
const keyword = ref('')

const modes: Array<{ key: SearchMode; label: string; placeholder: string }> = [
  { key: 'title', label: '标题', placeholder: '按标题搜索（留空浏览全部）' },
  { key: 'uid', label: 'UID', placeholder: '按 UID 搜索' },
  { key: 'nickname', label: '昵称', placeholder: '按昵称搜索' },
]

const currentPlaceholder = computed(
  () => modes.find((m) => m.key === mode.value)?.placeholder ?? '',
)

// 后端无分页，前端切片展示（后端补分页后替换实现，调用方不变）
const { visible, hasMore, loadMore, reset } = usePaginationShim(computed(() => list.posts))

onMounted(() => void search())

async function search() {
  try {
    await list.fetch(mode.value, keyword.value)
    reset()
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '加载失败')
  }
}

function switchMode(m: SearchMode) {
  mode.value = m
  void search()
}
</script>

<template>
  <main class="home">
    <div class="home__search">
      <div class="home__modes">
        <button
          v-for="m in modes"
          :key="m.key"
          class="mode"
          :class="{ 'mode--active': mode === m.key }"
          type="button"
          @click="switchMode(m.key)"
        >
          {{ m.label }}
        </button>
      </div>
      <div class="home__input-row">
        <BaseInput v-model="keyword" :placeholder="currentPlaceholder" @enter="search" />
        <BaseButton @click="search">搜索</BaseButton>
      </div>
    </div>

    <BaseSkeleton v-if="list.loading" variant="text" :rows="3" />

    <template v-else>
      <div v-if="visible.length" class="home__feed">
        <PostCard v-for="post in visible" :key="post.id" :post="post" />
      </div>
      <BaseEmpty v-else-if="list.searched" text="没有找到相关帖子" />

      <BaseButton v-if="hasMore" block variant="secondary" @click="loadMore()">加载更多</BaseButton>
    </template>
  </main>
</template>

<style scoped>
.home {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.home__search {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.home__modes {
  display: flex;
  gap: var(--space-1);
  padding: var(--space-1);
  background: var(--c-bg-page);
  border-radius: var(--radius-m);
}

.mode {
  flex: 1;
  padding: var(--space-2);
  border-radius: var(--radius-s);
  color: var(--c-text-2);
  font-size: 14px;
}

.mode--active {
  background: var(--c-bg-card);
  color: var(--c-text-1);
  font-weight: 500;
  box-shadow: var(--shadow-s);
}

.home__input-row {
  display: flex;
  gap: var(--space-2);
}
.home__input-row :deep(.field) {
  flex: 1;
}

.home__feed {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
</style>
