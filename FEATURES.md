# 功能特性说明

## 🆕 本次新增/优化
- 订单服务：雪花订单号、幂等键（创建/回调）、回调幂等表、乐观锁版本控制；支付/取消幂等与冲突检测。
- 订单列表：按当前用户分页，统一 `PageParam` 入参、`PageResult` 出参。
- 公共认证上下文：新增模块 `nova-mall-common-web` 提供 `AuthContext`，订单/购物车从请求头 `X-User-Id` 获取用户ID并校验。
- 购物车：接口改为从认证上下文取用户ID，无需显式传参。
- 集成测试：订单并发支付/回调幂等用例覆盖（H2 内存库）。

## ✅ 已实现的功能

本项目已成功集成以下企业级功能：

### 1. Knife4j API 文档 ✅

**技术**: Knife4j 4.5.0 + OpenAPI 3.0

**访问地址**: http://localhost:8080/doc.html

**功能**:
- 📖 美观的 API 文档界面
- 🧪 在线测试 API 接口
- 📝 接口参数说明和示例
- 🌐 中文界面支持
- 📊 请求/响应模型展示

**配置位置**:
- 配置类: `demo-web/src/main/java/com/example/web/config/Knife4jConfig.java`
- YAML配置: `demo-web/src/main/resources/application.yaml`

**使用示例**:
```java
@Tag(name = "用户管理", description = "用户相关的增删改查接口")
@RestController
public class UserController {
    
    @Operation(summary = "获取所有用户", description = "获取系统中所有用户的列表信息")
    @GetMapping("/list")
    public Result<List<UserDTO>> getAllUsers() {
        // ...
    }
}
```

---

### 2. 参数验证（@Valid） ✅

**技术**: Jakarta Validation + Spring Validation

**功能**:
- ✅ 自动参数验证
- ✅ 友好的错误提示
- ✅ 支持多种验证注解
- ✅ 自定义验证规则

**验证注解**:
```java
@Data
public class UserDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    private String username;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 150, message = "年龄必须小于150")
    private Integer age;
}
```

**Controller 使用**:
```java
@PostMapping
public Result<UserDTO> createUser(@Valid @RequestBody UserDTO user) {
    // 参数会自动验证，验证失败会被全局异常处理器捕获
}
```

**验证失败响应**:
```json
{
  "code": 400,
  "message": "参数验证失败: 用户名不能为空; 邮箱格式不正确",
  "data": null
}
```

---

### 3. 全局异常处理（@ControllerAdvice） ✅

**技术**: Spring MVC ExceptionHandler

**功能**:
- ✅ 统一异常处理
- ✅ 友好的错误响应
- ✅ 异常日志记录
- ✅ 多种异常类型支持

**处理的异常类型**:
| 异常 | HTTP状态码 | 说明 |
|------|-----------|------|
| BusinessException | 200 | 业务异常 |
| MethodArgumentNotValidException | 400 | 参数验证失败 |
| BindException | 400 | 参数绑定失败 |
| IllegalArgumentException | 400 | 非法参数 |
| NullPointerException | 500 | 空指针异常 |
| Exception | 500 | 其他未知异常 |

**实现位置**:
`demo-web/src/main/java/com/example/web/exception/GlobalExceptionHandler.java`

**示例**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(400, "参数验证失败: " + message);
    }
}
```

---

### 4. MyBatis Plus + MySQL ✅

**技术**: MyBatis Plus 3.5.9 + MySQL 8.0

**功能**:
- ✅ 数据持久化
- ✅ 自动 CRUD 操作
- ✅ 逻辑删除支持
- ✅ SQL 日志打印
- ✅ 下划线转驼峰命名
- ✅ 自动填充时间字段

**MyBatis Plus 特性**:

1. **BaseMapper**: 自动提供基础 CRUD
   ```java
   @Mapper
   public interface UserMapper extends BaseMapper<User> {
       // 自动拥有：insert, update, delete, selectById, selectList 等方法
   }
   ```

2. **实体注解**:
   ```java
   @TableName("user")
   public class User {
       @TableId(type = IdType.AUTO)
       private Long id;
       
       private String username;
       private String email;
       private Integer age;
   }
   ```

3. **逻辑删除**:
   - 配置 `logic-delete-field: deleted`
   - 删除操作自动变为 `UPDATE user SET deleted=1`
   - 查询自动添加 `WHERE deleted=0`

**数据库配置**:
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo?...
    username: root
    password: root
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
```

**初始化数据库**:
```bash
mysql -u root -p < database/init.sql
```

---

## 🎯 完整功能演示

### 场景1：正常创建用户

**请求**:
```bash
curl -X POST http://localhost:8080/user \
  -H "Content-Type: application/json" \
  -d '{
    "username": "测试用户",
    "email": "test@example.com",
    "age": 25
  }'
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 4,
    "username": "测试用户",
    "email": "test@example.com",
    "age": 25
  }
}
```

---

### 场景2：参数验证失败

**请求**:
```bash
curl -X POST http://localhost:8080/user \
  -H "Content-Type: application/json" \
  -d '{
    "username": "A",
    "email": "invalid-email",
    "age": 200
  }'
```

**响应** (自动验证):
```json
{
  "code": 400,
  "message": "参数验证失败: 用户名长度必须在2-20个字符之间; 邮箱格式不正确; 年龄必须小于150",
  "data": null
}
```

---

### 场景3：查询不存在的用户

**请求**:
```bash
curl http://localhost:8080/user/999
```

**响应**:
```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
```

---

### 场景4：业务异常

假设在 Service 中抛出业务异常：
```java
if (user.getAge() < 18) {
    throw new BusinessException(400, "用户年龄必须大于18岁");
}
```

**响应**:
```json
{
  "code": 400,
  "message": "用户年龄必须大于18岁",
  "data": null
}
```

---

## 📖 使用 Knife4j 测试

### 1. 启动应用

```bash
# 先初始化数据库
mysql -u root -p < database/init.sql

# 启动应用
java -jar demo-web/target/demo-web-0.0.1-SNAPSHOT.jar
```

### 2. 访问文档

浏览器打开: http://localhost:8080/doc.html

### 3. 在线测试

1. 选择"用户管理"分组
2. 点击"创建用户"接口
3. 点击"调试"按钮
4. 输入参数（会自动显示验证规则）:
   ```json
   {
     "username": "新用户",
     "email": "newuser@example.com",
     "age": 25
   }
   ```
5. 点击"发送"
6. 查看响应结果

### 4. 测试参数验证

故意输入错误参数:
```json
{
  "username": "A",
  "email": "invalid",
  "age": 200
}
```

会看到详细的验证错误信息。

---

## 🔍 技术实现细节

### 模块职责

| 模块 | 功能 | 技术 |
|------|------|------|
| demo-common | 工具类、常量、异常 | 纯 Java |
| demo-api | DTO、接口定义 | Validation API + Swagger |
| demo-service | 业务实现、数据访问 | MyBatis Plus |
| demo-web | Controller、全局异常处理 | Spring MVC + Knife4j |

### 数据流转

```
HTTP请求
   ↓
Controller (@Valid验证)
   ↓
Service接口
   ↓
ServiceImpl (调用 Mapper)
   ↓
MyBatis Plus (操作数据库)
   ↓
Entity ↔ DTO 转换
   ↓
Result封装
   ↓
HTTP响应 (异常被GlobalExceptionHandler处理)
```

---

## 📚 相关文档

- **数据库配置**: 查看 `DATABASE.md`
- **项目架构**: 查看 `ARCHITECTURE.md`
- **技术栈**: 查看 `TECH_STACK.md`
- **使用说明**: 查看 `README.md`

---

## 🎉 总结

本项目现已具备：

✅ **分层模块化架构** - 清晰的代码组织  
✅ **Knife4j API 文档** - 美观的接口文档  
✅ **参数自动验证** - 保证数据合法性  
✅ **全局异常处理** - 统一的错误响应  
✅ **MyBatis Plus** - 强大的持久化框架  
✅ **MySQL 数据库** - 可靠的数据存储  

所有功能已完整集成并测试通过！🎊





