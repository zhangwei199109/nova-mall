# 快速开始指南（nova-mall）

## 前置条件
- JDK 21+
- Maven 3.9+
- 默认使用 H2 内存库，无需额外数据库；如需 MySQL，见文末。

## 五步启动
1) **编译**
```bash
./mvnw clean package -DskipTests
```
2) **可选：repackage 可执行 jar（仅 Web/网关）**
```bash
./mvnw -DskipTests -pl nova-mall-gateway,nova-mall-user/nova-mall-user-web,nova-mall-order/nova-mall-order-web,nova-mall-product/nova-mall-product-web,nova-mall-cart/nova-mall-cart-web,nova-mall-stock/nova-mall-stock-web spring-boot:repackage
```
3) **一键启动全部**
```bash
./run-all.sh start
```
   - 网关：8092
   - 用户：8083，订单：8084，商品：8085，购物车：8086，库存：8087
   - 日志：`logs/`，PID：`.run-pids/`
4) **验证**
```bash
# 登录获取 token（默认账户：admin/admin123 或 user/user123）
curl -X POST http://localhost:8092/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# 携带 token 访问
curl http://localhost:8092/user/list -H "Authorization: Bearer <token>"
# 文档（经网关）
# 用户 http://localhost:8092/doc.html
# 订单 http://localhost:8092/order/doc.html
# 商品 http://localhost:8092/product/doc.html
# 购物车 http://localhost:8092/cart/doc.html
# 库存 http://localhost:8092/stock/doc.html
```
5) **停止**
```bash
./run-all.sh stop
```

## 手工启动某个服务
```bash
java -jar nova-mall-user/nova-mall-user-web/target/nova-mall-user-web-0.0.1-SNAPSHOT.jar
java -jar nova-mall-gateway/target/nova-mall-gateway-0.0.1-SNAPSHOT.jar
```

## 切换到 MySQL（可选）
1) 修改各 Web 模块 `src/main/resources/application.yaml`，将 H2 连接改为 MySQL。  
2) 执行对应初始化脚本（示例：用户服务）：  
```bash
mysql -u root -p < nova-mall-user/src/main/resources/db/init.sql
```
3) 重启服务。

## 常见问题
- **端口被占用**：修改对应 `application.yaml` 的 `server.port`，同时更新网关路由。
- **文档无法访问**：确认网关 8092 与对应服务端口均已启动。
- **H2 控制台**：直连服务端口，如 `http://localhost:8083/h2-console`。





