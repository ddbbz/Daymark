/**
 * 带命名空间与降级的 localStorage 封装。
 * 测试环境（node）没有 localStorage 时降级为内存 Map，保证契约测试可运行。
 */
const mem = new Map<string, string>()

function backend(): Storage | null {
  try {
    return typeof localStorage !== 'undefined' ? localStorage : null
  } catch {
    return null
  }
}

export function storageGet(key: string): string | null {
  const b = backend()
  if (b) return b.getItem(key)
  return mem.get(key) ?? null
}

export function storageSet(key: string, value: string): void {
  const b = backend()
  if (b) b.setItem(key, value)
  else mem.set(key, value)
}

export function storageRemove(key: string): void {
  const b = backend()
  if (b) b.removeItem(key)
  else mem.delete(key)
}

export function storageClear(): void {
  const b = backend()
  if (b) b.clear()
  else mem.clear()
}
