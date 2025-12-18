# Demo 分层模块化工程

## 项目概述

这是一个基于 Spring Boot 4.0.0 的**分层模块化架构**示例项目，采用经典的四层架构设计。

## 技术栈

- **Java**: 21
- **Spring Boot**: 4.0.0
- **构建工具**: Maven 3.9+
- **开发工具**: Lombok

## 项目结构

```
demo-parent/                    # 父工程
├── demo-common/               # 公共模块（纯 Java）
│   ├── constant/              # 常量定义
│   ├── exception/             # 异常类
│   └── util/                  # 工具类
├── demo-api/                  # API 接口定义模块
│   ├── dto/                   # 数据传输对象
│   ├── vo/                    # 视图对象
│   └── service/               # Service 接口定义
├── demo-service/              # 业务服务模块
│   ├── entity/                # 实体类（领域模型）
│   └── impl/                  # Service 接口实现
└── demo-web/                  # Web 层模块（可执行）
    ├── controller/            # 控制器
    └── DemoWebApplication     # 启动类
```

## 模块说明

### 1. demo-common（公共模块）

**职责**：
- 提供公共工具类
- 定义公共常量
- 定义公共异常

**依赖**：
- 无业务依赖（纯 Java 工具）
- Lombok

**主要类**：
- `StringUtil` - 字符串工具类
- `CommonConstant` - 公共常量
- `BusinessException` - 业务异常

### 2. demo-api（API 接口定义模块）

**职责**：
- 定义 Service 接口
- 定义数据传输对象（DTO）
- 定义视图对象（VO）
- 定义统一响应格式

**依赖**：
- `demo-common`
- Lombok
- Jackson Annotations

**主要类**：
- `Result<T>` - 统一响应封装
- `UserDTO` - 用户数据传输对象
- `UserService` - 用户服务接口

**特点**：
- 只定义接口，不包含实现
- 可被其他模块依赖和实现
- 面向接口编程

### 3. demo-service（业务服务模块）

**职责**：
- 实现 API 模块中定义的接口
- 包含业务逻辑
- 定义领域模型（Entity）

**依赖**：
- `demo-api`（传递依赖 `demo-common`）
- Spring Boot Starter
- Lombok

**主要类**：
- `User` - 用户实体（领域模型）
- `UserServiceImpl` - 用户服务实现
  - 内存存储（ConcurrentHashMap）
  - 完整的 CRUD 操作
  - Entity 和 DTO 转换

**特点**：
- 不依赖 Web 层
- 可独立测试
- 业务逻辑封装

### 4. demo-web（Web 层模块）

**职责**：
- HTTP 请求处理
- Controller 层实现
- 应用启动入口

**依赖**：
- `demo-service`（传递依赖 `demo-api` 和 `demo-common`）
- Spring Boot Web
- Lombok

**端口**: 8080

**主要类**：
- `DemoWebApplication` - 启动类
- `UserController` - 用户控制器

**API 接口**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/list` | 获取所有用户 |
| GET | `/user/{id}` | 获取用户详情 |
| POST | `/user` | 创建用户 |
| PUT | `/user/{id}` | 更新用户 |
| DELETE | `/user/{id}` | 删除用户 |
| GET | `/user/health` | 健康检查 |

## 模块依赖关系

```
demo-common（无依赖）
    ↑
demo-api（依赖 common）
    ↑
demo-service（依赖 api）
    ↑
demo-web（依赖 service，可执行）
```

**依赖传递**：
- `demo-web` 可以访问所有下层模块的类
- `demo-service` 可以访问 `demo-api` 和 `demo-common`
- `demo-api` 可以访问 `demo-common`
- `demo-common` 无依赖

## 快速开始

### 编译打包

```bash
# 在项目根目录执行
./mvnw clean package -DskipTests
```

### 启动应用

```bash
# 方式一：使用 java -jar
cd demo-web
java -jar target/demo-web-0.0.1-SNAPSHOT.jar

# 方式二：使用 Maven 插件
cd demo-web
../mvnw spring-boot:run
```

### 访问测试

```bash
# 健康检查
curl http://localhost:8080/user/health

# 获取用户列表
curl http://localhost:8080/user/list

# 获取单个用户
curl http://localhost:8080/user/1

# 创建用户
curl -X POST http://localhost:8080/user \
  -H "Content-Type: application/json" \
  -d '{"username":"新用户","email":"new@example.com","age":25}'

# 更新用户
curl -X PUT http://localhost:8080/user/1 \
  -H "Content-Type: application/json" \
  -d '{"username":"更新用户","email":"update@example.com","age":30}'

# 删除用户
curl -X DELETE http://localhost:8080/user/1
```

## 架构优势

### 1. 职责清晰
- 每个模块职责单一明确
- 符合单一职责原则（SRP）
- 易于理解和维护

### 2. 松耦合
- 面向接口编程
- 模块间依赖清晰
- 易于替换实现

### 3. 可测试性
- 各层可独立测试
- Service 层无需启动 Web 容器
- 便于单元测试和集成测试

### 4. 可重用性
- API 模块可被多个项目共享
- Common 模块可跨项目使用
- Service 层可独立部署为微服务

### 5. 易维护
- 修改某层不影响其他层
- 代码组织结构清晰
- 便于团队协作

## 分层说明

### Common 层
- **定位**：最底层，纯工具类
- **无业务逻辑**
- **可被所有层使用**

### API 层
- **定位**：接口定义层
- **只定义契约**，不包含实现
- **DTO/VO** 数据对象
- **Service 接口**定义

### Service 层
- **定位**：业务逻辑层
- **实现 API 层接口**
- **Entity** 领域模型
- **业务规则**处理

### Web 层
- **定位**：表现层/应用层
- **Controller** 请求处理
- **启动类**
- **最终可执行模块**

## 开发规范

### 1. 接口定义
```java
// 在 demo-api 中定义接口
public interface UserService {
    UserDTO getUserById(Long id);
}
```

### 2. 业务实现
```java
// 在 demo-service 中实现
@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserDTO getUserById(Long id) {
        // 业务逻辑
    }
}
```

### 3. Web 调用
```java
// 在 demo-web 中使用
@RestController
public class UserController {
    @Autowired
    private UserService userService;  // 依赖接口
}
```

## 扩展建议

### 添加新功能

1. **在 demo-api 中**：定义新的 DTO 和 Service 接口
2. **在 demo-service 中**：实现 Service 接口和业务逻辑
3. **在 demo-web 中**：添加 Controller 处理 HTTP 请求

### 集成数据库

1. 在 `demo-service` 添加依赖：
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa</artifactId>
   </dependency>
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
   </dependency>
   ```

2. Entity 添加 JPA 注解
3. 创建 Repository 接口
4. Service 层注入 Repository

### 添加缓存

在 `demo-service` 模块添加 Redis：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 添加 API 文档

在 `demo-web` 模块集成 Knife4j/Swagger

## 构建命令

```bash
# 编译所有模块
./mvnw clean package

# 只编译某个模块（包含依赖）
./mvnw clean package -pl demo-web -am

# 安装到本地仓库
./mvnw clean install

# 跳过测试
./mvnw clean package -DskipTests
```

## 运行应用

```bash
# 启动
java -jar demo-web/target/demo-web-0.0.1-SNAPSHOT.jar

# 或使用 Maven
cd demo-web && ../mvnw spring-boot:run
```

## 生成的 JAR 包

```
demo-common/target/demo-common-0.0.1-SNAPSHOT.jar       # 工具包
demo-api/target/demo-api-0.0.1-SNAPSHOT.jar             # API定义包
demo-service/target/demo-service-0.0.1-SNAPSHOT.jar     # 服务实现包
demo-web/target/demo-web-0.0.1-SNAPSHOT.jar             # 可执行应用包
```

只有 `demo-web` 是可执行的 Spring Boot 应用。

## 统一响应格式

所有接口返回统一的 `Result<T>` 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 实际数据
  }
}
```

## 最佳实践

1. **面向接口编程**：依赖接口而非实现
2. **单一职责**：每个模块职责明确
3. **依赖倒置**：上层依赖下层的抽象
4. **DTO 转换**：Entity 和 DTO 明确分离
5. **异常处理**：使用统一的业务异常

## 总结

该项目成功实现了**分层模块化架构**，具备以下特点：

✅ **清晰的分层结构**：Common → API → Service → Web  
✅ **职责明确**：每层职责单一，易于维护  
✅ **松耦合设计**：面向接口编程，易于扩展  
✅ **可独立测试**：各层可单独测试  
✅ **标准化**：统一的响应格式和异常处理  
✅ **易于扩展**：添加新功能只需遵循分层原则  

该架构适合中小型项目，也可作为大型项目的基础架构！
