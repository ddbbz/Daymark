/**
 * zod 运行时校验。
 *
 * 为什么需要：TS 只防编译期错误，防不了后端**运行时**返回意外结构。
 * 本项目后端有多处"静默不一致"，zod 在这里承担两件事：
 *   1. 关键响应校验
 *   2. **UserVO 白名单剥离**——后端 `user:{uid}` 缓存键会混入 UserEntity（含 password 哈希），
 *      `.strip()` 只保留声明的字段，是防密码泄漏的最后一道锁。
 */
import { z } from 'zod'

/** UserVO 白名单剥离。任何多余字段（尤其 password）会被丢弃。 */
export const userVoSchema = z
  .object({
    id: z.number(),
    uid: z.string(),
    nickname: z.string().nullable(),
    bio: z.string().nullable(),
    avatar: z.string().nullable(),
  })
  .strip()

/** 帖子列表项：只校验前端真正依赖的字段，其余保持宽松（后端结构不稳定） */
export const postVoSchema = z
  .object({
    id: z.number(),
    userId: z.number(),
    title: z.string().nullable(),
    content: z.string().nullable(),
    images: z.array(z.string()).nullable(),
    likeCount: z.number().int(),
    commentCount: z.number().int(),
    favoriteCount: z.number().int(),
    status: z.union([z.literal(0), z.literal(1), z.literal(2), z.literal(3)]),
    createdTime: z.string().nullable(),
    updatedTime: z.string().nullable(),
  })
  .strip()

export const commentVoSchema = z
  .object({
    id: z.number(),
    postId: z.number(),
    userId: z.number(),
    content: z.string(),
    createdTime: z.string().nullable(),
    commentObjectIsPost: z.boolean(),
  })
  .strip()

export const loginVoSchema = z
  .object({
    jwt: z.string(),
    role: z.union([z.literal(0), z.literal(1)]),
  })
  .strip()

/** 默认注册信息：uid 允许 null（后端兜底不充分），avatarUrl 允许 null */
export const registerDefaultVoSchema = z
  .object({
    uid: z.string().nullable(),
    avatarUrl: z.string().nullable(),
  })
  .strip()

export type UserVoSchema = z.infer<typeof userVoSchema>
