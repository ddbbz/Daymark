/**
 * 统一响应信封 —— 对应 com.ddbz.daymark.common.Result
 *
 * ⚠ 后端契约（源码注释原文）：`1 成功，0 和其它数字为失败`。
 * 这与任何"0 成功"的直觉都相反，前端唯一判据就是 `code === 1`。
 *
 * 用判别联合表达：
 *   - code === 1  → 成功，message 可能为 null（Result.success() 无参重载不设置 message）
 *   - code === 0  → 业务失败，message 一定有值，data 为 null
 */
export type ResultCode = 1 | 0

export type Result<T> =
  | { code: 1; message: string | null; data: T }
  | { code: 0; message: string; data: null }
