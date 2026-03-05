# Retro Game Online (test_demo)

这是一个基于 Spring Boot 3 + Vue 3 的复古游戏在线游玩平台 (zaixianwan.app 的高度还原克隆版)。

## 快速运行指南

### 1. 数据库准备
- 打开 MySQL 8，执行根目录下的 `schema.sql`，创建 `zaixianwan` 数据库和相应表结构以及初始测试数据。
- 根据您的本地环境，修改 `backend/src/main/resources/application.yml` 中的数据库账号密码 (默认配置为 root/root)。

### 2. 准备游戏 ROM 和封面
- 系统默认配置了静态文件映射：
  - 将游戏 ROM 文件（例如 `.nes`, `.gba`）放入 `D:\project\test_demo\data\roms\` 目录中。
  - 将游戏封面图片放入 `D:\project\test_demo\data\thumbs\` 目录中。
- **注意**：`schema_sa_token.sql` 和 `schema.sql` 一起执行后，新增了权限控制及测试账号数据。请确保您的数据库包含了所有表。

### 3. 测试账号 (SA-Token 权限管理)
- **管理员账号**：`admin` / `123456` (拥有进入后台进行角色/菜单管理的权限，以及任意接口权限)
- **普通用户**：`player` / `123456` (仅拥有游玩及查看列表权限，被拦截后台管理菜单)

### 4. 运行后端 (Spring Boot)
1. 进入 `backend` 目录。
2. 使用 IDEA 等 IDE 打开，或者使用 Maven 运行：`mvn spring-boot:run`。
3. 后端将启动并在 `http://localhost:8080` 提供 API 服务和静态资源托管。

### 5. 运行前端 (Vue 3)
1. 进入 `frontend` 目录。
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 打开浏览器访问终端输出的本地开发地址 (通常为 `http://localhost:5173`)。

即可开始愉快的怀旧游戏之旅！
