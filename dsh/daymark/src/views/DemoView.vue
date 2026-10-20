<script setup lang="ts">
import { ref } from 'vue'

import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseEmpty from '@/components/base/BaseEmpty.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseModal from '@/components/base/BaseModal.vue'
import BaseSkeleton from '@/components/base/BaseSkeleton.vue'
import BaseTabs from '@/components/base/BaseTabs.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import ImageLightbox from '@/components/common/ImageLightbox.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { useToastStore } from '@/stores/toast'

const toast = useToastStore()

const input = ref('')
const textarea = ref('')
const modalOpen = ref(false)
const confirmOpen = ref(false)
const activeTab = ref('a')
const lightboxOpen = ref(false)
const lightboxIndex = ref(0)

const tabs = [
  { key: 'a', label: '帖子' },
  { key: 'b', label: '评论' },
  { key: 'c', label: '收藏' },
]

const demoImages = [
  'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><rect width="200" height="200" fill="#ff2442"/><text x="100" y="120" font-size="60" text-anchor="middle" fill="#fff">1</text></svg>',
    ),
  'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><rect width="200" height="200" fill="#00b578"/><text x="100" y="120" font-size="60" text-anchor="middle" fill="#fff">2</text></svg>',
    ),
  'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><rect width="200" height="200" fill="#ff8f1f"/><text x="100" y="120" font-size="60" text-anchor="middle" fill="#fff">3</text></svg>',
    ),
]
</script>

<template>
  <main class="demo">
    <h1 class="demo__title">设计系统演示</h1>

    <section class="section">
      <h2 class="section__title">按钮 Button</h2>
      <div class="row">
        <BaseButton @click="toast.success('主要按钮')">主要</BaseButton>
        <BaseButton variant="secondary" @click="toast.info('次要按钮')">次要</BaseButton>
        <BaseButton variant="ghost" @click="toast.info('幽灵按钮')">幽灵</BaseButton>
        <BaseButton variant="danger" @click="toast.error('危险按钮')">危险</BaseButton>
        <BaseButton variant="text" @click="toast.info('文字按钮')">文字</BaseButton>
      </div>
      <div class="row">
        <BaseButton size="sm">小</BaseButton>
        <BaseButton size="md">中</BaseButton>
        <BaseButton size="lg">大</BaseButton>
        <BaseButton loading>加载中</BaseButton>
        <BaseButton disabled>禁用</BaseButton>
      </div>
    </section>

    <section class="section">
      <h2 class="section__title">输入 Input / Textarea</h2>
      <BaseInput v-model="input" label="用户名" placeholder="请输入" error="" />
      <BaseTextarea v-model="textarea" placeholder="正文（阶段 4 发布帖用）" :rows="3" />
    </section>

    <section class="section">
      <h2 class="section__title">徽章 Badge</h2>
      <div class="row">
        <BaseBadge>默认</BaseBadge>
        <BaseBadge tone="success">已发布</BaseBadge>
        <BaseBadge tone="warning">审核中</BaseBadge>
        <BaseBadge tone="danger">已驳回</BaseBadge>
      </div>
    </section>

    <section class="section">
      <h2 class="section__title">标签页 Tabs</h2>
      <BaseTabs v-model="activeTab" :items="tabs" />
      <p class="muted">当前：{{ activeTab }}</p>
    </section>

    <section class="section">
      <h2 class="section__title">头像 UserAvatar</h2>
      <div class="row">
        <UserAvatar name="小红" :size="40" />
        <UserAvatar name="阿蓝" :size="56" />
        <UserAvatar :size="40" />
      </div>
    </section>

    <section class="section">
      <h2 class="section__title">弹窗 Modal / 确认 ConfirmDialog</h2>
      <div class="row">
        <BaseButton @click="modalOpen = true">打开弹窗</BaseButton>
        <BaseButton variant="danger" @click="confirmOpen = true">删除确认</BaseButton>
      </div>
    </section>

    <section class="section">
      <h2 class="section__title">图片灯箱 ImageLightbox</h2>
      <div class="row row--wrap">
        <button
          v-for="(img, i) in demoImages"
          :key="i"
          class="thumb"
          type="button"
          @click="lightboxIndex = i; lightboxOpen = true"
        >
          <img :src="img" alt="" />
        </button>
      </div>
    </section>

    <section class="section">
      <h2 class="section__title">骨架屏 Skeleton</h2>
      <BaseSkeleton variant="text" :rows="2" />
      <div class="row">
        <BaseSkeleton variant="avatar" />
        <BaseSkeleton variant="card" width="200px" />
        <BaseSkeleton variant="image" width="120px" />
      </div>
    </section>

    <section class="section">
      <h2 class="section__title">空态 Empty</h2>
      <BaseEmpty text="还没有内容，去发布第一条吧" />
    </section>

    <BaseModal :open="modalOpen" title="弹窗标题" @close="modalOpen = false">
      <p class="muted">这是 BaseModal 的默认插槽内容，支持标题 + 关闭按钮。</p>
      <template #footer>
        <BaseButton block @click="modalOpen = false">知道了</BaseButton>
      </template>
    </BaseModal>

    <ConfirmDialog
      :open="confirmOpen"
      title="删除帖子"
      message="删除后不可恢复，确定删除吗？"
      confirm-text="删除"
      danger
      @confirm="confirmOpen = false; toast.success('已删除（演示）')"
      @cancel="confirmOpen = false"
    />

    <ImageLightbox v-model:index="lightboxIndex" :open="lightboxOpen" :images="demoImages" @close="lightboxOpen = false" />
  </main>
</template>

<style scoped>
.demo {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-4) var(--space-8);
}

.demo__title {
  font-size: 22px;
  margin-bottom: var(--space-6);
}

.section {
  padding: var(--space-4) 0;
  border-bottom: 1px solid var(--c-border);
}

.section__title {
  font-size: 14px;
  color: var(--c-text-2);
  margin-bottom: var(--space-3);
}

.row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
  margin-bottom: var(--space-3);
}
.row--wrap {
  flex-wrap: wrap;
}

.muted {
  color: var(--c-text-3);
  font-size: 13px;
}

.thumb {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-m);
  overflow: hidden;
  border: 1px solid var(--c-border);
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
