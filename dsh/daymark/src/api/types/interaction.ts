/**
 * 评论 / 点赞 / 收藏相关 DTO·VO。
 * 来源：CommentDTO / CommentVO / LikeVO / FavoriteVO
 *
 * ⚠ 这些 VO 里只有数字 `userId`，没有昵称/头像（后端缺 GET /user/{uid} 公开接口）。
 */
/** CommentVO —— 评论扁平结构，无 parentId（后端不支持嵌套回复） */
export interface CommentVO {
  id: number
  postId: number
  userId: number
  content: string
  createdTime: string | null
  commentObjectIsPost: boolean
}

/** PUT /comment 请求体 —— 后端从令牌取 userId，前端不要传 id */
export interface CommentCreateBody {
  postId: number
  content: string
  commentObjectIsPost: boolean
}

/**
 * DELETE /comment 请求体 —— ⚠ 必须同时带 id + userId + postId：
 *   - userId 用于后端比对"是否本人"（后端不校验令牌，只比对 body）
 *   - postId 用于回退帖子的评论数，缺了会报「系统异常」
 */
export interface CommentDeleteBody {
  id: number
  userId: number
  postId: number
}

/** LikeVO / FavoriteVO 结构相同 */
export interface LikeVO {
  id: number
  userId: number
  postId: number
  createdTime: string | null
}

export type FavoriteVO = LikeVO
