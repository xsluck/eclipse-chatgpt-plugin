# Eclipse AssistAI - Maven版本

这是Eclipse AssistAI插件的Maven版本，将原有的Eclipse插件项目迁移到了Maven构建系统。

## 项目结构

```
eclipse-assistai-maven/
├── pom.xml                                    # 父级POM文件
├── eclipse-assistai-dependencies/             # 依赖模块
│   └── pom.xml
├── eclipse-assistai-main/                     # 主插件模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/                              # Java源代码
│       └── resources/                         # 资源文件
│           ├── css/                           # 样式文件
│           ├── fonts/                         # 字体文件
│           ├── icons/                         # 图标文件
│           ├── js/                            # JavaScript文件
│           ├── prompts/                       # 提示模板
│           ├── OSGI-INF/                      # OSGI配置
│           ├── plugin.xml                     # 插件配置
│           └── fragment.e4xmi                 # E4片段
├── eclipse-assistai-feature/                  # 特性模块
│   ├── pom.xml
│   └── src/main/resources/
│       ├── feature.xml                        # 特性定义
│       └── build.properties                   # 构建属性
├── eclipse-assistai-tests/                    # 测试模块
│   ├── pom.xml
│   └── src/main/java/                         # 测试代码
└── eclipse-assistai-site/                     # 更新站点模块
    ├── pom.xml
    └── src/main/resources/
        ├── site.xml                           # 站点配置
        ├── index.html                         # 站点首页
        └── app.yaml                           # 应用配置
```

## 构建说明

### 前置要求

- Java 21+
- Maven 3.6+
- Eclipse Tycho插件

### 构建命令

```bash
# 清理并构建整个项目
mvn clean install

# 只构建主插件
mvn clean install -pl eclipse-assistai-main

# 构建并运行测试
mvn clean install -pl eclipse-assistai-tests

# 构建更新站点
mvn clean install -pl eclipse-assistai-site
```

### 开发环境设置

1. 安装Eclipse IDE for RCP and RAP Developers
2. 导入Maven项目到Eclipse
3. 配置目标平台（Target Platform）
4. 运行插件进行测试

## 主要功能

- **代码重构**：使用LLM重构选中的代码片段
- **文档生成**：为选中的类或方法生成JavaDoc注释
- **测试生成**：为选中的类或方法创建JUnit测试
- **错误修复**：使用LLM指导修复编译错误
- **代码讨论**：与LLM讨论当前打开文件的内容
- **Git提交信息**：基于暂存文件生成Git提交注释
- **MCP支持**：Model Context Protocol客户端实现
- **多模型支持**：支持OpenAI、Anthropic、Google、DeepSeek等多种LLM

## 支持的LLM提供商

| 提供商 | 协议 | 示例模型 | MCP/工具 | 视觉 | 备注 |
|--------|------|----------|----------|------|------|
| OpenAI | OpenAI API | gpt-4o, gpt-4-turbo, gpt-3.5-turbo | ✅ | ✅ | 默认集成 |
| Anthropic | Claude API | claude-3-7-sonnet, claude-3-5-sonnet, claude-3-opus | ✅ | ✅ | 原生Claude API集成 |
| Google | Gemini API | gemini-2.0-flash, gemini-1.5-pro | ✅ | ✅ | 专用集成 |
| DeepSeek | DeepSeek API | deepseek-chat | ✅ | ❌ | 专用集成 |
| Groq | OpenAI API | qwen-qwq-32b, llama3-70b-8192 | ✅ | ✅ | 使用OpenAI兼容API |
| 本地/自托管 | OpenAI API | 通过Ollama、LM Studio等的任何本地模型 | 可变 | 可变 | 使用OpenAI兼容端点配置 |

## 迁移说明

此Maven版本从原始的Eclipse插件项目迁移而来，主要变化包括：

1. **构建系统**：从Eclipse PDE构建迁移到Maven + Tycho
2. **依赖管理**：使用Maven依赖管理替代手动JAR文件管理
3. **模块化**：将项目分解为多个Maven模块
4. **标准化**：遵循Maven标准目录结构

## 许可证

本项目采用与原始项目相同的许可证。

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。
