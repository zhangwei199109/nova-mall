# 功能特性说明（nova-mall）

## 总览
- 多域拆分：用户、订单、商品、购物车、库存各自独立 API/Service/Web 模块。
- 统一网关：Spring Cloud Gateway 8092，负责路由、JWT 鉴权、白名单、重试、熔断，可选本地限流。
- 认证与授权：用户服务内置简单账户（admin/user），发放 JWT，网关与各 Web 层基于角色保护接口。
- 远程调用：统一使用 OpenFeign，已移除 Dubbo 依赖。
- 对象映射：全局采用 MapStruct（Service/Web 层均使用 Mapper 代替 BeanUtils），简化 DTO/实体/VO 转换。
- 文档与调试：各服务启用 Springdoc + Knife4j，可通过网关聚合访问。
- 数据访问：MyBatis Plus + H2 内存库（MySQL 兼容），提供 init.sql 便于切换 MySQL。
- 统一返回：`com.example.common.dto.Result` 统一响应格式，全局异常处理兜底。

## API 文档
- 访问（经网关）：  
  - 用户 `http://localhost:8092/doc.html`  
  - 订单 `http://localhost:8092/order/doc.html`  
  - 商品 `http://localhost:8092/product/doc.html`  
  - 购物车 `http://localhost:8092/cart/doc.html`  
  - 库存 `http://localhost:8092/stock/doc.html`
- 技术：Knife4j 4.5.0 + Springdoc OpenAPI 2.3.0
- 配置：各 Web 模块 `application.yaml` + 自动扫描注解（@Operation/@Tag）；全局定义 Bearer 鉴权方案，点击文档左侧 “Authorize” 填入 `Bearer <token>` 后，调用/示例 curl 会自动带头。

## 安全与网关
- JWT：签名密钥配置于 `nova-mall-gateway/src/main/resources/application.yaml` 的 `spring.cloud.gateway.auth.secret`，Token 发放/刷新由用户 Web 提供 `/auth/login`、`/auth/refresh`。
- 角色：`ADMIN`、`USER`，控制器使用 `@PreAuthorize` 做细粒度授权。
- 网关路由：按路径前缀转发到 8083/8084/8085/8086/8087，提供重试、熔断和统一白名单。
- CORS：全局放行（演示环境）。

## 数据与持久化
- 默认 H2 内存库，MySQL 兼容模式；每个域有独立 schema（init.sql 位于各 `*-service` 或根 `src/main/resources/db/`）。
- MyBatis Plus：开启驼峰映射、SQL 日志、逻辑删除（deleted 字段）。
- 端口与库示例：用户 8083 `jdbc:h2:mem:userdb`，订单 8084 `jdbc:h2:mem:orderdb` 等。

## 领域能力速览
- 用户：注册/查询/更新/删除，健康检查；内存用户表 + BCrypt 密码。
- 订单：基础 CRUD（示例数据）。
- 商品：基础 CRUD。
- 购物车：按用户增删改查条目、清空。
- 库存：查询、预占、释放、扣减。

## 异常与验证
- 参数验证：Jakarta Validation + `@Valid`，验证失败统一返回 400，错误消息拼接。
- 全局异常：`BusinessException`、校验异常、运行时异常统一封装为 `Result`。

## 监控与日志
- 网关加响应头 `X-Gateway: nova-mall-gateway` 以便排查。
- SQL 日志在控制台输出（StdOutImpl），便于演示。
- 各服务日志文件位于 `logs/`（配合 `run-all.sh` 启动时）。

## 待扩展示例
- 替换 H2 为 MySQL/Redis 并接入实际账户体系。
- 将内存用户表迁移为数据库/外部 IAM。
- 打通订单-库存-商品联动的事务/事件流程。





