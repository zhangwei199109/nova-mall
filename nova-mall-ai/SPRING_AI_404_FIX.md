## Spring AI 404 排查与修复记录（DashScope 兼容模式）

### 背景
- 使用 Spring AI 1.0.0-M1，模型 `qwen-turbo`，目标通过 DashScope 兼容模式调用 Chat Completions。
- 关闭自动 ChatClient，采用手工配置的 ChatClient。
- JDK 21 构建与运行，可执行 JAR 已通过 `spring-boot-maven-plugin` repackage 生成。

### 问题现象
- 商品文案接口调用返回 404。
- `curl` 调用 DashScope 兼容接口可 200，说明 key 与账号权限正常。

### 排查过程
1. **强制使用手工 ChatClient**  
   - 去掉 `@ConditionalOnMissingBean`、保留 `@Primary`，启动时打印 `Manual ChatClient using baseUrl=... model=...`，确认使用手工 bean。  
   - 启动参数加 `--spring.ai.chat.client.enabled=false`，避免自动 ChatClient 抢占。

2. **打开底层 HTTP 日志**  
   - 在 `ManualChatClientConfig` 给 RestClient/WebClient 加拦截器，打印 `OpenAiApi HTTP ...`。  
   - 启动时开启 `TRACE/DEBUG`（web.client/http/ai/openai/hc.client5.http.wire）。

3. **定位请求 URL**  
   - 日志显示：`OpenAiApi HTTP POST https://dashscope.aliyuncs.com/compatible-mode/v1/v1/chat/completions`。  
   - 发现 base-url 多了一段 `/v1`，而 Spring AI 会自动拼 `/v1/chat/completions`，导致双重 `/v1` → 404。

4. **修正配置并验证**  
   - 将 base-url 改为 `https://dashscope.aliyuncs.com/compatible-mode`（去掉末尾 `/v1`）。  
   - 重打包、重启后日志显示 `POST https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions`，404 消失。

### 正确配置
```yaml
spring:
  ai:
    openai:
      api-key: <your-key>
      base-url: https://dashscope.aliyuncs.com/compatible-mode   # 注意不要带 /v1
      chat:
        options:
          model: qwen-turbo
```

### 调试启动示例（可见请求 URL）
```bash
/Users/xpitdn0502270/Library/Java/JavaVirtualMachines/graalvm-jdk-21.0.7/Contents/Home/bin/java \
  -jar /Users/xpitdn0502270/Downloads/nova-mall/nova-mall-ai/nova-mall-ai-web/target/nova-mall-ai-web-0.0.1-SNAPSHOT.jar \
  --server.port=8086 \
  --spring.ai.chat.client.enabled=false \
  --spring.ai.openai.base-url=https://dashscope.aliyuncs.com/compatible-mode \
  --spring.ai.openai.chat.options.model=qwen-turbo \
  --logging.level.org.springframework.web.client=TRACE \
  --logging.level.org.springframework.http=TRACE \
  --logging.level.org.springframework.ai=DEBUG \
  --logging.level.org.springframework.ai.openai=DEBUG \
  --logging.level.org.apache.hc.client5.http=DEBUG \
  --logging.level.org.apache.hc.client5.http.wire=DEBUG
```
关键日志：  
- `Manual ChatClient using baseUrl=... model=...`  
- `OpenAiApi HTTP POST https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions`

### 核心教训
- DashScope 兼容模式 base-url 不应包含 `/v1`，否则会拼成 `/v1/v1/...` 导致 404。  
- 如需快速确认实际 URL，可在手工 ChatClient 的 RestClient/WebClient 加拦截器或开启 wire 级日志。  
- 关闭自动 ChatClient 时，确保手工 bean 的 RestClient/WebClient Builder 注入完整，避免 NPE/构造器不匹配。

### 建议
- 生产环境关闭 TRACE/DEBUG，仅保留必要 INFO。  
- 配置 `ai.qa.springAiBaseUrl`（自定义属性）便于业务日志打印 baseUrl（非功能必需）。  
- 若后续改回自动 ChatClient，确保 base-url 正确后可移除手工配置与拦截器。




















