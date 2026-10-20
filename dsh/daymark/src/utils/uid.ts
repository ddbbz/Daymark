/**
 * 默认 uid 兜底生成。
 * 后端 GET /register 返回的 uid 可能为 null（RegisterServiceImpl.createUsername 撞库兜底不充分），
 * 前端拿到 null 时用它生成本地默认值（用户仍可修改）。
 * 形式对齐后端：纯数字串，保证长度 ≥ 10。
 */
export function generateDefaultUid(): string {
  const digits = () => Math.floor(Math.random() * 1_000_000_000).toString()
  return (digits() + digits()).padStart(10, '0')
}
