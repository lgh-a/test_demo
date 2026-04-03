# Sa-Token 全局登录拦截开发文档

## 目标

为当前项目后端增加 `Sa-Token` 全局登录拦截，统一兜底需要登录的接口，减少在控制器里手写 `StpUtil.isLogin()` 的分散判断。

本任务只要求输出后端改造方案文档，供 AI 编辑器按文档执行。

---

## 已确认的项目现状

基于当前仓库代码，以下事实已经确认：

1. 后端项目路径为 `backend`
2. 技术栈为 `Spring Boot 3.2.3 + Spring MVC + MyBatis-Plus + Sa-Token 1.37.0`
3. `Sa-Token` 已集成，`token-name` 已配置为 `satoken`
4. 当前项目已经有：
   - `GlobalExceptionHandler` 统一处理 `NotLoginException`
   - `StpInterfaceImpl` 提供角色和权限列表
   - `Knife4j` 已集成
5. 当前 `WebConfig` 只有静态资源和 CORS 配置，还没有注册 Sa-Token 登录拦截器
6. 当前权限控制主要依赖 `@SaCheckPermission`
7. 当前前端匿名访问链路已经存在：
   - 首页读取分类
   - 游戏列表页读取列表
   - 未登录时可以打开登录弹窗并调用登录接口
8. 当前前端已依赖登录态的接口：
   - `/api/auth/info`
   - `/api/auth/logout`
   - `/api/games/{id}`
   - `/api/menus/**`
   - `/api/roles/**`

---

## 现有接口清单与拦截结论

以下结论基于当前控制器定义和前端实际调用关系，不按猜测处理。

### 一、应匿名放行的接口

这些接口是首页、列表页、登录流程或公共演示接口所必需，不应被全局登录拦截挡住：

1. `POST /api/auth/login`
   - 登录入口，必须放行
2. `GET /api/categories/consoles`
   - 首页公共分类数据，前端未登录时即会调用
3. `GET /api/categories/series`
   - 首页公共分类数据，前端未登录时即会调用
4. `GET /api/games/list`
   - 游戏列表页公共数据，前端未登录时即会调用
5. `GET /api/demo/sendSms`
   - 当前代码没有登录或权限注解，且属于限流演示接口
   - 建议默认放行，避免把演示接口行为改掉

### 二、应纳入全局登录拦截的接口

这些接口要么已经是登录态接口，要么已经依赖权限注解，要么具有明显后台管理属性，应统一要求先登录：

1. `POST /api/auth/logout`
2. `GET /api/auth/info`
3. `GET /api/games/{id}`
   - 当前已有 `@SaCheckPermission("game:play")`
   - 全局登录拦截负责“先登录”，权限注解继续负责“是否有 game:play”
4. `GET /api/menus/list`
5. `POST /api/menus/add`
6. `PUT /api/menus/{id}`
7. `DELETE /api/menus/{id}`
8. `GET /api/roles/list`
9. `POST /api/roles/add`
10. `DELETE /api/roles/{id}`
11. `GET /api/roles/{id}/menus`
12. `POST /api/roles/{id}/menus`
13. `POST /api/demo/admin/updateRule`
   - 当前没有权限注解，但路径已带 `admin`
   - 至少应要求登录，建议后续再补权限注解

### 三、无需纳入 `/api/**` 登录拦截范围的路径

以下路径不属于业务 API 登录校验目标，不建议被拦：

1. `/roms/**`
   - 静态 ROM 资源
2. `/thumbs/**`
   - 静态缩略图资源
3. `/doc.html`
4. `/v3/api-docs/**`
5. `/swagger-ui/**`
6. `/webjars/**`

严格来说，如果拦截器只对 `/api/**` 生效，上述非 `/api/**` 路径不会被匹配到；但为了后续维护清晰，文档中仍建议显式说明。

---

## 推荐改造方案

### 方案原则

1. 全局登录拦截只负责“是否已登录”
2. 细粒度权限仍由现有 `@SaCheckPermission` 负责
3. 不改变现有接口 URL、请求方法、返回结构
4. 不修改前端 `satoken` 请求头逻辑
5. 以“白名单放行 + 其余 `/api/**` 默认校验登录”为准

### 推荐修改文件

1. `backend/src/main/java/com/testdemo/config/WebConfig.java`
   - 在现有配置类中新增 `addInterceptors`
2. `backend/src/main/java/com/testdemo/controller/AuthController.java`
   - 可选清理 `getInfo()` 里的手动登录判断
3. `backend/src/main/java/com/testdemo/controller/RateLimitDemoController.java`
   - 可选补充权限注解给 `/api/demo/admin/updateRule`

如不希望把职责继续堆在 `WebConfig`，也可以新建：

`backend/src/main/java/com/testdemo/config/SaTokenConfigure.java`

但从当前项目规模看，直接放进 `WebConfig` 更直接。

---

## 具体修改要求

### 1. 在 `WebConfig` 注册 Sa-Token 拦截器

编辑文件：`backend/src/main/java/com/testdemo/config/WebConfig.java`

新增 `addInterceptors(InterceptorRegistry registry)`，使用 `SaInterceptor` + `SaRouter` 实现。

要求：

1. 拦截范围以 `/api/**` 为主
2. 放行以下匿名接口：
   - `/api/auth/login`
   - `/api/categories/consoles`
   - `/api/categories/series`
   - `/api/games/list`
   - `/api/demo/sendSms`
3. 对其余 `/api/**` 执行 `StpUtil.checkLogin()`
4. 保留现有静态资源映射和 CORS 配置，不要覆盖掉

推荐实现形态：

```java
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
```

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new SaInterceptor(handler -> {
        SaRouter.match("/api/**")
                .notMatch(
                        "/api/auth/login",
                        "/api/categories/consoles",
                        "/api/categories/series",
                        "/api/games/list",
                        "/api/demo/sendSms"
                )
                .check(r -> StpUtil.checkLogin());
    })).addPathPatterns("/**");
}
```

说明：

1. `addPathPatterns("/**")` 配合 `SaRouter.match("/api/**")` 使用，写法清晰
2. 不建议直接在 `excludePathPatterns(...)` 里硬编码全部规则，因为后续维护匿名白名单时更分散
3. 登录异常交给现有 `GlobalExceptionHandler` 处理即可

### 2. `AuthController.getInfo()` 去掉重复登录判断

编辑文件：`backend/src/main/java/com/testdemo/controller/AuthController.java`

当前代码里：

```java
if (!StpUtil.isLogin()) {
    return Result.error("Not logged in");
}
```

在全局登录拦截生效后，这段已经不是主防线，可按以下原则处理：

1. 推荐删除，直接使用 `StpUtil.getLoginIdAsLong()`
2. 如果保守处理，也可以暂时保留，不影响功能

推荐删除的原因：

1. 避免一部分接口走拦截器，一部分接口继续手写判断，导致风格不一致
2. 未登录时统一抛 `NotLoginException`，统一进入全局异常处理

### 3. `AuthController.logout()` 纳入登录拦截

无需额外改业务逻辑，但文档执行时要确认：

1. `/api/auth/logout` 不在匿名白名单中
2. 这样未登录时调用登出会走统一未登录响应

### 4. 保留 `@SaCheckPermission`，不要替换成仅登录校验

以下接口当前已经有权限要求，必须继续保留原注解：

1. `GameController.getGame` 的 `game:play`
2. `SysMenuController` 全部接口
3. `SysRoleController` 全部接口

全局登录拦截不替代权限控制，只是把“必须已登录”前置。

### 5. 为 `/api/demo/admin/updateRule` 增加更明确保护

编辑文件：`backend/src/main/java/com/testdemo/controller/RateLimitDemoController.java`

当前这个接口没有任何登录或权限注解，但路径本身明显是后台接口。最低要求是纳入全局登录拦截；更推荐再补一个权限注解，例如：

```java
@SaCheckPermission("sys:rate-limit:update")
```

如果做这一步，还要同步保证：

1. 权限点名称与数据库菜单/权限数据一致
2. 前端如需调用，已有账号具备该权限

如果当前库里没有对应权限数据，本次可以先只做“要求登录”，不要强行补权限点导致现网不可用。

---

## 不要这样改

1. 不要把 `/api/auth/login` 拦截掉
2. 不要把 `/api/categories/**` 全部拦截掉
3. 不要把 `/api/games/list` 拦截掉
4. 不要删除现有 `@SaCheckPermission`
5. 不要改动前端请求头名称 `satoken`
6. 不要修改统一返回结构 `Result`
7. 不要影响 `/roms/**` 和 `/thumbs/**` 静态资源访问
8. 不要为了全局登录拦截去改动 Knife4j 的访问路径

---

## 建议验证步骤

如果 AI 编辑器具备命令执行能力，修改后至少执行：

```bash
cd backend
mvn -q -DskipTests compile
```

然后按以下顺序验证：

1. 未登录调用 `POST /api/auth/login`，应成功
2. 未登录调用 `GET /api/categories/consoles`，应成功
3. 未登录调用 `GET /api/categories/series`，应成功
4. 未登录调用 `GET /api/games/list`，应成功
5. 未登录调用 `GET /api/auth/info`，应返回未登录错误
6. 未登录调用 `POST /api/auth/logout`，应返回未登录错误
7. 未登录调用 `GET /api/games/{id}`，应先返回未登录错误，而不是直接进入权限判断
8. 登录后调用 `GET /api/auth/info`，应成功
9. 登录后调用后台菜单、角色接口：
   - 有权限时成功
   - 无权限时返回无权限错误
10. 调用 `POST /api/demo/admin/updateRule`
   - 未登录时应被拦截
   - 已登录时至少可进入业务逻辑

---

## 完成标准

本任务完成时，必须同时满足：

1. 后端存在统一的 Sa-Token 全局登录拦截配置
2. 白名单只放行当前确认需要匿名访问的公共接口
3. `/api/auth/info`、`/api/auth/logout`、`/api/games/{id}`、`/api/menus/**`、`/api/roles/**`、`/api/demo/admin/updateRule` 已被纳入登录拦截
4. 现有 `@SaCheckPermission` 权限判断仍然有效
5. 首页、列表页、登录页链路不受影响
6. 后端编译通过

---

## 期望 AI 编辑器输出

AI 编辑器执行完本任务后，应明确汇报：

1. 改了哪些文件
2. 全局登录白名单包含哪些接口
3. 哪些接口被统一纳入登录拦截
4. 是否保留了原有权限注解
5. 编译是否通过
6. 如果没有做接口联调验证，需明确说明：

`已完成代码改造，未执行运行态接口验证`
