# Knife4j 接口文档集成任务

## 目标

为当前项目的后端 `backend` 集成 `Knife4j` 接口文档能力，提供：

1. OpenAPI 3 文档输出
2. 可访问的 Knife4j UI 页面
3. 基于当前项目 `Sa-Token` 的调试鉴权方案
4. 按业务域分组的接口文档

本任务只要求修改后端，不要求变更前端业务页面。

---

## 项目上下文

在开始编辑前，AI 编辑器必须基于以下已确认事实执行：

1. 后端项目路径为 `backend`
2. 技术栈为 `Spring Boot 3.2.3 + Java 21 + Spring MVC + MyBatis-Plus`
3. 认证方案为 `Sa-Token`
4. 前端请求头中的登录令牌名称为 `satoken`
5. 当前项目尚未集成 `swagger`、`springdoc`、`knife4j`
6. 当前接口统一返回结构为：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

7. 当前主要控制器分布如下：
   - `AuthController`：认证接口，路径前缀 `/api/auth`
   - `CategoryController`：分类接口，路径前缀 `/api/categories`
   - `GameController`：游戏接口，路径前缀 `/api/games`
   - `SysMenuController`：菜单管理接口，路径前缀 `/api/menus`
   - `SysRoleController`：角色管理接口，路径前缀 `/api/roles`
   - `RateLimitDemoController`：限流演示接口，路径前缀 `/api/demo`

---

## 集成原则

1. 当前项目是 `Spring Boot 3 + Jakarta Servlet`，必须使用 OpenAPI 3 兼容的 Knife4j Starter
2. 不得引入 `springfox` 相关依赖
3. 不得引入仅适用于 `Spring Boot 2` 的旧版 Swagger 配置方式
4. 优先使用注解增强已有控制器，不改变接口 URL、请求方法、响应结构
5. 文档调试鉴权必须使用请求头 `satoken`

---

## 推荐依赖方案

编辑文件：`backend/pom.xml`

推荐引入与 `Spring Boot 3` 兼容的 Starter：

```xml
<dependency>
  <groupId>com.github.xiaoymin</groupId>
  <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
  <version>4.1.0</version>
</dependency>
```

要求：

1. 不删除现有依赖
2. 不替换 `spring-boot-starter-web`
3. 不引入额外的 `swagger-bootstrap-ui`
4. 不引入 `springdoc-openapi-ui` 作为并行 UI 方案，避免重复入口和冲突

---

## 必须新增的配置

编辑文件：`backend/src/main/resources/application.yml`

在现有配置基础上新增 Knife4j / OpenAPI 相关配置，要求满足：

1. 启用 Knife4j
2. 启用 OpenAPI 文档
3. 保持当前已有 `server`、`spring`、`sa-token`、`mybatis-plus` 配置不变
4. 不创建重复根节点

推荐配置形态：

```yml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: false

knife4j:
  enable: true
```

说明：

1. UI 入口目标为 `/doc.html`
2. OpenAPI JSON 入口目标为 `/v3/api-docs`
3. 如果后续需要分组，允许增加 `springdoc.group-configs`

---

## 必须新增的配置类

建议新增文件：

1. `backend/src/main/java/com/testdemo/config/Knife4jConfig.java`

该配置类的职责必须包括：

1. 声明 OpenAPI 基础信息
2. 设置文档标题、描述、版本号
3. 配置服务端鉴权方案
4. 声明请求头安全方案名称与 `satoken` 绑定

配置要求：

1. 文档标题建议为 `test_demo API`
2. 描述需说明这是一个 `Retro Game Online` 后端接口文档
3. 安全方案类型使用 `APIKEY` / Header 头方式
4. Header 名称必须为 `satoken`
5. 建议默认对受保护接口声明安全要求

---

## 控制器注解改造要求

需要对现有控制器补充 OpenAPI 注解，但不得修改业务逻辑。

### 1. 类级别要求

每个控制器应补充：

1. `@Tag`
2. 清晰的中文名称

推荐分组名称：

1. `认证管理`：`AuthController`
2. `分类查询`：`CategoryController`
3. `游戏服务`：`GameController`
4. `菜单管理`：`SysMenuController`
5. `角色管理`：`SysRoleController`
6. `限流示例`：`RateLimitDemoController`

### 2. 方法级别要求

每个接口至少补充：

1. `@Operation(summary = "...")`
2. 必要时补充 `description`
3. 对请求参数补充 `@Parameter`
4. 对请求体 DTO 或实体补充 `@Schema`

### 3. 响应描述要求

关键接口建议补充：

1. `@ApiResponse(responseCode = "200", description = "调用成功")`
2. `@ApiResponse(responseCode = "500", description = "业务异常或系统异常")`

对鉴权接口还应补充：

1. `401/403` 类语义说明

说明：

当前项目异常被统一包装成 `Result.error(...)`，即使 HTTP 状态码未严格区分，也要在文档语义上写明未登录/无权限场景。

---

## 鉴权调试设计

Knife4j 集成后，AI 编辑器必须保证接口调试可以复用当前登录体系。

设计要求：

1. 在 OpenAPI 安全定义中声明请求头 `satoken`
2. 登录接口 `/api/auth/login` 默认不要求安全认证
3. 需要登录的接口在文档中可以手动填写 `satoken`
4. 受 `@SaCheckPermission` 保护的接口应在描述中注明需要相应权限

建议文档说明示例：

1. 先调用 `/api/auth/login`
2. 从响应中读取 `tokenValue`
3. 在 Knife4j 全局鉴权处填入 `satoken`
4. 再访问 `/api/auth/info` 或后台管理接口

---

## 接口分组建议

如果采用分组配置，推荐如下：

1. `auth`
   - 匹配 `/api/auth/**`
2. `game`
   - 匹配 `/api/categories/**`
   - 匹配 `/api/games/**`
3. `admin`
   - 匹配 `/api/menus/**`
   - 匹配 `/api/roles/**`
4. `demo`
   - 匹配 `/api/demo/**`

要求：

1. 分组名称稳定、可预测
2. 不要把所有接口全部堆到一个默认分组里
3. 分组配置与控制器 `@Tag` 可以同时存在

---

## 数据模型文档化要求

项目中以下类型建议补充 `@Schema` 注解或字段说明：

1. `Result`
2. `SysUser`
3. `GameInfo`
4. `SysRole`
5. `SysMenu`
6. `RateLimitRule`

要求：

1. 不因文档注解而改变序列化字段名
2. 不修改数据库映射关系
3. 敏感字段如密码必须明确标注不可返回或在文档中避免误导

特别注意：

`AuthController.login` 返回的 `userInfo` 如果包含不应暴露的字段，文档中必须准确说明实际返回结构；不要为文档方便而修改现有业务返回。

---

## 静态资源与访问路径约束

当前项目已在 `WebConfig` 中自定义了：

1. `/roms/**`
2. `/thumbs/**`
3. 全局 CORS

集成 Knife4j 时必须确保：

1. 不影响现有静态资源映射
2. 不拦截 `/doc.html`
3. 不拦截 `/v3/api-docs`
4. 不拦截 Knife4j 相关前端资源

如果后续新增拦截器，必须显式放行这些路径。

---

## 非目标

本任务不做以下事情：

1. 不重构控制器
2. 不新增业务接口
3. 不修改前端页面
4. 不修改 `satoken` 的请求头名称
5. 不调整数据库结构
6. 不引入单独的 Swagger 登录体系
7. 不为文档功能新增网关、反向代理或额外模块

---

## 验收标准

任务完成后，必须同时满足以下条件：

1. `backend/pom.xml` 已引入与 `Spring Boot 3` 兼容的 Knife4j Starter
2. `backend/src/main/resources/application.yml` 已具备可用的 Knife4j/OpenAPI 配置
3. 存在独立的 OpenAPI 配置类
4. 现有控制器已补充基础文档注解
5. 文档页面可通过 `/doc.html` 访问
6. OpenAPI JSON 可通过 `/v3/api-docs` 访问
7. 文档调试支持通过 Header `satoken` 进行鉴权
8. 不影响现有业务接口路径和响应结构

---

## 建议验证步骤

如果允许执行验证，建议按以下顺序检查：

1. 在 `backend` 目录执行编译
2. 启动后端服务
3. 打开 `http://localhost:8080/doc.html`
4. 确认能看到分组后的接口文档
5. 调用 `/api/auth/login`
6. 将返回的 `tokenValue` 填入全局 Header `satoken`
7. 调用 `/api/auth/info`
8. 调用一个受权限保护的接口，例如 `/api/menus/list`

---

## AI 编辑器输出要求

完成实现后，AI 编辑器应明确报告：

1. 修改了哪些文件
2. 是否编译通过
3. `/doc.html` 是否可访问
4. `/v3/api-docs` 是否可访问
5. 若未执行运行验证，必须明确说明：

`Knife4j design completed, runtime verification not performed`

