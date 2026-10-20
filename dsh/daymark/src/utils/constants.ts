/**
 * 业务常量镜像（供视图/组件使用）。
 * 真正的枚举定义在 api/types/enums.ts（契约层），这里仅 re-export。
 */
export { LoginRole, PostStatus } from '@/api/types/enums'
export type { LoginRoleValue, PostStatusValue } from '@/api/types/enums'

/** 帖子配图数量上限（前端校验，后端不校验） */
export const MAX_POST_IMAGES = 9
/** 图片大小上限（对齐后端 spring.servlet.multipart.max-file-size） */
export const MAX_IMAGE_BYTES = 10 * 1024 * 1024
/** 允许的图片扩展名（后端不校验，前端必须自检） */
export const ALLOWED_IMAGE_EXT = ['jpg', 'jpeg', 'png', 'webp', 'gif'] as const
