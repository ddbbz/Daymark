/**
 * 点赞 / 收藏交互状态。
 *
 * 后端行为（实测）：
 *   - PostVO 只有 likeCount / favoriteCount，**没有 likeStatus/favoriteStatus**，
 *     状态需逐帖调 isLike / isFavorite。
 *   - 点赞 / 收藏**不幂等**：重复点赞抛「这名用户已经点过赞了」，
 *     取消未点赞抛「用户未点赞」。
 *
 * 因此这里用「乐观更新 + pending 锁 + 失败回滚」：
 *   - pending 锁杜绝连点；
 *   - 计数用 delta（本地快照 + delta = 展示值），成功保留、失败回滚。
 */
import { defineStore } from 'pinia'

import { favoriteApi } from '@/api/modules/favorite'
import { likeApi } from '@/api/modules/like'

export const useInteractionStore = defineStore('interaction', {
  state: () => ({
    /** postId -> 是否点赞（null/undefined = 未知） */
    like: {} as Record<number, boolean | null>,
    /** postId -> 是否收藏 */
    favorite: {} as Record<number, boolean | null>,
    /** postId -> 点赞计数增量（+1/-1/0） */
    likeDelta: {} as Record<number, number>,
    favoriteDelta: {} as Record<number, number>,
    likePending: {} as Record<number, boolean>,
    favoritePending: {} as Record<number, boolean>,
  }),

  getters: {
    liked: (state) => (postId: number) => state.like[postId] ?? false,
    favorited: (state) => (postId: number) => state.favorite[postId] ?? false,
    likeDelta: (state) => (postId: number) => state.likeDelta[postId] ?? 0,
    favoriteDelta: (state) => (postId: number) => state.favoriteDelta[postId] ?? 0,
    likePending: (state) => (postId: number) => state.likePending[postId] ?? false,
    favoritePending: (state) => (postId: number) => state.favoritePending[postId] ?? false,
  },

  actions: {
    /** 确保点赞状态已知（详情页等 interactive 场景挂载时调用） */
    async ensureLike(postId: number): Promise<boolean> {
      if (this.like[postId] == null) {
        this.like[postId] = await likeApi.isLike(postId)
      }
      return this.like[postId] === true
    },

    async ensureFavorite(postId: number): Promise<boolean> {
      if (this.favorite[postId] == null) {
        this.favorite[postId] = await favoriteApi.isFavorite(postId)
      }
      return this.favorite[postId] === true
    },

    /** 乐观切换点赞；失败回滚并 rethrow（调用方 toast） */
    async toggleLike(postId: number): Promise<boolean> {
      if (this.likePending[postId]) return this.like[postId] === true
      if (this.like[postId] == null) {
        try {
          this.like[postId] = await likeApi.isLike(postId)
        } catch {
          this.like[postId] = false
        }
      }
      const prev = this.like[postId] === true
      const next = !prev
      this.likePending[postId] = true
      this.like[postId] = next
      this.likeDelta[postId] = (this.likeDelta[postId] ?? 0) + (next ? 1 : -1)
      try {
        if (next) await likeApi.like(postId)
        else await likeApi.unlike(postId)
        return next
      } catch (e) {
        this.like[postId] = prev
        this.likeDelta[postId] = (this.likeDelta[postId] ?? 0) + (next ? -1 : 1)
        throw e
      } finally {
        this.likePending[postId] = false
      }
    },

    async toggleFavorite(postId: number): Promise<boolean> {
      if (this.favoritePending[postId]) return this.favorite[postId] === true
      if (this.favorite[postId] == null) {
        try {
          this.favorite[postId] = await favoriteApi.isFavorite(postId)
        } catch {
          this.favorite[postId] = false
        }
      }
      const prev = this.favorite[postId] === true
      const next = !prev
      this.favoritePending[postId] = true
      this.favorite[postId] = next
      this.favoriteDelta[postId] = (this.favoriteDelta[postId] ?? 0) + (next ? 1 : -1)
      try {
        if (next) await favoriteApi.favorite(postId)
        else await favoriteApi.unfavorite(postId)
        return next
      } catch (e) {
        this.favorite[postId] = prev
        this.favoriteDelta[postId] = (this.favoriteDelta[postId] ?? 0) + (next ? -1 : 1)
        throw e
      } finally {
        this.favoritePending[postId] = false
      }
    },
  },
})
