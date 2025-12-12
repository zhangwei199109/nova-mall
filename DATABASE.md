# 数据库配置说明

## 数据库信息

- **数据库名**: demo
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci

## 初始化步骤

### 1. 安装 MySQL

确保您已安装 MySQL 8.0+

```bash
# macOS
brew install mysql

# 启动 MySQL
brew services start mysql
```

### 2. 执行初始化脚本

```bash
# 方式一：使用 mysql 命令
mysql -u root -p < database/init.sql

# 方式二：登录后执行
mysql -u root -p
source database/init.sql;
```

### 3. 修改数据库配置

编辑 `demo-web/src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo?...
    username: root        # 修改为您的用户名
    password: root        # 修改为您的密码
```

## 表结构

### user 表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | bigint | 用户ID | 主键，自增 |
| username | varchar(50) | 用户名 | 非空，唯一 |
| email | varchar(100) | 邮箱 | 非空，唯一 |
| age | int | 年龄 | 可空 |
| deleted | tinyint | 逻辑删除 | 默认0 |
| create_time | datetime | 创建时间 | 自动生成 |
| update_time | datetime | 更新时间 | 自动更新 |

### 测试数据

初始化脚本会插入3条测试数据：
- 张三, zhangsan@example.com, 25岁
- 李四, lisi@example.com, 30岁
- 王五, wangwu@example.com, 28岁

## MyBatis Plus 配置

### 主要特性

1. **自动CRUD**: 继承 BaseMapper 自动获得增删改查方法
2. **逻辑删除**: deleted 字段自动处理
3. **自动填充**: create_time 和 update_time 自动维护
4. **ID生成**: 主键自动递增

### 配置说明

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL日志
  global-config:
    db-config:
      id-type: auto          # ID自增
      logic-delete-field: deleted      # 逻辑删除字段
      logic-delete-value: 1            # 已删除值
      logic-not-delete-value: 0        # 未删除值
```

## 常用操作

### 查询所有
```java
List<User> users = userMapper.selectList(null);
```

### 根据ID查询
```java
User user = userMapper.selectById(1L);
```

### 插入
```java
User user = new User();
user.setUsername("test");
userMapper.insert(user);
```

### 更新
```java
user.setAge(30);
userMapper.updateById(user);
```

### 删除（逻辑删除）
```java
userMapper.deleteById(1L);  // deleted 字段会自动设为 1
```

## 故障排查

### 连接失败

1. 检查 MySQL 是否启动：
```bash
ps aux | grep mysql
```

2. 检查端口是否监听：
```bash
lsof -i :3306
```

3. 测试连接：
```bash
mysql -u root -p -e "SELECT 1"
```

### 权限问题

```sql
-- 创建用户并授权
CREATE USER 'demo'@'localhost' IDENTIFIED BY 'demo123';
GRANT ALL PRIVILEGES ON demo.* TO 'demo'@'localhost';
FLUSH PRIVILEGES;
```

## 性能优化

### 连接池配置

当前使用 HikariCP（Spring Boot 默认）：
- 最小空闲连接：5
- 最大连接数：20
- 连接超时：30秒

### 索引

- username: 唯一索引
- email: 唯一索引

根据查询需求可添加更多索引。

## 注意事项

⚠️ **开发环境配置**：
- 当前配置使用 `root/root`
- 生产环境请使用专用账号
- 建议使用环境变量管理密码

⚠️ **数据库名**：
- 默认：demo
- 可根据需要修改

⚠️ **初次启动**：
- 必须先执行 init.sql 创建数据库和表
- 否则应用启动会报错





