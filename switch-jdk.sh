#!/usr/bin/env bash
# 切换本机 JDK 版本的小工具。示例：./switch-jdk.sh 21

set -u -o pipefail

ver="${1:-}"
if [[ -z "${ver}" ]]; then
  echo "用法: $0 <版本号，例如 17/21/25>"
  exit 1
fi

try_set() {
  local path="$1"
  if [[ -d "$path" ]]; then
    export JAVA_HOME="$path"
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "已切换 JAVA_HOME=$JAVA_HOME"
    java -version
    exit 0
  fi
}

# 优先使用系统提供的 java_home
if java_home_path="$(/usr/libexec/java_home -v "$ver" 2>/dev/null)"; then
  try_set "$java_home_path"
fi

# 常见 Homebrew/官方安装路径候选
candidates=(
  "/opt/homebrew/opt/openjdk@${ver}/libexec/openjdk"
  "/opt/homebrew/opt/openjdk/libexec/openjdk"
  "/Library/Java/JavaVirtualMachines/temurin-${ver}.jdk/Contents/Home"
  "/Library/Java/JavaVirtualMachines/zulu-${ver}.jdk/Contents/Home"
  "/Library/Java/JavaVirtualMachines/corretto-${ver}.jdk/Contents/Home"
)

for c in "${candidates[@]}"; do
  try_set "$c"
done

# 最后兜底：遍历 JDK 目录模糊匹配版本号
if [[ -d /Library/Java/JavaVirtualMachines ]]; then
  while IFS= read -r dir; do
    try_set "$dir"
  done < <(find /Library/Java/JavaVirtualMachines -maxdepth 1 -type d -name "*${ver}*.jdk" -print 2>/dev/null | sed 's#$#/Contents/Home#')
fi

echo "未找到版本 ${ver} 的 JDK，请确认已安装。可用目录："
ls /Library/Java/JavaVirtualMachines 2>/dev/null || true
exit 1


