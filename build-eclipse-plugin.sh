#!/bin/bash

# Eclipse AssistAI 插件构建脚本
# 用于解决Tycho构建问题

echo "Eclipse AssistAI 插件构建脚本"
echo "================================"

# 检查环境
echo "检查构建环境..."

# 检查Java版本
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "Java版本: $JAVA_VERSION"

if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "警告: 建议使用Java 21或更高版本"
fi

# 检查Maven
if command -v mvnd &> /dev/null; then
    echo "使用mvnd构建工具"
    MAVEN_CMD="mvnd"
elif command -v mvn &> /dev/null; then
    echo "使用mvn构建工具"
    MAVEN_CMD="mvn"
else
    echo "错误: 未找到Maven构建工具"
    exit 1
fi

echo ""
echo "构建选项:"
echo "1. 清理Maven缓存并重试"
echo "2. 使用Eclipse IDE构建"
echo "3. 创建简化的JAR包"
echo "4. 退出"
echo ""

read -p "请选择构建方式 (1-4): " choice

case $choice in
    1)
        echo "清理Maven缓存..."
        rm -rf ~/.m2/repository/org/eclipse/tycho
        rm -rf ~/.m2/repository/org/bouncycastle
        echo "缓存清理完成，请重试构建"
        ;;
    2)
        echo "Eclipse IDE构建说明:"
        echo "1. 打开Eclipse IDE"
        echo "2. 导入现有项目: File -> Import -> Existing Projects into Workspace"
        echo "3. 选择项目根目录: $(pwd)"
        echo "4. 右键项目 -> Run As -> Maven build"
        echo "5. 输入目标: clean install"
        ;;
    3)
        echo "创建简化的JAR包..."
        echo "这将创建一个包含所有依赖的fat JAR"
        # 这里可以添加创建fat JAR的逻辑
        ;;
    4)
        echo "退出构建脚本"
        exit 0
        ;;
    *)
        echo "无效选择"
        exit 1
        ;;
esac

echo ""
echo "构建脚本执行完成"
