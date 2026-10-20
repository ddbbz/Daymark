/**
 * 图片地址归一化。
 *
 * 后端 `app.base-url = http://localhost:21001/file/`，上传/默认头像都是**绝对 URL**（指向 nginx）。
 * 开发期 nginx 未启动时这些绝对 URL 会裂图，这里提供兜底（仅开发态）。
 */
const DEV_PORT_REWRITE = ':21001'

/** 图片加载失败时的兜底占位图（内联 SVG data URI） */
export const PLACEHOLDER_IMG =
  'data:image/svg+xml,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect width="100" height="100" fill="#f0f0f2"/><circle cx="50" cy="38" r="15" fill="#cfcfd4"/><path d="M20 84c0-16 13-27 30-27s30 11 30 27" fill="#cfcfd4"/></svg>',
  )

export const PLACEHOLDER_AVATAR = PLACEHOLDER_IMG

/**
 * 归一化图片地址：
 *   - 绝对 URL（http/https）→ 原样返回（dev 兜底见 resolveAssetUrlFallback）
 *   - 相对路径 → 拼当前 origin
 *   - data:/blob: → 原样
 *   - 空/null → 占位图
 */
export function resolveAssetUrl(url: string | null | undefined): string {
  if (!url) return PLACEHOLDER_IMG
  if (/^(https?:)?\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) {
    return url
  }
  return window.location.origin + (url.startsWith('/') ? url : `/${url}`)
}

/**
 * 开发期兜底：后端把绝对 URL 写死成 21001（nginx 端口），
 * 当 nginx 未启动时，把 `:21001` 重写为 `:8080`（后端 WebConfig 同样映射了 /file/、/images/）。
 * 仅用于图片 onerror 时兜底，不改变主逻辑。
 */
export function devRewritePort(url: string | null | undefined): string {
  if (!url) return PLACEHOLDER_IMG
  if (url.includes(DEV_PORT_REWRITE) && import.meta.env.DEV) {
    return url.replace(DEV_PORT_REWRITE, ':8080')
  }
  return url
}
