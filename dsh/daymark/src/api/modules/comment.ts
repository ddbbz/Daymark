/**
 * 评论模块 —— CommentController（映射路径 /comment）
 */
import { request } from '../http'
import type { CommentCreateBody, CommentDeleteBody, CommentVO } from '../types/interaction'

export const commentApi = {
  /**
   * PUT /comment  body{postId,content,commentObjectIsPost} → 提示串
   * 后端从令牌解析 userId；前端不要传 id（CommentDTO.id 是原始 int，传 0 可能被当作显式主键）。
   */
  create: ({ postId, content, commentObjectIsPost = true }: CommentCreateBody) =>
    request<string>('/comment', { method: 'PUT', body: { postId, content, commentObjectIsPost } }),

  /**
   * DELETE /comment  body{id,userId,postId} → 提示串
   * ⚠ 必须三件套：userId 用于后端比对（不校验令牌），postId 用于回退评论数（缺了报「系统异常」）。
   */
  remove: ({ id, userId, postId }: CommentDeleteBody) =>
    request<string>('/comment', { method: 'DELETE', body: { id, userId, postId } }),

  /** GET /comment?postId= → CommentVO[]（扁平列表，无分页、无嵌套回复） */
  list: (postId: number) =>
    request<CommentVO[]>('/comment', { method: 'GET', params: { postId } }),
}
