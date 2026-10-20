/**
 * 帖子相关 DTO·VO。
 * 来源：PostDTO / PostVO / PostStatusDTO
 *
 * ⚠ 字段名单复数陷阱（后端实测）：
 *   - 请求体字段是 `image`（PostDTO.image，单数）
 *   - 响应体字段是 `images`（PostVO.images，复数）
 *   发错字段名会被 Jackson 静默忽略 → 图片直接丢失且不报错。
 *   因此请求/响应拆成两个 interface，从编译期杜绝混用。
 */
import type { PostStatusValue } from './enums'

/** 帖子（响应）—— PostVO */
export interface PostVO {
  id: number
  userId: number
  title: string | null
  content: string | null
  /** ⚠ 响应字段，复数 */
  images: string[] | null
  likeCount: number
  commentCount: number
  favoriteCount: number
  status: PostStatusValue
  /** LocalDateTime，无时区后缀（如 `2026-09-15T10:00:00`） */
  createdTime: string | null
  updatedTime: string | null
}

/** POST /post/publish 请求体 —— 后端只读 title/content/image；userId/id 由令牌决定，会被忽略 */
export interface PostPublishBody {
  title: string
  content: string
  /** ⚠ 请求字段，单数 */
  image: string[]
}

/** POST /post/updatePost 请求体 —— ⚠ userId 必传（原始 int，省略即反序列化为 0 导致报错） */
export interface PostUpdateBody {
  id: number
  userId: number
  title: string
  content: string
  /** ⚠ 请求字段，单数 */
  image: string[]
}

/** POST /post/updateStatus、POST /review 请求体 —— 只读 id 与 status */
export interface PostStatusBody {
  id: number
  status: PostStatusValue
}
