# daymark 前端

按后端 `daymark`（Spring Boot）**源码实测契约**构建的全新前端。
技术栈：**Vue 3.5 + TypeScript 5.9 + Vite 8 + Pinia 4 + Vue Router 5（history 模式）+ zod**。

> 契约唯一来源是后端 `daymark/src/main/java` 的源码，**不是**根目录的《Daymark-接口文档.md》
> （那份文档与实现系统性冲突）。

## 启动

```bash
pnpm install
pnpm dev          # http://localhost:21000
pnpm build        # 产物 dist/
pnpm typecheck    # vue-tsc 类型检查
pnpm lint         # oxlint
pnpm test         # 契约测试（vitest）
```

后端需先起（`server.port: 8080`），dev 代理已配好（同源请求，无跨域）：

| 前端请求 | 转发到 |
| --- | --- |
| `/api/xxx` | `http://localhost:8080/xxx`（去掉 `/api` 前缀） |
| `/file/**` | `http://localhost:8080/file/**` |
| `/images/**` | `http://localhost:8080/images/**` |

## 三条必须先知道的契约事实

1. **`code === 1` 才是成功**（`code === 0` 业务失败）。这是后端 `Result.java` 注释原文，
   与「0 成功」的直觉相反。
2. **业务失败仍返回 HTTP 200**，不能靠 HTTP 状态码判断成败。
3. **401 无响应体**，且鉴权请求头名是 **`token`**（无 `Bearer` 前缀）。
   → 请求层**必须先判断 `res.status === 401` 再解析 JSON**。

详见 `docs/backend-contract.md`（契约事实全集）与各 `src/api/modules/*.ts` 的字段级注释。

## 当前进度

| 阶段 | 状态 |
| --- | --- |
| 0 骨架 + 工具链 | ✅ 完成（typecheck / lint / test / build 全绿） |
| 1 契约层 + 契约测试 | ✅ 完成 |
| 2 设计系统与基础组件 | ✅ 完成（8 base + 4 common 组件，/demo 演示页） |
| 3 认证与个人中心 | ✅ 完成（登录/注册/审查员注册/资料/改密/我的帖子） |
| 4 帖子与互动 | ✅ 完成（发现页/发布/详情/点赞收藏/评论） |
| 5 审核台 + 响应式 + E2E | ✅ 完成（审核台、桌面/移动响应式、Playwright 配置与冒烟/主链路 spec） |

当前 19 个单元/契约测试全绿；`pnpm typecheck / lint / test / build` 全绿。

**未完成的运行时验收**（需要后端环境）：E2E 主链路、以及阶段 3~4 的端到端联调。
前置条件：后端 8080 运行 + MySQL + Redis + 建表 SQL（仓库仍无 `schema.sql`）。
E2E 首次运行还需 `pnpm exec playwright install chromium`（下载浏览器）。

## 目录

```
src/
├── api/
│   ├── http.ts          # fetch 封装（token 头 / 401 前置 / code===1）
│   ├── errors.ts        # ApiError / AuthError / NetworkError
│   ├── schemas.ts       # zod 响应校验 + UserVO 白名单剥离（防密码泄漏）
│   ├── types/           # 全部 DTO/VO（请求 image 单数 / 响应 images 复数 分名）
│   └── modules/         # auth / user / post / comment / like / favorite / review
├── stores/              # auth / postList / postDetail / interaction / toast
├── router/              # history 路由 + 登录态/角色守卫
├── composables/         # useUpload / usePaginationShim
├── utils/               # constants / format / assetUrl / storage / validate / uid
├── views/               # 9 个视图（已实现）
├── e2e/                 # Playwright 冒烟 + 主链路 spec
└── styles/              # tokens / reset / base / utilities
```
