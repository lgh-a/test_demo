# test_demo

`test_demo` 是一个在线怀旧游戏演示平台，前后端分离，支持游戏列表、收藏、历史记录、权限控制、后台管理、动态限流和在线游玩。

## 技术栈

- 后端：Spring Boot 3.2.3、MyBatis-Plus 3.5.5、Sa-Token 1.37.0、Redis、Knife4j
- 前端：Vue 3、Vue Router 4、Pinia、Naive UI、Tailwind CSS、Vite
- 模拟器：Nostalgist.js
- 数据库：MySQL 8

## 项目结构

```text
test_demo/
├─ backend/                  Spring Boot 后端
│  ├─ src/main/java/com/testdemo/
│  │  ├─ config/             配置、拦截器、日志、限流缓存
│  │  ├─ controller/         接口控制层
│  │  ├─ dto/                请求对象
│  │  ├─ entity/             数据库实体
│  │  ├─ mapper/             MyBatis-Plus Mapper
│  │  ├─ service/            业务接口与实现
│  │  ├─ vo/                 视图对象
│  │  └─ common/             通用响应与异常
│  └─ src/main/resources/    yml、logback、mapper
├─ frontend/                 Vue 3 前端
│  ├─ src/
│  │  ├─ api/                请求封装
│  │  ├─ components/         业务组件
│  │  ├─ router/             路由与权限守卫
│  │  ├─ store/              Pinia 状态管理
│  │  └─ views/              页面视图
├─ data/
│  ├─ roms/                  游戏 ROM 文件目录
│  └─ thumbs/                游戏封面目录
└─ database.sql              最终数据库初始化脚本
```

## 系统架构

### 后端架构

- `controller -> service -> mapper -> mysql`
- Sa-Token 负责登录态、角色权限校验
- Redis 用于 Sa-Token 会话和动态限流缓存
- `WebConfig` 负责 CORS、静态资源映射、全局登录拦截
- `logback-spring.xml` 负责控制台和文件日志输出
- `GlobalExceptionHandler` 负责统一异常响应

### 前端架构

- `views` 承载页面
- `components` 复用后台管理、收藏、密码修改、用户管理等组件
- `router` 负责登录保护和权限跳转
- `store` 负责用户信息、权限、收藏、历史记录等状态
- `api/request.js` 统一请求、Token 注入和错误处理

### 资源架构

- 游戏 ROM 通过后端静态资源映射暴露为 `/roms/**`
- 缩略图通过后端静态资源映射暴露为 `/thumbs/**`
- 若未显式配置 `ROMS_PATH` 和 `THUMBS_PATH`，系统会自动探测 `data/roms`、`../data/roms`、`data/thumbs`、`../data/thumbs`

## 功能清单

### 前台功能

- 首页展示最近更新、最近新增、按名称排序的游戏列表
- 游戏列表分页、筛选、排序
- 游戏详情播放页
- 基于 Nostalgist.js 的在线游玩
- 收藏游戏
- 最近游玩历史
- 未登录跳转登录
- 无权限页、404 页

### 后台功能

- 用户管理
  - 用户列表
  - 新增用户
  - 修改用户
  - 重置密码
- 角色管理
  - 角色列表
  - 新增角色
  - 删除角色
  - 角色菜单授权
- 菜单权限管理
  - 菜单树查询
  - 新增菜单
  - 修改菜单
  - 删除菜单
- 限流规则管理
  - 规则列表
  - 新增规则
  - 修改规则
  - 删除规则
  - 刷新缓存
- 后台概览
  - 用户数
  - 游戏数
  - 角色数
  - 菜单数

### 认证与权限

- 登录接口：`/api/auth/login`
- 当前用户信息：`/api/auth/info`
- 全局登录拦截：默认拦截 `/api/**`
- 放行接口：
  - `/api/auth/login`
  - `/api/categories/consoles`
  - `/api/categories/series`
  - `/api/games/list`
  - `/api/demo/sendSms`
- 细粒度权限点示例：
  - `game:play`
  - `sys:user:list`
  - `sys:role:list`
  - `sys:menu:list`
  - `rate-limit:rule:list`

## 主要接口模块

- `AuthController`：登录、登出、当前用户信息
- `CategoryController`：主机、系列分类
- `GameController`：游戏列表、游戏详情
- `UserHistoryController`：游玩历史
- `UserFavoriteController`：收藏管理
- `SysUserController`：后台用户管理
- `SysRoleController`：后台角色管理
- `SysMenuController`：后台菜单权限管理
- `RateLimitRuleController`：动态限流规则管理
- `AdminOverviewController`：后台概览

## 配置说明

### 后端核心环境变量

- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `REDIS_DATABASE`
- `ROMS_PATH`
- `THUMBS_PATH`
- `CORS_ALLOWED_ORIGIN_1`
- `CORS_ALLOWED_ORIGIN_2`
- `LOG_PATH`

### 前端环境变量

- `VITE_API_BASE_URL`

开发环境默认值在：

- `frontend/.env.development`
- `frontend/.env.production`

## 数据目录要求

将 ROM 和封面放到以下目录：

- `data/roms/`
- `data/thumbs/`

当前示例数据内置了一条可直接测试的游戏记录：

- `/roms/Adventure Island 4 (English v1.0).nes`
- `/thumbs/t1.png`

## 默认账号

- 管理员：`admin / 123456`
- 普通用户：`player / 123456`

## 启动方式

### 1. 初始化数据库

执行根目录脚本：

```sql
source database.sql;
```

或直接在 MySQL 客户端执行 `database.sql` 全量建库建表。

### 2. 启动后端

```powershell
cd backend
mvn spring-boot:run
```

默认地址：

- API：`http://localhost:8080/api`
- 文档：`http://localhost:8080/doc.html`

### 3. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

默认前端地址：

- `http://localhost:5173`

## 数据库设计摘要

### 业务表

- `game_console`
- `game_series`
- `game_info`
- `user_history`
- `user_favorite`

### 权限表

- `sys_user`
- `sys_role`
- `sys_menu`
- `sys_user_role`
- `sys_role_menu`

### 限流表

- `sys_rate_limit_rule`

## 日志与测试

- 运行日志输出到 `backend/logs/` 或自定义 `LOG_PATH`
- 后端已有测试目录：`backend/src/test/`
- 前端当前以构建校验为主：`npm run build`

## 当前仓库约定

- 说明文档统一收口到本文件
- 数据库初始化统一使用根目录 `database.sql`
- 历史测试日志、过程性报告、迁移碎片文件不再保留
