/**
 * 点赞模块 —— LikeController（映射路径 /like）
 *
 * ⚠ 点赞/取消点赞**不幂等**：重复点赞抛「这名用户已经点过赞了」，
 * 未点赞就取消抛「用户未点赞，取消点赞失败」。UI 层需乐观更新 + pending 锁 + 失败回滚。
 */
import { request } from '../http'
import type { LikeVO } from '../types/interaction'

export const likeApi = {
  /** PUT /like?postId= → null */
  like: (postId: number) =>
    request<null>('/like', { method: 'PUT', params: { postId } }),

  /** DELETE /like?postId= → null */
  unlike: (postId: number) =>
    request<null>('/like', { method: 'DELETE', params: { postId } }),

  /** GET /like/isLike?postId= → boolean */
  isLike: (postId: number) =>
    request<boolean>('/like/isLike', { method: 'GET', params: { postId } }),

  /** GET /like/likeInformation?postId= → LikeVO[]（点赞人列表，只有 userId） */
  list: (postId: number) =>
    request<LikeVO[]>('/like/likeInformation', { method: 'GET', params: { postId } }),
}
