/**
 * 帖子列表状态（发现页）。
 * 后端无「获取全部帖子」接口，唯一按 status=1 过滤的列表是 showByTitle（空关键字=浏览全部）。
 */
import { defineStore } from 'pinia'

import { postApi } from '@/api/modules/post'
import type { PostVO } from '@/api/types/post'

export type SearchMode = 'title' | 'uid' | 'nickname'

export const usePostListStore = defineStore('postList', {
  state: () => ({
    posts: [] as PostVO[],
    mode: 'title' as SearchMode,
    keyword: '',
    loading: false,
    /** 是否已执行过一次查询（区分「还没搜」与「搜了没结果」） */
    searched: false,
  }),

  actions: {
    async fetch(mode: SearchMode, keyword: string) {
      this.mode = mode
      this.keyword = keyword ?? ''
      this.loading = true
      try {
        let list: PostVO[]
        if (mode === 'uid') list = await postApi.byUid(this.keyword)
        else if (mode === 'nickname') list = await postApi.byNickname(this.keyword)
        else list = await postApi.byTitle(this.keyword)
        this.posts = Array.isArray(list) ? list : []
        this.searched = true
        return this.posts
      } finally {
        this.loading = false
      }
    },

    reset() {
      this.posts = []
      this.keyword = ''
      this.mode = 'title'
      this.searched = false
    },
  },
})
