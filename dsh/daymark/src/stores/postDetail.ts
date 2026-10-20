/**
 * 帖子详情缓存。
 * 后端**没有** GET /post/{id}，详情只能靠列表跳转时缓存到内存；
 * 直接刷新 / 分享链接时缓存丢失 → 详情页显示降级态（不造假数据）。
 */
import { defineStore } from 'pinia'

import type { PostVO } from '@/api/types/post'

export const usePostDetailStore = defineStore('postDetail', {
  state: () => ({
    cache: {} as Record<number, PostVO>,
  }),

  getters: {
    byId: (state) => (id: number): PostVO | null => state.cache[id] ?? null,
  },

  actions: {
    cachePost(post: PostVO) {
      this.cache[post.id] = post
    },
  },
})
