/**
 * 格式化工具。
 * ⚠ 后端时间字段是 LocalDateTime，无时区后缀（如 `2026-09-15T10:00:00`），
 * 不能用 `new Date(str)`（各引擎按本地/UTC 解析不一致），必须显式按本地时间解析。
 */
import { PostStatus, type PostStatusValue } from '@/api/types/enums'

/** 把后端无时区的 LocalDateTime 字符串按**本地时间**解析为 Date */
export function parseLocalDateTime(value: string | null | undefined): Date | null {
  if (!value) return null
  // 统一把空格分隔也视为 T 分隔，避免 `2026-09-15 10:00:00` 形式
  const normalized = value.trim().replace(' ', 'T')
  const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/.exec(normalized)
  if (!m) return null
  const [, y, mo, d, h, mi, s] = m
  return new Date(Number(y), Number(mo) - 1, Number(d), Number(h), Number(mi), Number(s ?? '0'))
}

/** 相对时间（刚刚 / n 分钟前 / n 小时前 / n 天前 / 具体日期） */
export function fmtRelative(value: string | null | undefined): string {
  const date = parseLocalDateTime(value)
  if (!date) return ''
  const diff = Date.now() - date.getTime()
  const minute = 60_000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < 30 * day) return `${Math.floor(diff / day)} 天前`
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
    date.getDate(),
  ).padStart(2, '0')}`
}

/** 数字缩写（128 → 128；1234 → 1.2k；12000 → 1.2万） */
export function fmtCount(n: number): string {
  if (n < 1000) return String(n)
  if (n < 10_000) return `${(n / 1000).toFixed(1)}k`
  return `${(n / 10_000).toFixed(1)}万`
}

/** 帖子状态元信息（用于徽章展示） */
export const postStatusMeta: Record<
  PostStatusValue,
  { label: string; tone: 'default' | 'success' | 'warning' | 'danger' }
> = {
  [PostStatus.failed]: { label: '已驳回', tone: 'danger' },
  [PostStatus.success]: { label: '已发布', tone: 'success' },
  [PostStatus.reviewing]: { label: '审核中', tone: 'warning' },
  [PostStatus.privacy]: { label: '已删除', tone: 'default' },
}

/** 作者展示名：后端 PostVO/CommentVO 只有数字 userId，无昵称接口时降级显示「用户 <id>」 */
export function displayName(nickname: string | null | undefined, userId: number): string {
  if (nickname) return nickname
  return `用户 ${userId}`
}
