/**
 * 后端常量镜像。
 * 来源：com.ddbz.daymark.common.constant.LoginRole / PostStatus
 * 后端改动时必须同步这里（契约测试会锁住它们）。
 */

/** LoginRole：0=普通用户，1=审查员 */
export const LoginRole = {
  user: 0,
  reviewer: 1,
} as const
export type LoginRoleValue = (typeof LoginRole)[keyof typeof LoginRole]

/** PostStatus：帖子状态 */
export const PostStatus = {
  failed: 0, // 驳回 / 发布失败
  success: 1, // 审核通过
  reviewing: 2, // 审核中
  privacy: 3, // 已删除
} as const
export type PostStatusValue = (typeof PostStatus)[keyof typeof PostStatus]
