# Sa-Token Redis Migration Task

## Goal

Migrate the backend `Sa-Token` storage from the current default in-memory mode to `Redis`.

Project layout:

- Backend Maven project: `backend`
- Frontend project: `frontend`

This task only changes the backend. Do not change the frontend token handling flow.

## Confirmed Current State

Before editing, keep these repo facts in mind:

1. `Sa-Token` is already integrated in `backend/pom.xml`
2. There is currently no Redis dependency in the backend
3. There is currently no `spring.data.redis` configuration
4. There is no custom `SaTokenDao` implementation in the codebase
5. Therefore the current `Sa-Token` storage is the default in-memory implementation

## Required Changes

### 1. Update Maven dependencies

Edit: `backend/pom.xml`

Keep the existing `sa-token-spring-boot3-starter` dependency and add:

1. `spring-boot-starter-data-redis`
2. `sa-token-redis-jackson`

Requirements:

- Keep all `Sa-Token` versions aligned with the existing version `1.37.0`
- Do not remove current dependencies such as MyBatis-Plus, MySQL, Validation, or Lombok
- Do not introduce old Redis integrations that are incompatible with Spring Boot 3

Target dependency shape:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
  <groupId>cn.dev33</groupId>
  <artifactId>sa-token-redis-jackson</artifactId>
  <version>1.37.0</version>
</dependency>
```

### 2. Add Redis configuration

Edit: `backend/src/main/resources/application.yml`

Keep the existing `server`, `spring.datasource`, and `mybatis-plus` configuration. Add Redis configuration under the existing `spring:` node.

Recommended config:

```yml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0
      timeout: 5s
```

If `spring:` already exists, merge into it. Do not create a duplicate root `spring:` block.

### 3. Add Sa-Token configuration

Edit: `backend/src/main/resources/application.yml`

Add `sa-token` configuration with at least:

```yml
sa-token:
  token-name: satoken
  timeout: 2592000
  active-timeout: -1
  is-concurrent: true
  is-share: true
  token-style: uuid
  is-log: true
```

Notes:

- `token-name: satoken` must stay consistent with the current frontend request header
- The frontend already sends the token in the `satoken` header, so do not rename it
- If the project already has partial `sa-token` config, extend it instead of overwriting it

### 4. Optional Redis comment

It is acceptable to add a short comment near the Redis config, for example:

```yml
# Change Redis host, password, and database index for the target environment
```

If needed, a password field may be added as an empty placeholder:

```yml
password:
```

Do not hardcode a password value unless the repository already has one.

## Explicit Non-Goals

Do not do any of the following:

1. Do not change frontend `localStorage` logic
2. Do not change the login API response structure in `AuthController`
3. Do not add a custom `SaTokenDao` class unless there is a strong technical blocker
4. Do not change database schema
5. Do not delete `schema_sa_token.sql`

## Suggested Validation

If command execution is available, run this in `backend` after editing:

```bash
mvn -q -DskipTests compile
```

If runtime verification is possible, also do this:

1. Start Redis
2. Start the backend
3. Call `/api/auth/login`
4. Confirm `Sa-Token` related keys are written into Redis
5. Call `/api/auth/info` and confirm the token still works

## Acceptance Criteria

The task is complete only if all of the following are true:

1. `backend/pom.xml` includes Redis support and `sa-token-redis-jackson`
2. `backend/src/main/resources/application.yml` includes usable Redis config
3. `backend/src/main/resources/application.yml` includes `sa-token.token-name: satoken`
4. The backend compiles successfully
5. Login state is stored in Redis instead of only application memory

## Expected AI Output

After completing the edits, the AI editor should report:

1. Which files were changed
2. Whether compilation passed
3. If runtime Redis verification was not performed, explicitly state: `Code migration completed, Redis runtime integration not verified`
