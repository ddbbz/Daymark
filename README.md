# Daymark

类小红书风格的社区发帖交流平台：用户注册登录、发布帖子、评论、点赞、收藏、搜索，并支持审查员（管理员）审核机制。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3 · Java 21 · MyBatis-Plus · MySQL · Redis · JWT (jjwt) · BCrypt |
| 前端 | Vue 3 · Vite · Pinia · Vue Router（部署于 nginx） |

## 功能列表

- 用户注册 / 登录（JWT 令牌 + BCrypt 密码加密）
- 帖子发布、修改、删除，图片上传（默认头像 / 帖子多图）
- 帖子评论（增删查）、点赞、收藏
- 按标题模糊搜索帖子
- 审查员体系：审查员注册（邀请码）、审核 / 驳回帖子（状态机：失败=0 / 成功=1 / 审核中=2 / 私密=3）
- 审查员与普通用户界面严格分离（角色守卫 + 导航隔离）

## 目录结构

```
Daymark/
├── daymark/          # 后端工程（Spring Boot）
│   └── src/main/java/com/ddbz/daymark/
├── dsh/daymark/ # 前端工程（Vue 3 + Vite）
└── upload/           # 运行时上传的图片（不纳入版本控制）
```

## 快速开始

### 1. 数据库与缓存

- MySQL：创建数据库 `daymark`，按 `reviewer`、`user`、`post`、`comment` 等表结构建表（字段与 Entity 对齐，`uid` 建议 `varchar(10)` 或适当放宽）；
- Redis：任意可用实例即可。

### 2. 后端启动

```bash
cd daymark
mvn spring-boot:run
```

配置项均支持环境变量覆盖（不设置时使用默认值）：

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `DB_PASSWORD` | `xxx123456` | MySQL 密码（**开源后请务必修改**） |
| `REDIS_HOST` / `REDIS_PASSWORD` | `192.168.150.101` / `xxx123456` | Redis 地址与密码 |
| `JWT_SECRET` | `daymark-secret-key-please-change-me-...` | JWT 签名密钥（**部署时务必更换**） |
| `REVIEWER_INVITE_CODE` | `daymark-reviewer` | 审查员注册邀请码 |

### 3. 前端启动 / 部署

```bash
cd dsh/daymark
npm install
npm run dev        # 开发模式（默认 21000）
npm run build      # 构建到 dist/
```

生产部署：用任意静态服务器托管 `dsh/daymark/dist`，并将 `/api` 请求代理到后端 `8080`，`/images` 静态资源指向上传目录（本项目使用 nginx，参考 `dsh/nginx-1.30.5/conf` 配置）。

### 4. 登录两种角色

- 普通用户：`/register` 注册 → `/login` 登录；
- 审查员：`/reviewerRegister`（需要邀请码）→ `/reviewerLogin` 登录 → 进入审核界面。


### 5. 数据库配置
方式一： 使用DataGrip等工具配置
- 在DataGrip中通过`CREATE DATABASE daymark DEFAULT CHARSET utf8mb4;`创建daymark数据库
- 将db文件下daymark_schema.sql拖入DataGrip中执行，创建表结构

方式二： 使用PowerShell以管理员身份创建
- 运行`mysql -u root -p`，随后输入密码，进入MySQL命令行界面
- 执行`CREATE DATABASE daymark DEFAULT CHARSET utf8mb4;`创建daymark数据库
- 执行`USE daymark;`切换到daymark数据库
- 执行`source D:\zxc\project\Daymark\db\daymark_schema.sql;`导入表结构
