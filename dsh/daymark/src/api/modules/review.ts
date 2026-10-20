/**
 * 审核模块 —— ReviewController（映射路径 /review）
 *
 * ⚠ GET /review 返回的 PostVO **不填**点赞/评论/收藏计数（恒为 0），审核台不要显示这三个数字。
 */
import { request } from '../http'
import type { PostStatusBody, PostVO } from '../types/post'
import type { PostStatusValue } from '../types/enums'

export const reviewApi = {
  /** GET /review → 待审 PostVO[]（status=2） */
  pending: () => request<PostVO[]>('/review', { method: 'GET' }),

  /** POST /review  body{id,status} → null（需 role=1，后端校验权限） */
  decide: (id: number, status: PostStatusValue) =>
    request<null>('/review', { method: 'POST', body: { id, status } satisfies PostStatusBody }),
}
