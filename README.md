# nova-mall 分层与微服务示例

面向学习与演示的多模块电商后台示例，基于 Spring Boot 3.3 + Spring Cloud Gateway。每个业务域（用户、订单、商品、购物车、库存）都拆分为 API、Service、Web 三层，网关统一鉴权与路由。

## 模块一览

| 模块 | 作用 | 可运行端口 |
| --- | --- | --- |
| `nova-mall-common` | 公共 DTO、异常、工具 | - |
| `nova-mall-gateway` | API 网关，JWT 校验、路由、限流、熔断 | 8092 |
| `nova-mall-user` (`*-api/-service/-web`) | 用户与认证（内存用户 + JWT 发放） | 8083 |
| `nova-mall-order` (`*-api/-service/-web`) | 订单 CRUD | 8084 |
| `nova-mall-product` (`*-api/-service/-web`) | 商品 CRUD | 8085 |
| `nova-mall-cart` (`*-api/-service/-web`) | 购物车增删改查 | 8086 |
| `nova-mall-stock` (`*-api/-service/-web`) | 库存查询与预占/释放/扣减 | 8087 |

所有 Web/Service 默认使用 H2 内存库（MySQL 兼容模式），MyBatis Plus 管理数据访问。

## 关键特性

- Spring Cloud Gateway：JWT 鉴权、白名单、重试、熔断、（可选）本地限流。
- Spring Security：基于角色的接口保护；默认用户 `admin/admin123`、`user/user123`。
- Springdoc + Knife4j：各服务提供 OpenAPI 文档，可通过网关聚合访问。
- MyBatis Plus + H2：按领域分表，内存数据库即开即用；提供初始化 SQL 便于切换 MySQL。
- 分层模块化：common → domain API → domain service → domain web，接口与实现解耦。
- MapStruct：Service/Web 层统一用 Mapper 完成 DTO/实体/VO 映射，替代 BeanUtils 以获得更好的类型安全与性能。

## 快速开始

### 环境
- JDK 21+（必须使用 JDK21，否则 Spring Boot 3 插件会因 class 版本报错）
- Maven 3.9+

### 一键构建与启动
```bash
# 根目录，必须先设 JDK 21（IDEA 运行 Maven 也需指定 JDK21）
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

mvn clean package -DskipTests

# 后台启动全部（使用 spring-boot:run，日志在 logs/，pid 在 logs/*.pid）
chmod +x scripts/start-all.sh
./scripts/start-all.sh
```

> IDE/Maven Runner 提示：在 IntelliJ IDEA 的 Maven Runner/Project SDK 中同样选择 JDK21，避免使用系统 JDK8 导致构建失败。 

### 手动运行单个服务（jar）
```bash
java -jar nova-mall-user/nova-mall-user-web/target/nova-mall-user-web-0.0.1-SNAPSHOT.jar
java -jar nova-mall-gateway/target/nova-mall-gateway-0.0.1-SNAPSHOT.jar
```
或使用源码启动：
```bash
mvn -pl nova-mall-user/nova-mall-user-web spring-boot:run
mvn -pl nova-mall-gateway spring-boot:run
```

### 验证
1) 登录获取 Token
```bash
curl -X POST http://localhost:8092/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
2) 携带 Token 访问用户列表（经网关）
```bash
curl http://localhost:8092/user/list \
  -H "Authorization: Bearer <accessToken>"
```
3) 浏览文档（经网关）
- 用户服务：`http://localhost:8092/doc.html`
- 订单服务：`http://localhost:8092/order/doc.html`
- 商品服务：`http://localhost:8092/product/doc.html`
- 购物车服务：`http://localhost:8092/cart/doc.html`
- 库存服务：`http://localhost:8092/stock/doc.html`

## 数据库
- 默认：H2 内存库，启动即用，控制台 `/h2-console`（需直连对应端口，例如 `http://localhost:8083/h2-console`）。
- 切换 MySQL：将各 Web 模块的 `application.yaml` 改为 MySQL 连接，并执行对应 `.../resources/db/init.sql` 初始化表数据。

## 目录结构（简版）
```
nova-mall/
├── nova-mall-common                # 公共 DTO/异常/工具
├── nova-mall-gateway               # 网关（8092）
├── nova-mall-user/
│   ├── nova-mall-user-api          # 接口/DTO
│   ├── nova-mall-user-service      # 业务与持久化
│   └── nova-mall-user-web          # Web 层（8083，JWT 发放）
├── nova-mall-order/                # 订单域（8084）
├── nova-mall-product/              # 商品域（8085）
├── nova-mall-cart/                 # 购物车域（8086）
├── nova-mall-stock/                # 库存域（8087）
└── run-all.sh                      # 一键启动/停止
```

## 请求链路（示例）
1. 客户端调用 `/auth/login` 获取 JWT。
2. 随后请求携带 `Authorization: Bearer <token>` 访问网关。
3. 网关验证签名/白名单 → 路由到对应 Web 服务。
4. Web 层执行参数校验、授权校验，调用 Service + MyBatis Plus 访问数据，返回统一 `Result<T>`。

## 常用命令
- 清理并编译：`./mvnw clean package -DskipTests`
- 只跑网关：`java -jar nova-mall-gateway/target/nova-mall-gateway-0.0.1-SNAPSHOT.jar`
- 查看日志：`tail -f logs/nova-mall-gateway-0.0.1-SNAPSHOT.log`
