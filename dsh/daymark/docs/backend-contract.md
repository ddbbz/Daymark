# 后端契约（前端唯一依据）

> 本文全部来自后端 `daymark/src/main/java` 源码实测，**不参考**根目录《Daymark-接口文档.md》。

## 全局约定

| 项 | 值 |
| --- | --- |
| 成功判定 | `code === 1`（`0` 失败）；`message` 成功时可能为 `null` |
| 业务失败 HTTP 状态 | **200**（不靠状态码判断） |
| 鉴权头 | `token: <jwt>`（无 `Bearer` 前缀） |
| 401 | **无响应体**（必须先判状态码再解析 JSON） |
| 未捕获异常 | 兜底为 `"系统异常，请稍后重试"` |
| 角色 | `0` 普通用户 / `1` 审查员（`sub` = uid） |
| 分页 | **无**（列表全量返回数组） |
| 时间 | `LocalDateTime`，**无时区后缀**（前端自写解析器） |
| 后端端口 | `8080`；图片由 nginx `21001` 的 `/file/` 直发 |

## 接口清单（36 个端点）

### 认证 / 用户
| 方法 | 路径 | 鉴权 | 请求 | 返回 data |
| --- | --- | --- | --- | --- |
| POST | `/login` | 否 | `{uid,password}` | `LoginVO{jwt,role:0}` |
| GET | `/register` | 否 | — | `{uid,avatarUrl}`（uid 可能 null） |
| POST | `/register` | 否 | `{uid,password,nickname,bio,avatar}` | 提示串 |
| POST | `/register/avatar` | 否 | multipart **`avatar`** | URL 串 |
| POST | `/reviewerLogin` | 否 | `{uid,password}` | `LoginVO{jwt,role:1}` |
| POST | `/reviewerRegister` | 否 | `{uid,password,inviteCode}` | null |
| GET | `/user/me` | 是 | — | `UserVO{id,uid,nickname,bio,avatar}` |
| PUT | `/user/me/update/information` | 是 | `{nickname,bio,avatar}` | 提示串 |
| PUT | `/user/me/update/password` | 是 | `{password,newPassword}` | 提示串 |
| POST | `/user/me/avatar` | 是 | multipart **`avatar`** | URL 串 |

### 帖子
| 方法 | 路径 | 鉴权 | 请求 | 返回 data |
| --- | --- | --- | --- | --- |
| POST | `/post/publish` | 是 | `{title,content,`**`image`**`[]}` | 提示串 |
| GET | `/post/showByUid?uid=` | 否 | query | `PostVO[]` |
| GET | `/post/showByNickname?nickname=` | 否 | query | `PostVO[]` |
| GET | `/post/showByTitle?title=` | 否 | query | `PostVO[]`（唯一 status=1 过滤） |
| POST | `/post/updateStatus` | 是 | `{id,status}` | null |
| POST | `/post/updatePost` | 是 | `{id,`**`userId`**`,title,content,`**`image`**`[]}` | null |
| POST | `/post/avatar` | 是 | multipart **`avatars`** | `URL[]` |

### 评论 / 点赞 / 收藏 / 审核
| 方法 | 路径 | 鉴权 | 请求 | 返回 data |
| --- | --- | --- | --- | --- |
| PUT | `/comment` | 是 | `{postId,content,commentObjectIsPost}` | 提示串 |
| DELETE | `/comment` | 是 | `{id,userId,postId}` | 提示串 |
| GET | `/comment?postId=` | 否 | query | `CommentVO[]`（扁平） |
| PUT / DELETE | `/like?postId=` | 是 | query | null |
| GET | `/like/isLike?postId=` | 否 | query | boolean |
| GET | `/like/likeInformation?postId=` | 否 | query | `LikeVO[]` |
| PUT / DELETE | `/favorite?postId=` | 是 | query | null |
| GET | `/favorite/isFavorite?postId=` | 否 | query | boolean |
| GET | `/favorite/favoriteInformation?postId=` | 否 | query | `FavoriteVO[]` |
| GET | `/review` | 是 | — | 待审 `PostVO[]`（计数恒 0） |
| POST | `/review` | 是 | `{id,status}` | null |

`PostStatus`：`0` 驳回 / `1` 通过 / `2` 审核中 / `3` 已删除。

## 前端必须绕开的陷阱（已实现/已测试）

1. **字段名**：请求 `image`（单数）→ 响应 `images`（复数）；上传头像 `avatar`、配图 `avatars`。
2. **删评论三件套**：`id + userId + postId`（后端不校验令牌，只比对 body 的 userId）。
3. **编辑帖子**：必带 `userId`（等于当前用户数字 id）；成功后状态回审核中、计数清零。
4. **点赞/收藏不幂等**：重复操作抛业务异常 → 前端乐观更新 + pending 锁 + 回滚。
5. **`user:{uid}` 缓存混入 password** → 前端 zod 白名单剥离。
6. **缺失接口**：无 `GET /post/{id}`、无 feed 分页、无 `GET /user/{uid}`、无「我点赞/收藏」列表。
