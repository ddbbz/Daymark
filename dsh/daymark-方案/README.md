# Daymark 前端项目方案（正式版）

| 项 | 内容 |
| --- | --- |
| 项目名称 | **daymark**（前端） |
| 目标路径 | `D:\zxc\project\Daymark\dsh\daymark` |
| 文档版本 | **v1.0 正式版**（取代 `Daymark-前端编写计划-dsh目录.md` 的计划稿地位） |
| 编写日期 | 2026-02-11 |
| 状态 | 方案已定，**尚未创建任何项目文件** |
| 契约唯一来源 | `daymark\src\main\java`（后端源码，逐文件实测） |
| 明确忽略 | 根目录 `Daymark-接口文档.md`、`frontend\`（既有前端） |

---

## 0. 复查结论（本次新增）

### 0.1 两个 P0 的复查结果

我重新读取了两个 P0 的源码，并检查了文件修改时间与全仓库引用。

| P0 | 上次结论 | **复查结论** | 证据 |
| --- | --- | --- | --- |
| **P0-1** 上传图片 URL 端口 | 「端口写死 21001，该端口无服务，所有上传图片不可见」 | ⚠ **结论需修正：代码已改，且原判断是错的——21001 有服务** | 见 0.2 |
| **P0-2** 审查员注册逻辑写反 | 「`if(!judgeUidIsRepeat)` 导致新 uid 被判重复，接口不可用」 | ✅ **已修复，且修复正确** | 见 0.3 |

### 0.2 P0-1 复查详情（重要修正）

**代码已修改**（`ModifyUserAvatarPathFormatUtil.java`，修改时间 `2026/10/10 16:47`）：

```java
// 现在（第 49 行）：已改用注入的配置项
return baseUrl + newFileName;

// 上次看到的（旧版）：
return "http://localhost:21001/file/" + newFileName;
```

配套地，`application.yml` 也被改了（同一时间戳），`app.base-url` 从 `http://localhost:8080` 改为：

```yaml
app:
  base-url: http://localhost:21001/file/     # 第 47 行，注意结尾带 /file/
```

**同时 `RegisterServiceImpl` 的默认头像也跟着改了**（第 74 行）：

```java
registerDefaultVO.setAvatarUrl(baseUrl + "3cb8738059d1f022db0825072074cd09.jpg");
//                                ↑ 去掉了 "/images/"，因为 baseUrl 已含 "/file/"
```

**关键发现：21001 并不是孤儿端口。** 仓库里有一套 nginx：

```
frontend\nginx-1.30.5\nginx.exe
frontend\nginx-1.30.5\conf\nginx.conf   ← listen 21001
```

```nginx
server {
    listen       21001;
    location /      { root html; index index.html; }              # 托管前端产物
    location /api/  { proxy_pass http://localhost:8080/; }         # 反代后端
    location /file/ { alias D:/zxc/project/Daymark/upload/; }      # 直发图片
}
```

`frontend\nginx-1.30.5\html\` 里有构建产物（`index.html` + `assets/`），`logs\nginx.pid` 存在（2026/9/29 启动过），`access.log` 有访问记录。**实测当前 nginx 未在运行**（端口 21000/21001/8080 均无监听）。

**所以真实架构是这样**（nginx 是统一入口，静态图片绕过后端由 nginx 直发）：

```
browser ──► nginx :21001 ──┬── /            → html/   （SPA 产物）
                           ├── /api/        → localhost:8080  （Spring Boot）
                           └── /file/xxx    → D:/zxc/project/Daymark/upload/  （图片直发）
```

**因此 P0-1 的正确结论是：**

| 维度 | 结论 |
| --- | --- |
| 代码里的硬编码 | ✅ **已消除**，改为从 `app.base-url` 读 |
| 默认头像与上传图片是否一致 | ✅ **已统一**，都走 `baseUrl`，都指向 `/file/` |
| 图片实际能否显示 | ✅ **能**——只要 nginx 在 21001 上运行 |
| 我上次的判断 | ❌ **错了**：我说「21001 无人监听、图片必然不可见」，实际它是 nginx 端口，是有意设计 |
| 残留隐患 | ⚠ **降级，但仍存在**（见下） |

**残留隐患（从 P0 降级为 P2）：**

1. **部署耦合**：`app.base-url` = `http://localhost:21001/file/` 是**绝对地址**，且**写死在 yml 里**。换机器、换端口、上域名、上 CDN 都要改配置；而且 `localhost` 语义在真实服务器上会指向服务器自己。
2. **单点依赖**：**nginx 不启动，全站图片（含默认头像）都不显示**。开发时如果只起后端 + Vite，图片必然裂——这是最容易踩的坑，必须写进 README。
3. **仍存绝对 URL 入库**：`user.avatar` 与 `post.images` 存的是完整 URL，不是相对路径。

> **结论：P0-1 不需要前端"擦屁股"了**，之前计划的「检测到 21001 就把端口重写为 8080」不再作为主策略（仅保留为开发期兜底，见第 4.3 节）。这条不再是开工阻塞项。

### 0.3 P0-2 复查详情

`ReviewerServiceImpl.java` 第 66 行（修改时间 `2026/10/27:32`）：

```java
// 现在：逻辑正确 —— judgeUidIsRepeat 返回 true 表示"已存在"，此时才报重复
if (judgeUidIsRepeat(reviewerDTO.getUid())) {
    throw new BusinessException("uid已经重复");
}
```

配合 `judgeUidIsRepeat`（`selectOne(...) == null ? false : true`），语义现已一致：**uid 已存在 → true → 抛"已重复"；uid 不存在 → false → 放行注册**。

✅ **修复正确，审查员注册接口现在可用。**

**连带新增的边界问题（P2，非阻塞）：**

1. `judgeUidIsRepeat(null)`：`LambdaQueryWrapper.eq(field, null)` 在 MyBatis-Plus 中默认会**跳过该条件**，导致 `selectOne` 查出任意一行 → 返回 `true` → 报"uid已经重复"。即 **`uid` 传空会得到错误提示而不是"参数缺失"**。前端应在提交前本地做非空 + 长度校验。
2. `password` 为 `null` 时 `passwordEncoder.encode(null)` 抛异常 → 被兜底成「系统异常」。前端同样本地校验。
3. `inviteCode` 比较用的是 `.equals`（区分大小写），默认值 `daymark-reviewer`。

### 0.4 既有 nginx 对新方案的影响（新增需求）

| 影响 | 说明 | 应对 |
| --- | --- | --- |
| **history 模式需要 nginx 改造** | 现有 `nginx.conf` 的 `/` 只有 `root html; index index.html;`，**没有 `try_files`**。产出是 SPA，**直接访问 `/home`、`/post/3` 会 404**（或刷新即 404） | ⚠ **必须加** `try_files $uri $uri/ /index.html;`。这也解释了旧前端为什么用 hash 模式——**旧 nginx 配置本来就只支持 hash**。你已选 history，则 nginx 必须同步改 |
| 产物部署位置变了 | 新项目在 `dsh\daymark`，nginx 的 `root html` 指向 `frontend\nginx-1.30.5\html` | 部署时把 `dsh\daymark\dist\*` 拷到 `html\`，或把 `root` 改为新路径 |
| dev 与 prod 图片路径不同 | dev：Vite 21000，图片来自 nginx 21001；prod：同源 21001 | 见 4.3 图片策略 |

---

## 1. 后端契约（前端架构的硬约束）

只列**决定前端写法**的条目，全部来自源码实测。

| # | 契约事实 | 源码 | 前端应对 |
| --- | --- | --- | --- |
| 1 | **`code === 1` 才是成功**，`0` 失败 | `Result.java`（注释原文："1成功，0和其它数字为失败"） | 唯一判据；用字面量类型锁死 |
| 2 | 业务失败**返回 HTTP 200** | `GlobalExceptionHandler` 无 `@ResponseStatus` | 禁止用 `res.ok` 判断业务成败 |
| 3 | **401 无响应体**，请求头名是 **`token`**（无 `Bearer`） | `JwtTokenUserInterceptor` | **必须先判 `res.status === 401` 再解析 JSON** |
| 4 | 兜底异常被吞成 `"系统异常，请稍后重试"` | `GlobalExceptionHandler:20` | 拿到该文案 = 后端 bug，引导反馈而非重试 |
| 5 | `LoginVO` **只有 `{jwt, role}`** | `LoginVO.java` | 登录后必须补 `GET /user/me` |
| 6 | `role`：0=用户，1=审查员；`sub`=uid | `LoginRole` / `JwtUtil` | 前端解 JWT 做预判；角色隔离是**前端职责** |
| 7 | **请求 `image`（单数）/ 响应 `images`（复数）** | `PostDTO` vs `PostVO` | 两个 interface 分开命名；发错**静默丢图** |
| 8 | 上传字段名：`/register/avatar` 与 `/user/me/avatar` 用 **`avatar`**；`/post/avatar` 用 **`avatars`** | 各 Controller | API 层封装差异 |
| 9 | `/user/me/avatar` **不在拦截器白名单** | `WebConfig:31-39` | 改头像必须带 token |
| 10 | 分页**完全不存在**，列表全量返回 | 各 `showBy*` / `GET /comment` | 按 `T[]` 处理，但留可替换抽象 |
| 11 | `GET /post/showByTitle` 是**唯一按 `status=1` 过滤**的列表接口 | `PostInformationImpl.showByTitle` | 首页"浏览全部"只能靠 `showByTitle("")` |
| 12 | **无 `GET /post/{id}`** | `PostController` | 详情页**刷新/分享必失败** → 必须做降级态 |
| 13 | **无 `GET /user/{uid}`** | 无 | 帖子/评论**只能显示「用户 {id}」**，不伪造 |
| 14 | **无「我点赞/我收藏」列表** | `LikeController`/`FavoriteController` | 不实现；UI 标注后端未提供 |
| 15 | 点赞/取消/收藏**全部不幂等**，会抛业务异常 | `LikeServiceImpl` | 乐观更新 + pending 锁 + 失败回滚 |
| 16 | `DELETE /comment` **只比对 body 的 `userId`，不校验令牌** | `CommentServiceImpl:62` | 越权风险；UI 层只给自己的评论显示删除按钮 |
| 17 | `DELETE /comment` **body 必须含 `id+userId+postId`** | 同上 + `updatePostCommentCount` | 缺 `postId` → 「系统异常」 |
| 18 | 编辑帖子**清零三个计数**且**重置为审核中** | `PostInformationImpl.updatePost` | 编辑后重新拉取；提示"将重新审核" |
| 19 | `updatePost` 必带 `userId`（原始 `int`，缺省=0） | 同上 | 先确保 `/user/me` 拿到 `id` 再允许编辑 |
| 20 | **`post.images` 存 JSON**，`PostVO.images` 是数组 | `JacksonTypeHandler` | 类型 `string[]`；响应可能为 `null` |
| 21 | 时间字段 `LocalDateTime`，**无时区后缀** | 各 VO | 禁止 `new Date(str)`，自写解析器 |
| 22 | 改密码字段是 **`password`**，不是 `oldPassword` | `UserPasswordDTO` | 按 `password` 提交 |
| 23 | 改密码未判 `user == null` | `UserInformationServiceImpl:152` | 进改密页前确保 me 已加载成功 |
| 24 | 注册默认 `uid` 可能为 **`null`** | `RegisterServiceImpl.createUsername(5)` | 前端本地兜底生成 |
| 25 | `GET /review` 返回的 `PostVO` **计数恒为 0** | `ReviewerServiceImpl` | 审核台**不显示**计数 |
| 26 | `showByUid` 缓存路径**不过滤 `status`** | `PostInformationImpl.getPostsByUserId` | 显示真实状态徽章，不本地过滤 |
| 27 | **`user:{uid}` 缓存键同时存 `UserEntity`（含 password）与 `UserVO`** | `UserInformationServiceImpl:81` / `PostInformationImpl:243` | ⚠ **密码哈希可能混入响应** → 白名单剥离 + zod `.strip()` |
| 28 | CORS 全放开 | `WebConfig` | dev 不用代理也不跨域，但仍用代理保持同源 |
| 29 | 后端端口 **8080**；图片由 **nginx 21001** 的 `/file/` 直发 | `application.yml` + `nginx.conf` | 代理与图片策略见第 4 节 |

### 1.1 接口全清单（实测，共 36 个端点）

| 模块 | 方法与路径 | 鉴权 | 请求 | 返回 `data` |
| --- | --- | --- | --- | --- |
| 认证 | `POST /login` | 否 | `{uid,password}` | `LoginVO{jwt,role:0}` |
| 认证 | `GET /register` | 否 | — | `{uid,avatarUrl}` |
| 认证 | `POST /register` | 否 | `{uid,password,nickname,bio,avatar}` | 提示串 |
| 认证 | `POST /register/avatar` | 否 | multipart **`avatar`** | URL 串 |
| 认证 | `POST /reviewerLogin` | 否 | `{uid,password}` | `LoginVO{jwt,role:1}` |
| 认证 | `POST /reviewerRegister` | 否 | `{uid,password,inviteCode}` | `null` |
| 用户 | `GET /user/me` | **是** | — | `UserVO{id,uid,nickname,bio,avatar}` |
| 用户 | `PUT /user/me/update/information` | **是** | `{nickname,bio,avatar}` | 提示串 |
| 用户 | `PUT /user/me/update/password` | **是** | `{password,newPassword}` | 提示串 |
| 用户 | `POST /user/me/avatar` | **是** | multipart **`avatar`** | URL 串 |
| 帖子 | `POST /post/publish` | 是 | `{title,content,`**`image`**`[]}` | 提示串 |
| 帖子 | `GET /post/showByUid?uid=` | 否 | query | `PostVO[]` |
| 帖子 | `GET /post/showByNickname?nickname=` | 否 | query | `PostVO[]` |
| 帖子 | `GET /post/showByTitle?title=` | 否 | query | `PostVO[]`（唯一过滤 status=1） |
| 帖子 | `POST /post/updateStatus` | 是 | `{id,status}` | `null` |
| 帖子 | `POST /post/updatePost` | 是 | `{id,`**`userId`**`,title,content,`**`image`**`[]}` | `null` |
| 帖子 | `POST /post/avatar` | 是 | multipart **`avatars`** | `URL[]` |
| 评论 | `PUT /comment` | 是 | `{postId,content,commentObjectIsPost}` | 提示串 |
| 评论 | `DELETE /comment` | 是 | `{id,userId,postId}` | 提示串 |
| 评论 | `GET /comment?postId=` | 否 | query | `CommentVO[]`（扁平） |
| 点赞 | `PUT /like?postId=` | 是 | query | `null` |
| 点赞 | `DELETE /like?postId=` | 是 | query | `null` |
| 点赞 | `GET /like/isLike?postId=` | 否 | query | `boolean` |
| 点赞 | `GET /like/likeInformation?postId=` | 否 | query | `LikeVO[]` |
| 收藏 | `PUT /favorite?postId=` | 是 | query | `null` |
| 收藏 | `DELETE /favorite?postId=` | 是 | query | `null` |
| 收藏 | `GET /favorite/isFavorite?postId=` | 否 | query | `boolean` |
| 收藏 | `GET /favorite/favoriteInformation?postId=` | 否 | query | `FavoriteVO[]` |
| 审核 | `GET /review` | 是 | — | 待审 `PostVO[]` |
| 审核 | `POST /review` | 是 | `{id,status}` | `null` |

`PostStatus`：`0` 驳回 / `1` 通过 / `2` 审核中 / `3` 已删除。

---

## 2. 技术栈（正式选型）

### 2.1 依赖清单

| 类别 | 选型 | 锁定版本 | 说明 |
| --- | --- | --- | --- |
| 运行时 | Node.js | ≥ 22.18（本机 **24.18.0** ✅） | Vite 8 要求 |
| 包管理 | pnpm | ≥ 10（本机 **12.4.2** ✅） | 严格锁文件 |
| 框架 | **vue** | `^3.5` | `<script setup>` + Composition API |
| 语言 | **typescript** | `^5.9` | 见 2.2 |
| 构建 | **vite** | `^8` | — |
| 官方插件 | `@vitejs/plugin-vue` | `^6` | — |
| 类型检查 | `vue-tsc` | 最新 | SFC 模板内类型也查 |
| 路由 | **vue-router** | `^4` | **history 模式** |
| 状态 | **pinia** | `^3` | — |
| 工具集 | `@vueuse/core` | `^13` | debounce / localStorage / IntersectionObserver |
| HTTP | **自研 typed fetch** | — | 不引 axios（理由见 2.3） |
| 校验 | **zod** | `^4` | 表单 + 响应校验 + 密码剥离 |
| 图标 | `lucide-vue-next` | 最新 | tree-shakable |
| Lint | `oxlint` + `eslint` + `eslint-plugin-vue` + `prettier` | 与既有配置同源 | oxlint 粗筛，eslint 精查 |
| 单测 | `vitest` + `@vue/test-utils` + `happy-dom` | 最新 | 契约测试优先 |
| E2E | `@playwright/test` | 最新 | 主链路 |
| 自动导入 | `unplugin-auto-import` + `unplugin-vue-components` | 最新 | 免 import `ref/computed`、免注册组件 |

**不引入**：axios、element-plus / ant-design-vue / naive-ui、tailwindcss、nuxt、TanStack Query、moment、dayjs。

### 2.2 选 TypeScript 的理由（核心决策）

后端有 29 条契约事实，其中**大半是"静默错误"类型**——写错了不报错，只在运行时表现为数据丢失或图片消失。这正是 TS 的主场。

| 后端陷阱 | JS 的后果 | TS 的收益 |
| --- | --- | --- |
| 请求 `image` / 响应 `images` | **静默丢图，零报错** | 两 interface 字段名不同 → **编译期报错** |
| `code===1` 才成功 | 写成 `0` → 全站判失败 | 字面量类型 `1 \| 0` + 判别联合 |
| `uid:string` vs `id:number` | 传错变 0 → "作者不匹配" | 类型不可互换 |
| `LoginVO` 只有 `{jwt,role}` | 误读 `user.nickname` → undefined | 精确 interface 立即报错 |
| 401 无响应体 | 先 `json()` → 吞掉真实语义 | `never` 返回 + 显式 `AuthError` 分支 |
| 密码哈希可能混入 `UserVO` | 整体存储/打印 → 泄漏 | 白名单 interface（无索引签名） |
| 时间无时区 | `new Date(str)` 行为不一致 | Branded type 强制走统一解析器 |

### 2.3 不引 axios 的理由

后端 3 条非标准行为（头名 `token`、401 空 body、`code===1`）任何库都要写胶水；自研 20 行 `fetch` 反而能给出**类型精确**的 `ApiError | AuthError` 判别联合，且少一个依赖。

### 2.4 样式与路由决策

- **样式**：原生 CSS + 设计令牌（CSS 变量）+ `@layer`，SFC `<style scoped>` 隔离。不引 Tailwind（与 scoped 风格分裂）、不引 UI 库（小红书式视觉冲突）。
- **路由**：**history 模式**（已确认）。URL 形如 `/home`、`/post/3`。⚠ **必须同步给 nginx 加 `try_files`**（见 0.4）。

---

## 3. 项目结构

```
D:\zxc\project\Daymark\dsh\daymark\
├── package.json  pnpm-lock.yaml
├── tsconfig.json  tsconfig.app.json  tsconfig.node.json
├── vite.config.ts  vitest.config.ts  playwright.config.ts
├── .env.development   # VITE_API_BASE=/api
├── .env.production
├── eslint.config.js  .oxlintrc.json  .prettierrc.json  .editorconfig  .gitignore
├── index.html
├── README.md                       # 启动方式（含 nginx 依赖）+ 契约速查 + 陷阱清单
├── docs/backend-contract.md        # 第 1 节的工程化副本
└── src/
    ├── main.ts   App.vue   env.d.ts
    ├── api/
    │   ├── http.ts                 # token 头 / 401 前置 / code===1 / 超时 / 取消
    │   ├── errors.ts               # ApiError(code) / AuthError / NetworkError
    │   ├── types/{envelope,user,post,interaction,reviewer}.ts
    │   ├── schemas.ts              # zod 响应校验 + UserVO 白名单剥离
    │   └── modules/{auth,user,post,comment,like,favorite,review}.ts
    ├── composables/
    │   ├── useAsync.ts             # loading/error/data 状态机
    │   ├── useUpload.ts            # 类型/大小/数量校验 + 3 接口字段名差异
    │   ├── usePaginationShim.ts    # 全量数组→前端切片（后端补分页后换实现）
    │   └── useTime.ts              # LocalDateTime 安全解析 + 相对时间
    ├── stores/
    │   ├── auth.ts  postList.ts  postDetail.ts  interaction.ts  userCache.ts  toast.ts
    ├── router/{index.ts,guards.ts}
    ├── components/
    │   ├── base/      Button Input Modal Skeleton Empty Badge Tabs
    │   ├── post/      PostCard PostGrid PostImages PostStatusBadge
    │   ├── comment/   CommentList CommentItem CommentEditor
    │   ├── common/    ImageLightbox UserAvatar ToastHost ConfirmDialog
    │   └── layout/    AppHeader TabBar PageShell
    ├── views/
    │   ├── LoginView RegisterView ReviewerRegisterView
    │   ├── HomeView PostDetailView PublishView MeView ReviewerView NotFoundView
    ├── utils/
    │   ├── constants.ts   # PostStatus / LoginRole 镜像
    │   ├── format.ts      # 数字缩写 / 相对时间 / 状态文案
    │   ├── assetUrl.ts    # resolveAssetUrl()
    │   ├── validate.ts    # zod schema 集合
    │   └── storage.ts
    ├── styles/{tokens,reset,base,utilities}.css
    └── tests/contract/  tests/components/
```

### 3.1 分层契约（ESLint `no-restricted-imports` 强制）

```
views/ components/   ← 只编排与展示，禁止直接 fetch
   ↓
stores/              ← 状态、缓存、乐观更新、pending 锁
   ↓
api/modules/         ← 路径与字段名的唯一真相（单复数映射只在这里）
   ↓
api/http.ts          ← token 头、401 前置、code===1、错误分类
   ↓
fetch
```

**硬规则**：`views/`、`components/` **禁止** import `api/http.ts` 或 `api/modules/*`。

---

## 4. 关键实现策略

### 4.1 请求层（`api/http.ts`）

```
① headers.token = localStorage 里的 JWT（无 Bearer）
② fetch(API_BASE + path + query)
③ if (res.status === 401) → clearSession() + AuthError     ← 必须最先，因为无 body
④ text = await res.text(); if (!text) → 按成功无数据处理
⑤ JSON.parse → if (code === 1) return data
              else throw ApiError(message, code)
⑥ catch 网络异常 → NetworkError("请确认后端已启动")
```

### 4.2 密码剥离（`api/schemas.ts`）——针对契约事实 #27

```ts
// 只保留白名单字段，任何多余字段（尤其 password）被 strip 掉
export const UserVOSchema = z.object({
  id: z.number(), uid: z.string(),
  nickname: z.string().nullish(), bio: z.string().nullish(),
  avatar: z.string().nullish(),
}).strip()
```

**硬规则**：用户对象**永不**整体 `JSON.stringify`、**永不**存 localStorage、**永不**透传给组件。

### 4.3 图片地址策略（`utils/assetUrl.ts`）

当前后端 `app.base-url = http://localhost:21001/file/`，所以图片是**绝对 URL**。`resolveAssetUrl()` 处理四种形态：

| 输入 | 处理 |
| --- | --- |
| `http://localhost:21001/file/xxx` | 原样返回；**但 dev 时若 nginx 未启动会裂图** → `@error` 兜底把 `:21001` 重写为 `:8080`（后端 `/file/**` 也映射同一目录），仅开发期，记为 `DEV_FALLBACK` |
| `/file/xxx`、`/images/xxx` | 拼 `window.location.origin`（交给 Vite 代理或 nginx） |
| `data:` / `blob:` | 原样 |
| 空 / `null` | 返回占位图 |

### 4.4 开发与部署拓扑

| 环境 | 拓扑 | 说明 |
| --- | --- | --- |
| **开发** | `browser → Vite :21000 ─(proxy /api,/file,/images)→ :8080` | 只需后端；图片建议同时起 nginx:21001，否则依赖 4.3 的兜底重写 |
| **部署（现状）** | `browser → nginx :21001 ─┬─ / → html/ ──┬─ /api/ → :8080 ──┴─ /file/ → upload/` | 与后端 `app.base-url` 一致 |

**Vite 代理配置**（开发期）：

```
/api     → http://localhost:8080    （rewrite 去掉 /api 前缀）
/file    → http://localhost:8080
/images  → http://localhost:8080
```

### 4.5 nginx 改造需求（history 模式的前置条件）

现有 `frontend\nginx-1.30.5\conf\nginx.conf` 第 43-46 行：

```nginx
location / {
    root   html;
    index  index.html index.htm;
}
```

**必须改为：**

```nginx
location / {
    root   html;
    index  index.html;
    try_files $uri $uri/ /index.html;      # ← SPA 回退，history 模式必需
}
```

否则 `/home`、`/post/3` 直接访问或刷新即 404。

### 4.6 交互细节（针对不幂等接口）

点赞/收藏采用：**乐观更新 → 置 pending 锁 → 请求 → 失败回滚并 toast**。pending 期间按钮禁用，杜绝连点（后端重复点赞会抛「这名用户已经点过赞了」）。

---

## 5. 构建流程（六阶段，约 7 个工作日）

| 阶段 | 内容 | 交付物 | 验收标准 | 依赖后端? |
| --- | --- | --- | --- | --- |
| **0** 骨架 | 依赖安装、TS strict、Vite 三路代理、lint/format/vitest/playwright 配置 | 可跑的空白应用 | `dev` 起得来；`typecheck`/`lint`/`build` **全 0 错** | ❌ 不需要 |
| **1** 契约层 | `api/types` + `http.ts` + `schemas.ts` + 7 个 modules | 完整 API 层 | **契约测试全绿**；故意把 `image` 写 `images` 会**编译失败** | ❌ 不需要 |
| **2** 设计系统 | `styles/` 令牌 + `components/base/*` + `components/common/*` | 基础组件库 | 演示页双端（移动/桌面）正常 | ❌ 不需要 |
| **3** 认证与个人 | auth store、路由守卫、Login/Register/ReviewerRegister/Me | 4 个视图 | 注册→登录→改资料→改密→重登 全链路通 | ⚠ 需要后端可运行 |
| **4** 帖子与互动 | Home、Publish、PostDetail、PostCard 等、interaction store | 核心功能 | 发帖→审核→首页可见→点赞/收藏/评论→删除 全链路通 | ⚠ 需要后端可运行 |
| **5** 审核台与收尾 | ReviewerView、NotFound、响应式、README、E2E、部署产物 | 可交付前端 | E2E 全绿；`build` 产物可部署 | ⚠ 需要后端可运行 |

**阶段 0~2 完全不依赖后端运行**，可立即开工；阶段 3 起需要 MySQL + Redis + 后端启动。

### 5.1 每阶段门禁

```
pnpm typecheck  → 0 错误
pnpm lint       → 0 错误
pnpm test       → 契约测试全绿
pnpm build      → 产出 dist/
```

---

## 6. 测试方案

| 层级 | 工具 | 覆盖（按风险排序） |
| --- | --- | --- |
| **契约单测**（最高优先） | Vitest | ① 401 空 body → `AuthError`；② `code===1` 成功 / `code===0` 抛 `ApiError`；③ `publish` 请求体字段名是 **`image`**；④ 解析用 **`images`**；⑤ `DELETE /comment` 含 `id+userId+postId`；⑥ `uid:null` 本地兜底；⑦ 无时区时间串解析；⑧ `UserVO` 剥离后**不含 `password`**；⑨ 上传字段名 `avatar` vs `avatars` |
| 组件测试 | Vitest + test-utils | 点赞 pending 锁与回滚、上传校验、骨架屏/空态切换 |
| E2E | Playwright | 注册→登录→发帖(带图)→审查员审核通过→首页可见→点赞/收藏→评论→删评论→编辑后重新审核 |
| 手工回归 | — | 移动/桌面布局、灯箱键盘操作、401 自动跳登录、角色越权跳转、**nginx 未启动时的图片兜底** |

---

## 7. 后端依赖与待办（阻塞项已明确）

### 7.1 前端开工的阻塞项

| # | 项 | 状态 | 影响 |
| --- | --- | --- | --- |
| 1 | `schema.sql`（**仓库无建表 SQL**） | ❌ 待补 | 阶段 3 起无法联调 |
| 2 | MySQL（`localhost:3306/daymark`）可用 | ❓ 待确认 | 同上 |
| 3 | Redis（默认 `192.168.150.101:6379`）可用 | ❓ 待确认 | 后端起不来 |
| 4 | nginx 是否随开发一起启动 | ❓ 待确认 | 决定图片是否裂 |

### 7.2 后端功能待补（影响前端功能完整性，非开工阻塞）

| 优先级 | 项 | 前端现状 |
| --- | --- | --- |
| P1 | `GET /post/{id}` | 详情页只能靠内存数据，**刷新/分享必失败** → 做降级态 |
| P1 | 列表分页 `page/pageSize/total` | 全量返回；`usePaginationShim` 前端切片（不解决传输） |
| P1 | `GET /user/{uid}` | 只显示「用户 {id}」，不伪造 |
| P1 | `GET /user/me/likes`、`/user/me/favorites` | 不实现，UI 标注 |
| P2 | 静态资源目录配置化（去掉 `D:/zxc/...` 硬编码） | — |
| P2 | `app.base-url` 改相对路径方案 | 前端 `resolveAssetUrl` 已兼容 |

### 7.3 已修复（复查确认）

| 项 | 结论 |
| --- | --- |
| ✅ P0-1 上传 URL 硬编码 | **已修复**：改用 `app.base-url`，且 21001 是 nginx 端口，方案自洽 |
| ✅ P0-2 审查员注册逻辑反了 | **已修复**，逻辑正确，接口可用 |

---

## 8. 风险登记

| 风险 | 等级 | 应对 |
| --- | --- | --- |
| **nginx 未启动 → 图片全裂** | 高 | README 明确写；4.3 的 dev 兜底重写 |
| **无建表 SQL / MySQL / Redis 不可用** | 高 | 列为阶段 3 前置，先做阶段 0~2 |
| **nginx 缺 `try_files` → history 路由 404** | 高 | 4.5 给出确切改法，部署前必改 |
| 后端不补 `GET /post/{id}` | 中 | 详情页降级态，不造假数据 |
| 后端不补分页 | 中 | 前端切片，量级可控 |
| `LocalDateTime` 无时区 | 中 | 统一解析器 + 契约测试 |
| 密码哈希混入 `UserVO` | 中 | zod 白名单剥离 + 类型约束 |
| TS 上手成本 | 中 | 契约层严格，视图层可宽松 |
| 部署绝对 URL 耦合 | 低 | 已从 P0 降级；记录在案 |

---

## 9. 开工清单

| # | 动作 | 状态 |
| --- | --- | --- |
| 1 | 确认目标目录 `dsh\daymark` | ✅ 已确认 |
| 2 | 确认路由模式 history | ✅ 已确认 |
| 3 | 复查两个 P0 | ✅ 已完成（本文件第 0 节） |
| 4 | **创建项目骨架 + 工具链（阶段 0）** | ⏳ **待你放行** |
| 5 | 契约层 + 契约测试（阶段 1） | ⏳ |
| 6 | 设计系统与基础组件（阶段 2） | ⏳ |
| 7 | 向后端索要 `schema.sql`，确认 MySQL/Redis | ⏳ 可与 4~6 并行 |
| 8 | 认证与个人中心（阶段 3） | ⏳ 需 7 |
| 9 | 帖子与互动（阶段 4） | ⏳ |
| 10 | 审核台 + E2E + README（阶段 5） | ⏳ |
| 11 | 部署前给 nginx 加 `try_files`（4.5） | ⏳ 部署时 |

**建议**：立即从阶段 0 开工（不依赖后端），同时并行推进第 7 项。
