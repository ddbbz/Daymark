/**
 * 用户 / 认证相关 DTO·VO。
 * 来源：LoginDTO / LoginVO / UserDTO / UserPasswordDTO / RegisterDTO / UserVO / RegisterDefaultVO
 */
import type { LoginRoleValue } from './enums'

/** POST /login、POST /reviewerLogin 请求体（LoginDTO / ReviewerDTO） */
export interface LoginBody {
  uid: string
  password: string
}

/** 登录响应 —— LoginVO。⚠ 只有 { jwt, role }，没有 uid/昵称/头像 */
export interface LoginVO {
  jwt: string
  role: LoginRoleValue
}

/**
 * 用户资料 —— UserVO。
 * ⚠ 后端 `user:{uid}` 缓存键同时被 UserEntity（含 password）与 UserVO 写入，
 * 响应里可能混入密码哈希。类型上禁止索引签名，运行时用 zod 白名单剥离。
 */
export interface UserVO {
  id: number
  uid: string
  nickname: string | null
  bio: string | null
  avatar: string | null
}

/** GET /register 响应 —— RegisterDefaultVO。⚠ uid 可能为 null（后端撞库兜底不充分） */
export interface RegisterDefaultVO {
  uid: string | null
  avatarUrl: string | null
}

/** POST /register 请求体 —— RegisterDTO（avatar 是上传后的 URL 字符串，不是文件） */
export interface RegisterBody {
  uid: string
  password: string
  nickname?: string | null
  bio?: string | null
  avatar?: string | null
}

/** PUT /user/me/update/information 请求体 —— UserDTO（仅传入才修改） */
export interface UpdateProfileBody {
  nickname?: string | null
  bio?: string | null
  avatar?: string | null
}

/** PUT /user/me/update/password 请求体 —— UserPasswordDTO。⚠ 字段名是 password，不是 oldPassword */
export interface UpdatePasswordBody {
  password: string
  newPassword: string
}

/** POST /reviewerRegister 请求体 —— ReviewerDTO */
export interface ReviewerRegisterBody {
  uid: string
  password: string
  inviteCode: string
}
