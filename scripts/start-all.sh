#!/usr/bin/env bash
set -euo pipefail

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# 默认 JDK 路径（可按需覆盖）
JAVA_HOME_DEFAULT="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export JAVA_HOME="${JAVA_HOME:-$JAVA_HOME_DEFAULT}"
export PATH="$JAVA_HOME/bin:$PATH"

LOG_DIR="$PROJECT_ROOT/logs"
mkdir -p "$LOG_DIR"

MVN_BIN="${MVN_BIN:-mvn}"

# 需要启动的模块与名称
services=(
  "nova-mall-user/nova-mall-user-web:user"
  "nova-mall-product/nova-mall-product-web:product"
  "nova-mall-stock/nova-mall-stock-web:stock"
  "nova-mall-cart/nova-mall-cart-web:cart"
  "nova-mall-order/nova-mall-order-web:order"
  "nova-mall-gateway:gateway"
)

start_service() {
  local module="$1"
  local name="$2"

  echo "starting $name ($module)..."
  (
    cd "$PROJECT_ROOT"
    nohup "$MVN_BIN" -DskipTests -pl "$module" -am spring-boot:run > "$LOG_DIR/$name.log" 2>&1 &
    echo $! > "$LOG_DIR/$name.pid"
  )
  echo "$name started. log: $LOG_DIR/$name.log pid: $(cat "$LOG_DIR/$name.pid")"
}

for entry in "${services[@]}"; do
  IFS=: read -r module name <<<"$entry"
  start_service "$module" "$name"
done


