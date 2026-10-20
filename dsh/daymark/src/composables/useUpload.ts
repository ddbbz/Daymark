/**
 * 图片上传校验。
 * 后端上传接口**零校验**（不查类型/大小/扩展名），前端必须自检。
 */
import { ALLOWED_IMAGE_EXT, MAX_IMAGE_BYTES, MAX_POST_IMAGES } from '@/utils/constants'

export function useImageUpload() {
  function validateFile(file: File): string | null {
    const dot = file.name.lastIndexOf('.')
    const ext = dot >= 0 ? file.name.slice(dot + 1).toLowerCase() : ''
    if (!ALLOWED_IMAGE_EXT.includes(ext as (typeof ALLOWED_IMAGE_EXT)[number])) {
      return '仅支持 jpg/png/webp/gif 格式'
    }
    if (file.size > MAX_IMAGE_BYTES) {
      return '单张图片不能超过 10MB'
    }
    return null
  }

  function validateList(files: File[]): string | null {
    if (files.length > MAX_POST_IMAGES) {
      return `最多上传 ${MAX_POST_IMAGES} 张图片`
    }
    for (const file of files) {
      const err = validateFile(file)
      if (err) return err
    }
    return null
  }

  return { validateFile, validateList }
}
