/**
 * 帖子模块 —— PostController（映射路径 /post）
 *
 * ⚠ 字段名单复数不一致（后端实测）：
 *   请求体用单数 `image`（PostDTO.image），响应体用复数 `images`（PostVO.images）。
 *   写错字段会被 Jackson 静默忽略 → 图片丢失且不报错。映射只在本层发生。
 */
import { request } from '../http'
import type { PostPublishBody, PostStatusBody, PostUpdateBody, PostVO } from '../types/post'
import type { PostStatusValue } from '../types/enums'

export const postApi = {
  /**
   * POST /post/publish  body{title,content,image[]} → 提示串
   * 作者由令牌决定；body 里的 userId/id 会被后端忽略；status 强制为审核中(2)。
   */
  publish: ({ title, content, image }: PostPublishBody) =>
    request<string>('/post/publish', { method: 'POST', body: { title, content, image } }),

  /** GET /post/showByUid?uid= → PostVO[]（缓存路径可能包含审核中内容，见 README） */
  byUid: (uid: string) => request<PostVO[]>('/post/showByUid', { method: 'GET', params: { uid } }),

  /** GET /post/showByNickname?nickname= → PostVO[]（不过滤状态） */
  byNickname: (nickname: string) =>
    request<PostVO[]>('/post/showByNickname', { method: 'GET', params: { nickname } }),

  /** GET /post/showByTitle?title= → PostVO[]（唯一按 status=1 过滤的列表接口；空串=浏览全部） */
  byTitle: (title: string) =>
    request<PostVO[]>('/post/showByTitle', { method: 'GET', params: { title } }),

  /** POST /post/updateStatus  body{id,status} → null（归属校验依据令牌） */
  updateStatus: (id: number, status: PostStatusValue) =>
    request<null>('/post/updateStatus', {
      method: 'POST',
      body: { id, status } satisfies PostStatusBody,
    }),

  /**
   * POST /post/updatePost  body{id,userId,title,content,image[]} → null
   * ⚠ userId 必传且必须等于当前令牌用户的数据库 id。
   * ⚠ 编辑会把状态重置为审核中，并清零点赞/评论/收藏计数。
   */
  update: ({ id, userId, title, content, image }: PostUpdateBody) =>
    request<null>('/post/updatePost', {
      method: 'POST',
      body: { id, userId, title, content, image },
    }),

  /** POST /post/avatar  multipart `avatars`（复数，可多张）→ URL[] */
  uploadImages: (files: File[]) => {
    const form = new FormData()
    for (const file of files) form.append('avatars', file)
    return request<string[]>('/post/avatar', { method: 'POST', form })
  },
}
