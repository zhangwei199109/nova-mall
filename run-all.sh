#!/usr/bin/env bash
# 一键启动/停止所有 Web + 网关服务的脚本
# 用法：
#   ./run-all.sh start   # 构建可执行包并依次后台启动
#   ./run-all.sh stop    # 停止所有已记录的进程
# 说明：
# - 默认使用 Homebrew 的 JDK21，如需自定义请 export JAVA_HOME 后再运行
# - 首次运行需联网下载构建插件；若在离线环境，请提前准备好 Maven 缓存

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

# 可自定义 JAVA_HOME，默认尝试 Homebrew JDK21
JAVA_HOME_DEFAULT="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
if [[ -z "${JAVA_HOME:-}" ]]; then
  export JAVA_HOME="$JAVA_HOME_DEFAULT"
fi
export PATH="$JAVA_HOME/bin:$PATH"

MODULES=(
  "nova-mall-gateway/target/nova-mall-gateway-0.0.1-SNAPSHOT.jar"
  "nova-mall-user/nova-mall-user-web/target/nova-mall-user-web-0.0.1-SNAPSHOT.jar"
  "nova-mall-order/nova-mall-order-web/target/nova-mall-order-web-0.0.1-SNAPSHOT.jar"
  "nova-mall-product/nova-mall-product-web/target/nova-mall-product-web-0.0.1-SNAPSHOT.jar"
  "nova-mall-cart/nova-mall-cart-web/target/nova-mall-cart-web-0.0.1-SNAPSHOT.jar"
  "nova-mall-stock/nova-mall-stock-web/target/nova-mall-stock-web-0.0.1-SNAPSHOT.jar"
)

PIDS_DIR="$ROOT_DIR/.run-pids"
LOG_DIR="$ROOT_DIR/logs"
MAVEN_REPO="$ROOT_DIR/.m2repo"
mkdir -p "$PIDS_DIR" "$LOG_DIR"

build() {
  echo "=== 第一步：编译所有依赖（无 repackage） ==="
  ./mvnw -Dmaven.repo.local="$MAVEN_REPO" -DskipTests package

  echo "=== 第二步：仅对 Web/Gateway 模块执行 repackage ==="
  local pl="nova-mall-gateway,nova-mall-user/nova-mall-user-web,nova-mall-order/nova-mall-order-web,nova-mall-product/nova-mall-product-web,nova-mall-cart/nova-mall-cart-web,nova-mall-stock/nova-mall-stock-web"
  ./mvnw -Dmaven.repo.local="$MAVEN_REPO" -DskipTests -pl "$pl" spring-boot:repackage
}

start_all() {
  # 构建已完成时，可注释或删除上面的 build 调用，直接启动
  # build
  for jar in "${MODULES[@]}"; do
    if [[ ! -f "$jar" ]]; then
      echo "未找到 $jar，跳过"
      continue
    fi
    name="$(basename "$jar" .jar)"
    log="$LOG_DIR/$name.log"
    echo "启动 $name ..."
    nohup java -jar "$jar" > "$log" 2>&1 &
    echo $! > "$PIDS_DIR/$name.pid"
    echo "  PID $(cat "$PIDS_DIR/$name.pid"), 日志: $log"
  done
}

stop_all() {
  if ls "$PIDS_DIR"/*.pid >/dev/null 2>&1; then
    for f in "$PIDS_DIR"/*.pid; do
      pid="$(cat "$f" 2>/dev/null || true)"
      name="$(basename "$f" .pid)"
      if [[ -n "$pid" ]]; then
        echo "停止 $name (PID $pid)"
        kill "$pid" 2>/dev/null || true
      fi
      rm -f "$f"
    done
  else
    echo "无记录的进程可停止"
  fi
}

case "${1:-}" in
  start) start_all ;;
  stop)  stop_all ;;
  *)
    echo "用法: $0 {start|stop}"
    exit 1
    ;;
esac

