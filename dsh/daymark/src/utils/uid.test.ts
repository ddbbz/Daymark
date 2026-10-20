import { describe, expect, it } from 'vitest'

import { generateDefaultUid } from './uid'

describe('generateDefaultUid（后端默认 uid 为 null 时的本地兜底）', () => {
  it('生成纯数字串，长度 >= 10', () => {
    const uid = generateDefaultUid()
    expect(uid).toMatch(/^\d{10,}$/)
  })

  it('多次生成不重复（随机）', () => {
    const a = generateDefaultUid()
    const b = generateDefaultUid()
    expect(a).not.toBe(b)
  })
})
