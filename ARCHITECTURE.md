# nova-mall 架构说明

## 总体设计
- 多模块多域：用户、订单、商品、购物车、库存各自独立 API / Service / Web。
- 统一入口：Spring Cloud Gateway（8092）负责路由、JWT 鉴权、白名单、重试、熔断、CORS。
- 分层模式：common（公共）→ domain-api（契约）→ domain-service（业务/持久化）→ domain-web（接口/启动）。
- 数据层：默认 H2 内存库（MySQL 兼容），MyBatis Plus 负责 CRUD、逻辑删除和 SQL 日志。

## 架构图
```
             ┌────────────────────────┐
             │   nova-mall-gateway    │ 8092
             └─────────▲──────────────┘
                       │ 路由+鉴权
        ┌──────────────┴────────────────────────────────┐
        │              │              │                  │              │
   user-web        order-web      product-web       cart-web       stock-web
   (8083)          (8084)         (8085)            (8086)         (8087)
        ▲              ▲              ▲                  ▲              ▲
   user-service   order-service   product-service   cart-service   stock-service
        ▲              ▲              ▲                  ▲              ▲
   user-api        order-api      product-api       cart-api       stock-api
        └─────────────────────────┬───────────────────────────────┘
                         nova-mall-common
```

## 模块职责
- `nova-mall-common`：公共 DTO、异常、工具，无 Spring 依赖。
- `*-api`：领域接口与 DTO 契约，不含实现。
- `*-service`：业务实现、实体与 Mapper、事务与校验。
- `*-web`：Controller、参数/权限校验、OpenAPI 文档、启动入口。
- `nova-mall-gateway`：JWT 校验、路径路由、重试/熔断、白名单、可选本地限流。

## 端口与路由前缀
- 网关：8092
- 用户：8083（`/auth/**`, `/user/**`）
- 订单：8084（`/order/**`）
- 商品：8085（`/product/**`）
- 购物车：8086（`/cart/**`）
- 库存：8087（`/stock/**`）
网关按前缀路由并做 JWT 校验，文档与静态资源已加入白名单；各 OpenAPI 已声明 Bearer 安全方案，Knife4j 点击 “Authorize” 后示例调用会带上 `Authorization: Bearer <token>`。

## 数据与初始化
- 默认 H2 内存库，URL 形如 `jdbc:h2:mem:userdb;MODE=MySQL;...`，控制台 `/h2-console` 直连对应端口。
- 切换 MySQL：修改各 Web `application.yaml` 数据源，执行对应 `resources/db/init.sql`。
- MyBatis Plus：开启驼峰映射、SQL 日志、逻辑删除字段 `deleted`。

## 请求链路示例
1. 客户端调用 `/auth/login`（网关 → 用户服务）获取 JWT。
2. 随后请求携带 `Authorization: Bearer <token>` 访问网关。
3. 网关验证签名/白名单后路由到目标 Web。
4. Web 层做参数校验与授权，调用 Service + MyBatis Plus，返回统一 `Result<T>`。

## 秒杀运营场景（nova-mall-ops）
- 场景目标：提供运营侧秒杀活动的配置、上/下线与商品上下架，供下游（商品/前台）按活动窗口拉取和展示。
- 数据模型：`ops_seckill_activity`（见 `nova-mall-ops/src/main/resources/db/init.sql`）包含商品ID、标题、秒杀价、总库存、限购、开始/结束时间、状态（ONLINE/OFFLINE）、逻辑删除等字段。
- 访问入口：`http://localhost:8090/static/ops-admin/index.html`（H2 内存库默认端口 8090），默认账号在 `ops.auth.username/password`。
- 认证：`POST /auth/login` 返回 JWT，运营接口需 `Authorization: Bearer <token>` 且角色 `ROLE_OPS_ADMIN`。
- 运营接口（`nova-mall-ops`）：
  - `GET /admin/activities`：活动列表（按ID倒序）。
  - `GET /admin/activities/active`：仅返回在线且在时间窗内的活动，供下游预热/缓存。
  - `POST /admin/activities`：新建活动，默认状态 OFFLINE。
  - `PUT /admin/activities/{id}`：更新活动。
  - `POST /admin/activities/{id}/online` / `offline`：上线/下线活动。
  - `POST /admin/products/{id}/on-shelf` / `off-shelf`：透传到商品域做上/下架。
- 前端操作流程（静态页 `static/ops-admin/index.html`）：
  1) 运营登录获取 JWT（默认 admin/admin123，可改配置）。
  2) 新建/更新活动：录入商品ID、价格、库存、限购和开始/结束时间，更新时带活动ID。
  3) 上线/下线活动：切换状态后，下游通过 `GET /admin/activities/active` 拉取最新在线活动。
  4) 商品上/下架：调用商品域接口，控制商品整体售卖状态。
- 下游消费建议：在前台/商品侧定时或按事件拉取 `listActive` 结果放入本地缓存，避免活动时间窗外售卖；下单层仍需做库存/时间窗口校验（当前示例未实现扣减与防超卖，需在订单/库存域完善）。

## 演进方向
- 将内存用户迁移到数据库或外部 IAM，完善密码加盐策略。
- 引入 MySQL/Redis，构建订单-库存-商品的事务/补偿或事件链路。
- 增加监控与追踪（Actuator/Prometheus/Zipkin）。
- 前端聚合或 API 文档聚合改进。



