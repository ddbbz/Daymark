import { z } from 'zod'

/** 用户名规则：3~20 位，字母数字下划线（对齐后端约定） */
const uidRule = z
  .string()
  .min(3, '用户名至少 3 位')
  .max(20, '用户名最多 20 位')
  .regex(/^[A-Za-z0-9_]+$/, '用户名仅限字母、数字、下划线')

const passwordRule = z.string().min(6, '密码至少 6 位').max(32, '密码最多 32 位')

export const loginSchema = z.object({
  uid: uidRule,
  password: passwordRule,
})

export const registerSchema = z.object({
  uid: uidRule,
  password: passwordRule,
  nickname: z.string().max(20, '昵称最多 20 字').optional(),
  bio: z.string().max(200, '简介最多 200 字').optional(),
})

export const reviewerRegisterSchema = z.object({
  uid: uidRule,
  password: passwordRule,
  inviteCode: z.string().min(1, '邀请码不能为空'),
})

export const passwordSchema = z.object({
  password: passwordRule,
  newPassword: passwordRule,
})

/** 从 zod safeParse 结果里取第一条错误文案（结构类型，避免绑定 zod 内部类型名） */
export function firstError(result: {
  success: boolean
  error?: { issues: Array<{ message: string }> }
}): string | null {
  if (result.success) return null
  return result.error?.issues[0]?.message ?? '输入有误'
}
