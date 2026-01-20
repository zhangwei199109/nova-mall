<div align="center">

# 🛒 Nova Mall

> 基于 Spring Cloud 的微服务电商系统 | 企业级分层架构 | 开箱即用

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.5-blue.svg)](https://spring.io/projects/spring-cloud)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.9-red.svg)](https://baomidou.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

[项目简介](#-项目简介) • [核心特性](#-核心特性) • [快速开始](#-快速开始) • [技术栈](#-技术栈) • [项目结构](#-项目结构) • [文档](#-文档)

</div>

---

## 📖 项目简介

**Nova Mall** 是一个基于 Spring Boot 3.3.x 和 Spring Cloud 2023.0.x 构建的现代化微服务电商系统。采用分层多模块架构设计，遵循领域驱动设计（DDD）理念，提供完整的电商业务功能，包括用户管理、商品管理、订单处理、购物车、库存管理、支付、秒杀运营和 AI 智能问答等核心模块。

### ✨ 项目亮点

- 🏗️ **分层架构**：清晰的 api / service / web 三层分离，职责明确
- 🔐 **统一网关**：Spring Cloud Gateway 统一入口，JWT 鉴权，路由转发
- 📚 **完整文档**：Knife4j API 文档，支持在线测试
- 🚀 **开箱即用**：默认 H2 内存库，无需额外配置即可运行
- 🔄 **幂等保障**：订单、支付支持幂等性控制，防止重复操作
- 🤖 **AI 集成**：内置 AI 智能问答模块，支持流式响应
- ⚡ **秒杀运营**：完整的秒杀活动配置和管理系统
- 🛡️ **企业级特性**：全局异常处理、参数验证、逻辑删除、乐观锁

---

## 🎯 核心特性

### 业务模块

| 模块 | 功能描述 | 端口 |
|------|---------|------|
| **用户服务** | 用户注册、登录、JWT 认证 | 8083 |
| **商品服务** | 商品信息管理、上下架 | 8085 |
| **订单服务** | 订单创建、查询、支付、取消 | 8084 |
| **购物车服务** | 购物车增删改查 | 8086 |
| **库存服务** | 库存管理、扣减 | 8087 |
| **支付服务** | 支付、退款、回调处理 | 8095 |
| **运营服务** | 秒杀活动配置、商品管理 | 8090 |
| **AI 服务** | 智能问答、流式响应 | 8086 |
| **网关服务** | 统一路由、鉴权、限流 | 8092 |

### 技术特性

- ✅ **Knife4j API 文档** - 美观的接口文档，支持在线测试
- ✅ **参数验证** - Jakarta Validation 自动参数校验
- ✅ **全局异常处理** - 统一的异常处理和错误响应
- ✅ **MyBatis Plus** - 强大的 ORM 框架，自动 CRUD
- ✅ **逻辑删除** - 数据安全，支持逻辑删除
- ✅ **乐观锁** - 并发控制，防止数据冲突
- ✅ **幂等性** - 订单、支付支持幂等键控制
- ✅ **JWT 认证** - 无状态认证，支持角色权限
- ✅ **统一响应** - `Result<T>` 统一响应格式

---

## 🚀 快速开始

### 前置要求

- **JDK 21+** 
- **Maven 3.9+**
- **可选**：MySQL 8.0+（默认使用 H2 内存库）

### 一键启动（推荐）

```bash
# 1. 克隆项目
git clone <repository-url>
cd nova-mall

# 2. 编译项目
mvn clean package -DskipTests

# 3. 一键启动所有服务（后台运行）
chmod +x scripts/start-all.sh
./scripts/start-all.sh

# 4. 查看日志
tail -f logs/*.log
```

### 手动启动

```bash
# 启动网关（必须先启动）
mvn -pl nova-mall-gateway spring-boot:run

# 启动业务服务（可并行启动）
mvn -pl nova-mall-user/nova-mall-user-web spring-boot:run
mvn -pl nova-mall-order/nova-mall-order-web spring-boot:run
mvn -pl nova-mall-product/nova-mall-product-web spring-boot:run
mvn -pl nova-mall-cart/nova-mall-cart-web spring-boot:run
mvn -pl nova-mall-stock/nova-mall-stock-web spring-boot:run
```

### 验证服务

```bash
# 1. 登录获取 Token
curl -X POST http://localhost:8092/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. 使用 Token 访问接口
curl http://localhost:8092/user/list \
  -H "Authorization: Bearer <your-token>"

# 3. 访问 API 文档
# 网关文档：http://localhost:8092/doc.html
# 订单文档：http://localhost:8092/order/doc.html
# 商品文档：http://localhost:8092/product/doc.html
```

### 停止服务

```bash
# 停止所有后台服务
for f in logs/*.pid; do 
  [ -f "$f" ] && kill $(cat "$f") 2>/dev/null || true
done
```

---

## 🛠️ 技术栈

### 核心框架

- **Spring Boot 3.3.4** - 应用框架
- **Spring Cloud 2023.0.5** - 微服务框架
- **Spring Cloud Gateway** - API 网关
- **MyBatis Plus 3.5.9** - ORM 框架

### 工具库

- **Knife4j 4.5.0** - API 文档
- **JJWT 0.12.5** - JWT 认证
- **Lombok 1.18.34** - 代码简化
- **Jakarta Validation** - 参数验证

### 数据存储

- **H2 Database** - 内存数据库（默认）
- **MySQL 8.0.33** - 关系型数据库（可选）
- **HikariCP** - 连接池

### 消息队列（可选）

- **RocketMQ 2.3.0** - 消息中间件

---

## 📁 项目结构

```
nova-mall/
├── nova-mall-common/              # 公共模块（DTO、异常、工具）
├── nova-mall-common-web/          # Web 公共模块（认证上下文）
├── nova-mall-gateway/             # API 网关（路由、鉴权）
│
├── nova-mall-user/                # 用户服务
│   ├── nova-mall-user-api/        # 用户 API 契约
│   ├── nova-mall-user-service/    # 用户业务实现
│   └── nova-mall-user-web/        # 用户 Web 接口
│
├── nova-mall-order/               # 订单服务
│   ├── nova-mall-order-api/
│   ├── nova-mall-order-service/
│   └── nova-mall-order-web/
│
├── nova-mall-product/            # 商品服务
├── nova-mall-cart/                # 购物车服务
├── nova-mall-stock/               # 库存服务
├── nova-mall-pay/                 # 支付服务
├── nova-mall-ops/                 # 运营服务（秒杀活动）
├── nova-mall-ai/                  # AI 服务（智能问答）
└── nova-mall-ads/                 # 广告服务
```

### 架构图

```
             ┌────────────────────────┐
             │   nova-mall-gateway    │ 8092
             │   (路由 + JWT 鉴权)     │
             └─────────▲──────────────┘
                       │
        ┌──────────────┴──────────────────────────────┐
        │              │              │               │
   user-web        order-web      product-web     cart-web
   (8083)          (8084)         (8085)         (8086)
        ▲              ▲              ▲               ▲
   user-service   order-service   product-service  cart-service
        ▲              ▲              ▲               ▲
   user-api        order-api      product-api      cart-api
        └─────────────────────────┬──────────────────┘
                         nova-mall-common
```

---

## 📚 文档

- 📖 [快速开始指南](QUICKSTART.md) - 详细的启动和配置说明
- 🏗️ [架构设计](ARCHITECTURE.md) - 系统架构和设计理念
- ✨ [功能特性](FEATURES.md) - 完整的功能列表和实现细节
- 💾 [数据库配置](DATABASE.md) - 数据库配置和初始化
- ❓ [帮助文档](HELP.md) - 常见问题和故障排查

---

## 🔌 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 网关 | 8092 | 统一入口，路由转发 |
| 用户 | 8083 | 用户管理、认证 |
| 订单 | 8084 | 订单处理 |
| 商品 | 8085 | 商品管理 |
| 购物车 | 8086 | 购物车服务 |
| 库存 | 8087 | 库存管理 |
| 运营 | 8090 | 秒杀活动管理 |
| 支付 | 8095 | 支付服务 |
| AI | 8086 | AI 智能问答 |

### API 文档地址

- **网关文档**：http://localhost:8092/doc.html
- **订单文档**：http://localhost:8092/order/doc.html
- **商品文档**：http://localhost:8092/product/doc.html
- **购物车文档**：http://localhost:8092/cart/doc.html
- **库存文档**：http://localhost:8092/stock/doc.html

---

## 🎮 功能演示

### 1. 用户登录

```bash
curl -X POST http://localhost:8092/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 2. 创建订单

```bash
curl -X POST http://localhost:8092/order/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -H "X-User-Id: 1" \
  -d '{
    "productId": 1,
    "quantity": 2,
    "address": "北京市朝阳区"
  }'
```

### 3. AI 智能问答

```bash
# 同步问答
curl -X POST http://localhost:8086/ai/qa \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是微服务？"
  }'

# 流式问答（SSE）
curl -X GET "http://localhost:8086/ai/qa/stream?question=什么是Spring%20Cloud"
```

### 4. 秒杀活动管理

访问运营后台：http://localhost:8090/static/ops-admin/index.html

---

## 🔧 配置说明

### 切换 MySQL

1. 修改各 Web 模块的 `application.yaml`：
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/nova_mall?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
```

2. 执行初始化脚本：
```bash
mysql -u root -p < nova-mall-user/src/main/resources/db/init.sql
```

### H2 控制台

访问地址：`http://localhost:8083/h2-console`

JDBC URL 见各模块的 `application.yaml` 配置。

---

## 🎯 核心功能详解

### 订单服务

- ✅ 雪花算法生成订单号
- ✅ 幂等性控制（创建、支付、取消）
- ✅ 乐观锁版本控制
- ✅ 分页查询（按用户）
- ✅ 支付回调处理

### 支付服务

- ✅ 支付发起（支持幂等键）
- ✅ 支付回调（幂等处理）
- ✅ 退款处理
- ✅ 支付状态查询

### 秒杀运营

- ✅ 活动配置（商品、价格、库存、限购）
- ✅ 活动上线/下线
- ✅ 商品上架/下架
- ✅ 活动列表查询（支持筛选在线活动）

### AI 服务

- ✅ 同步问答接口
- ✅ SSE 流式响应
- ✅ 行流式响应
- ✅ 向量检索（可选）

---

## 🛡️ 安全特性

- **JWT 认证**：无状态认证，支持 Token 刷新
- **角色权限**：支持 `ROLE_OPS_ADMIN` 等角色
- **白名单机制**：文档、静态资源无需认证
- **参数验证**：自动校验请求参数
- **SQL 注入防护**：MyBatis Plus 参数化查询
- **逻辑删除**：数据安全，支持恢复

---

## 📦 构建与打包

### 全量打包

```bash
mvn clean package -DskipTests
```

### 单模块打包

```bash
# 订单服务
mvn -pl nova-mall-order/nova-mall-order-web -am clean package -DskipTests

# AI 服务
mvn -pl nova-mall-ai/nova-mall-ai-web -am clean package -DskipTests
```

### 运行 JAR

```bash
java -jar nova-mall-order/nova-mall-order-web/target/nova-mall-order-web-0.0.1-SNAPSHOT.jar
```

---

## 🚧 生产环境建议

- ⚠️ **数据库**：将 H2 切换为 MySQL，配置主从复制
- ⚠️ **缓存**：引入 Redis 缓存热点数据
- ⚠️ **消息队列**：使用 RocketMQ 处理异步任务
- ⚠️ **监控**：集成 Prometheus + Grafana
- ⚠️ **日志**：使用 ELK 或 Loki 集中日志管理
- ⚠️ **限流熔断**：在网关配置限流和熔断规则
- ⚠️ **安全加固**：妥善管理 JWT 密钥和 API Key
- ⚠️ **CORS 配置**：收紧跨域白名单
- ⚠️ **库存扣减**：完善库存扣减和防超卖机制

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 👥 作者

张威

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [MyBatis Plus](https://baomidou.com/)
- [Knife4j](https://doc.xiaominfo.com/)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐ Star！**

Made with ❤️ by 张威

</div>
