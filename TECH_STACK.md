# 工程技术栈详解

## 📚 技术栈总览

本项目是一个**企业级分层模块化架构**，集成了以下主流技术：

### 核心技术

| 技术 | 版本 | 用途 | 模块 |
|------|------|------|------|
| Java | 21 | 开发语言 | 全部 |
| Spring Boot | 4.0.0 | 应用框架 | Service, Web |
| Maven | 3.9.11 | 构建工具 | 全部 |
| Lombok | 自动管理 | 代码简化 | 全部 |

### Web 技术

| 技术 | 版本 | 用途 | 模块 |
|------|------|------|------|
| Spring MVC | 4.0.0 | Web框架 | Web |
| Jackson | 自动管理 | JSON处理 | Web, API |
| Tomcat | 内嵌 | Web服务器 | Web |

### 数据库技术

| 技术 | 版本 | 用途 | 模块 |
|------|------|------|------|
| MyBatis Plus | 3.5.9 | ORM框架 | Service |
| MySQL | 8.0.33 | 关系数据库 | Service |
| HikariCP | 自动管理 | 连接池 | Service |

### 文档与验证

| 技术 | 版本 | 用途 | 模块 |
|------|------|------|------|
| Knife4j | 4.5.0 | API文档 | Web |
| Swagger 3 | 2.2.22 | OpenAPI规范 | API, Web |
| Jakarta Validation | 自动管理 | 参数验证 | API, Web |

---

## 🏗️ 架构技术

### 1. 分层架构

**设计模式**: Layered Architecture

```
Web Layer (表现层)          → demo-web
    ↓
Service Layer (业务层)      → demo-service
    ↓
API Layer (接口层)          → demo-api
    ↓
Common Layer (公共层)       → demo-common
```

**技术实现**:
- Maven Multi-Module
- 面向接口编程
- 依赖倒置原则

---

### 2. 模块化设计

**技术**: Maven Multi-Module Project

**模块划分**:
```xml
<modules>
    <module>demo-common</module>   <!-- 无Spring依赖 -->
    <module>demo-api</module>      <!-- 接口定义 -->
    <module>demo-service</module>  <!-- 业务实现 -->
    <module>demo-web</module>      <!-- Web应用 -->
</modules>
```

---

## 🔧 详细技术说明

### demo-common 模块

**技术栈**:
```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
    </dependency>
</dependencies>
```

**技术特点**:
- ✅ 零Spring依赖
- ✅ 纯Java工具类
- ✅ 可跨项目复用

**包含技术**:
- Lombok: 代码简化
- JUnit 5: 单元测试

---

### demo-api 模块

**技术栈**:
```xml
<dependencies>
    <!-- demo-common -->
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>demo-common</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    
    <!-- Jackson Annotations - JSON注解 -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-annotations</artifactId>
    </dependency>
    
    <!-- Validation API - 验证注解 -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
    </dependency>
    
    <!-- Swagger Annotations - 文档注解 -->
    <dependency>
        <groupId>io.swagger.core.v3</groupId>
        <artifactId>swagger-annotations-jakarta</artifactId>
    </dependency>
</dependencies>
```

**技术应用**:

1. **Jakarta Validation**:
   ```java
   @NotBlank(message = "用户名不能为空")
   @Size(min = 2, max = 20)
   private String username;
   ```

2. **Swagger注解**:
   ```java
   @Schema(description = "用户名", example = "张三", required = true)
   private String username;
   ```

3. **Jackson注解**:
   ```java
   @JsonProperty("username")
   private String username;
   ```

---

### demo-service 模块

**技术栈**:
```xml
<dependencies>
    <!-- demo-api -->
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>demo-api</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter - IoC容器 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    
    <!-- MyBatis Plus - ORM框架 -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>
    
    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

**技术应用**:

1. **Spring IoC**:
   ```java
   @Service
   public class UserServiceImpl implements UserService {
       @Autowired
       private UserMapper userMapper;
   }
   ```

2. **MyBatis Plus**:
   ```java
   @Mapper
   public interface UserMapper extends BaseMapper<User> {
       // 自动拥有 CRUD 方法
   }
   
   @TableName("user")
   public class User {
       @TableId(type = IdType.AUTO)
       private Long id;
   }
   ```

3. **HikariCP 连接池**:
   ```yaml
   spring:
     datasource:
       hikari:
         minimum-idle: 5
         maximum-pool-size: 20
   ```

---

### demo-web 模块

**技术栈**:
```xml
<dependencies>
    <!-- demo-service -->
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>demo-service</artifactId>
    </dependency>
    
    <!-- Spring Boot Web - MVC框架 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Knife4j - API文档 -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- Validation - 参数验证 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

**技术应用**:

1. **Spring MVC**:
   ```java
   @RestController
   @RequestMapping("/user")
   public class UserController {
       @GetMapping("/list")
       public Result<List<UserDTO>> getAllUsers() {...}
   }
   ```

2. **参数验证**:
   ```java
   @PostMapping
   public Result<UserDTO> createUser(@Valid @RequestBody UserDTO user) {
       // @Valid 触发自动验证
   }
   ```

3. **全局异常处理**:
   ```java
   @RestControllerAdvice
   public class GlobalExceptionHandler {
       @ExceptionHandler(BusinessException.class)
       public Result<Void> handleBusinessException(BusinessException e) {...}
   }
   ```

4. **Knife4j配置**:
   ```java
   @Configuration
   public class Knife4jConfig {
       @Bean
       public OpenAPI customOpenAPI() {
           return new OpenAPI()
                   .info(new Info()
                           .title("Demo 应用 API 文档")
                           .version("1.0.0"));
       }
   }
   ```

---

## 🎯 设计模式应用

### 1. DTO 模式
- **位置**: demo-api
- **实现**: UserDTO
- **用途**: 数据传输，隐藏内部实现

### 2. 接口模式
- **位置**: demo-api (接口) + demo-service (实现)
- **用途**: 面向接口编程，松耦合

### 3. 统一响应模式
- **位置**: demo-api
- **实现**: Result<T>
- **用途**: 标准化API响应

### 4. 全局异常处理模式
- **位置**: demo-web
- **实现**: @RestControllerAdvice
- **用途**: 统一异常处理

### 5. Mapper 模式
- **位置**: demo-service
- **实现**: UserMapper extends BaseMapper
- **用途**: 数据访问抽象

---

## 📊 技术选型理由

### 为什么用 MyBatis Plus？

✅ **强大的CRUD**: BaseMapper 提供单表操作  
✅ **代码生成**: 自动生成 Entity、Mapper、Service  
✅ **逻辑删除**: 开箱即用  
✅ **分页插件**: 简化分页查询  
✅ **性能优化**: 批量操作、性能分析  

### 为什么用 Knife4j？

✅ **美观界面**: 比原生 Swagger UI 更好看  
✅ **中文支持**: 完美支持中文  
✅ **增强功能**: 离线文档、参数搜索  
✅ **Spring Boot 集成**: 零配置启动  

### 为什么分层设计？

✅ **职责清晰**: 每层职责单一  
✅ **易于测试**: Service 层可独立测试  
✅ **易于维护**: 修改某层不影响其他层  
✅ **可重用**: API 层可作为 SDK  

---

## 🚀 性能特性

### 1. 数据库连接池（HikariCP）
- 最快的连接池实现
- Spring Boot 默认选择
- 配置优化的连接参数

### 2. MyBatis Plus 优化
- 自动SQL优化
- 批量操作支持
- 懒加载配置

### 3. 日志框架（Logback）
- 异步日志输出
- 按日期滚动
- 压缩归档

---

## 🔐 安全特性

### 当前实现
- ✅ 参数验证（防止恶意输入）
- ✅ SQL预编译（MyBatis Plus 自动，防止SQL注入）
- ✅ 异常信息过滤（不暴露敏感信息）

### 可扩展
- Spring Security（认证授权）
- JWT Token（无状态认证）
- HTTPS 配置

---

## 📈 可扩展技术

### 短期（易集成）
- Redis 缓存
- 定时任务
- 文件上传
- Excel 导入导出

### 中期（需设计）
- 分布式事务
- 消息队列
- 全文搜索
- 数据权限

### 长期（架构级）
- 微服务化
- 容器化部署
- 服务网格
- DevOps 流程

---

## 🎯 技术亮点

### 1. 分层模块化
- 4个独立模块
- 清晰的依赖关系
- 符合企业级标准

### 2. 企业级特性
- ✅ API 文档自动生成
- ✅ 参数自动验证
- ✅ 全局异常处理
- ✅ 数据持久化

### 3. 开发效率
- MyBatis Plus 自动 CRUD
- Knife4j 在线测试
- Lombok 减少代码
- 热部署支持

### 4. 代码质量
- 统一响应格式
- 规范的异常处理
- 完善的日志记录
- 清晰的代码结构

---

## 💻 开发技术栈

### IDE 支持
- IntelliJ IDEA（推荐）
- Eclipse
- VS Code

### 必需工具
- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Git

### 推荐工具
- Postman / Apifox（API测试）
- Navicat / DBeaver（数据库管理）
- Redis Desktop Manager（缓存管理）

---

## 📝 配置技术

### 1. YAML 配置
```yaml
# 应用配置
server:
  port: 8080

# 数据源配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo
    username: root
    password: root
    
# MyBatis Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 2. Java 配置
```java
@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(...);
    }
}
```

### 3. 注解配置
```java
@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.service.mapper")
public class DemoWebApplication {
    //...
}
```

---

## 🎓 技术学习路径

### 初级开发者
1. 理解分层架构
2. 掌握 Spring Boot 基础
3. 学习 MyBatis Plus 使用
4. 熟悉 RESTful API 设计

### 中级开发者
1. 深入 Spring 原理
2. 掌握 MyBatis Plus 高级特性
3. 学习性能优化
4. 掌握设计模式

### 高级开发者
1. 架构设计能力
2. 微服务架构
3. 分布式系统
4. 性能调优

---

## 📚 技术文档

### 官方文档
- **Spring Boot**: https://spring.io/projects/spring-boot
- **MyBatis Plus**: https://baomidou.com/
- **Knife4j**: https://doc.xiaominfo.com/
- **Jakarta Validation**: https://jakarta.ee/specifications/bean-validation/

### 项目文档
- `README.md` - 项目使用说明
- `ARCHITECTURE.md` - 架构设计文档
- `FEATURES.md` - 功能特性说明
- `DATABASE.md` - 数据库配置
- `QUICKSTART.md` - 快速开始指南

---

## 🌟 技术优势总结

### 开发效率
- ✅ MyBatis Plus 减少80%的 SQL 代码
- ✅ Lombok 减少60%的样板代码
- ✅ Knife4j 零配置文档生成
- ✅ Spring Boot 自动配置

### 代码质量
- ✅ 分层架构保证可维护性
- ✅ 参数验证保证数据质量
- ✅ 全局异常处理保证稳定性
- ✅ 统一响应格式保证一致性

### 可扩展性
- ✅ 模块化设计易于扩展
- ✅ 面向接口编程易于替换
- ✅ 松耦合架构易于重构
- ✅ 标准化技术易于集成

### 团队协作
- ✅ API 文档自动生成
- ✅ 代码结构清晰
- ✅ 职责划分明确
- ✅ 技术栈主流成熟

---

## 🎯 技术栈完整性

### ✅ 已具备
- 应用框架（Spring Boot）
- Web 框架（Spring MVC）
- 持久化框架（MyBatis Plus）
- 数据库（MySQL）
- API 文档（Knife4j）
- 参数验证（Validation）
- 异常处理（ControllerAdvice）
- 日志框架（Logback）
- 构建工具（Maven）
- 代码简化（Lombok）

### 🔮 可扩展
- 缓存（Redis）
- 安全（Spring Security）
- 消息队列（RabbitMQ/Kafka）
- 定时任务（Quartz）
- 搜索引擎（Elasticsearch）
- 分布式配置（Nacos）
- 分布式追踪（SkyWalking）
- 容器化（Docker）

---

## 💡 总结

本项目使用的技术栈：

**✅ 成熟稳定** - 所有技术都是业界主流  
**✅ 功能完善** - 具备企业级应用必备功能  
**✅ 易于学习** - 技术栈简洁清晰  
**✅ 便于扩展** - 模块化架构易于集成新技术  

**适用场景**:
- 中小型企业应用
- 电商系统
- 后台管理系统
- 微服务单个服务
- 技术学习和实践

这是一个**生产级别**的技术架构，可以直接用于实际项目开发！🚀
