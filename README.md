# nova-mall 分层多模块项目

基于 Spring Boot 3.3.x / Spring Cloud 2023.0.x，按 api / service / web 分层拆分，默认使用 H2 内存库（MySQL 兼容）。

## 模块总览
- 公共：`nova-mall-common`、`nova-mall-common-web`
- 网关：`nova-mall-gateway`
- 业务域：`nova-mall-order`、`nova-mall-product`、`nova-mall-stock`、`nova-mall-cart`、`nova-mall-user`（均含 api/service/web）
- 运营：`nova-mall-ops`（秒杀活动配置、上线/下线、商品上下架）
- 支付：`nova-mall-pay`（pay-api/service/web，支付、退款回调）
- AI：`nova-mall-ai`（api/service/web）

## 环境与构建
- 要求：JDK 21，Maven 3.9+
- 全量打包：`mvn clean package -DskipTests`
- 单模块打包示例：
  - 订单 Web：`mvn -pl nova-mall-order/nova-mall-order-web -am clean package -DskipTests`
  - AI Web：`mvn -pl nova-mall-ai/nova-mall-ai-web -am clean package -DskipTests`
- 本地运行示例（可替换模块与端口）：`mvn -pl nova-mall-order/nova-mall-order-web spring-boot:run`

## 端口与文档
- 网关：8092（路由与 JWT 鉴权）
- 用户：8083
- 订单：8084
- 商品：8085
- 购物车：8086
- 库存：8087
- 运营：8090（静态页）
- 支付：8095（pay-web）
- 文档：各 Web `doc.html`，OpenAPI `v3/api-docs`；示例：订单 `http://localhost:8084/doc.html`，AI `http://localhost:8086/doc.html`

## 快速开始（本地）
- 拉起依赖：`./run-all.sh` 或逐个 `mvn -pl <module> spring-boot:run`（先启动网关、再启动业务模块更便于联调）
- 登录/鉴权：用户侧 `POST /auth/login` 获取 JWT，前端/网关请求带 `Authorization: Bearer <token>`
- H2 控制台：`/h2-console`（各 Web 端口），JDBC URL 见对应 `application.yaml`
- 切 MySQL：修改各 Web `spring.datasource.*` 并执行对应 `resources/db/init.sql`
- 统一返回：`Result<T>`（code/message/data）

## 秒杀运营（nova-mall-ops）
- 入口：`http://localhost:8090/static/ops-admin/index.html`
- 认证：`POST /auth/login` 获取 JWT，默认账号在 `ops.auth.username/password`（配置见 `nova-mall-ops/src/main/resources/application.yaml`），接口需 `ROLE_OPS_ADMIN`
- 数据模型：表 `ops_seckill_activity`（商品ID、标题、秒杀价、总库存、限购、开始/结束时间、状态 ONLINE/OFFLINE、逻辑删除）
- 核心接口（运营侧）：`GET /admin/activities`，`GET /admin/activities/active`（在线且在时间窗内，下游拉取），`POST /admin/activities`（新建，默认 OFFLINE），`PUT /admin/activities/{id}`，`POST /admin/activities/{id}/online|offline`，`POST /admin/products/{id}/on-shelf|off-shelf`
- 前端操作流：登录→新建/更新活动→上线/下线→商品上/下架；下游建议定时或按事件拉取 `listActive` 缓存，前台下单仍需做时间窗与库存校验
- 下游集成建议：商品/前台在缓存 `active` 列表后，下单时仍做二次校验（时间窗、库存、状态），库存扣减与防超卖需在订单/库存域实现（当前示例未覆盖）

## AI 模块（nova-mall-ai）
- 接口：`POST /ai/qa` 同步问答；`POST /ai/qa/stream` 与 `GET /ai/qa/stream` SSE 流式；`POST /ai/qa/stream-chunk` 行流
- 测试页：`http://localhost:8086/ai-stream-test.html`（页内可改 Base URL）
- 关键配置（`nova-mall-ai/nova-mall-ai-web/src/main/resources/application.yaml`）：`ai.qa.llm-api-key`、`ai.qa.llm-endpoint`、`ai.qa.llm-model`（默认 `qwen-turbo`）、`ai.qa.vector-endpoint`（可空，空则用 FAQ）

## 网关与认证
- 路由前缀：用户 `/auth/**,/user/**`，订单 `/order/**`，商品 `/product/**`，购物车 `/cart/**`，库存 `/stock/**`
- 鉴权：JWT Bearer，非白名单路径需校验签名与过期；示例白名单包含文档与静态资源
- 角色：运营端使用 `ROLE_OPS_ADMIN`（见 `nova-mall-ops` 登录），业务端按各服务默认策略（示例较简化）

## 数据与初始化
- H2 URL 形如 `jdbc:h2:mem:xxx;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- 每个 Web 模块的 `resources/db/init.sql` 可直接用于 MySQL 初始化
- MyBatis Plus：开启驼峰、SQL 日志、逻辑删除字段 `deleted`

## 目录提示
- 前端静态页：`nova-mall-ops/src/main/resources/static/ops-admin/index.html`
- 配置示例：`nova-mall-ops/src/main/resources/application.yaml`（含运营账号与 JWT 秘钥）、`nova-mall-ai/nova-mall-ai-web/src/main/resources/application.yaml`
- 脚本：`run-all.sh`（本地串行启动示例）、`scripts/start-all.sh`（可按需调整端口/配置）

## 生产加固建议
- 收紧 CORS/白名单，妥善管理 JWT 秘钥与 LLM Key
- 网关启用限流与熔断，必要时接入日志与链路追踪
- 将 H2 切换为 MySQL/Redis，并在订单/库存侧补全库存扣减、防重与补偿流程

## 数据与运行
- 默认 H2 内存库，控制台可通过各端口 `/h2-console` 访问；切换 MySQL 时修改各 Web `application.yaml` 与对应 `resources/db/init.sql`
- 统一响应体：`Result<T>`，逻辑删除字段为 `deleted`（MyBatis Plus 配置在各模块 `application.yaml`）

## 支付/退款（nova-mall-pay）
- 接口：`POST /pay` 发起支付（可带 `Idempotency-Key` 幂等）；`POST /pay/{payNo}/callback`；`GET /pay/{payNo}` 查询。
- 退款：`POST /refund` 发起退款；`POST /refund/{refundNo}/callback`；`GET /refund/{refundNo}` 查询。
- 状态：支付/退款都支持 INIT→SUCCESS（简易流程），幂等键 `Idempotency-Key` + 唯一回调键（状态幂等）。

## 提示
- 生产环境请收紧 CORS、管理 JWT/LLM Key，可在网关启用限流与白名单
- 端口可用 `--server.port=` 覆盖；各模块可单独启动，亦可经网关统一入口
