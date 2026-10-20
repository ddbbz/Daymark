/**
 * 收藏模块 —— FavoriteController（映射路径 /favorite）
 * ⚠ 与点赞一致：不幂等，重复收藏抛「该帖子已收藏」。
 */
import { request } from '../http'
import type { FavoriteVO } from '../types/interaction'

export const favoriteApi = {
  /** PUT /favorite?postId= → null */
  favorite: (postId: number) =>
    request<null>('/favorite', { method: 'PUT', params: { postId } }),

  /** DELETE /favorite?postId= → null */
  unfavorite: (postId: number) =>
    request<null>('/favorite', { method: 'DELETE', params: { postId } }),

  /** GET /favorite/isFavorite?postId= → boolean */
  isFavorite: (postId: number) =>
    request<boolean>('/favorite/isFavorite', { method: 'GET', params: { postId } }),

  /** GET /favorite/favoriteInformation?postId= → FavoriteVO[] */
  list: (postId: number) =>
    request<FavoriteVO[]>('/favorite/favoriteInformation', { method: 'GET', params: { postId } }),
}
