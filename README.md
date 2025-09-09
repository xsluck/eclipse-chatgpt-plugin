# Eclipse AssistAI - Maven版本

[English](#english) | [中文](#中文)

---

## English

### Overview

Eclipse AssistAI is a powerful Eclipse IDE plugin that integrates Large Language Models (LLMs) to enhance your coding experience. This Maven version migrates the original Eclipse plugin project to the Maven build system, providing better dependency management and standardized project structure.

### Key Features

- **🔧 Code Refactoring**: Use LLMs to refactor selected code snippets intelligently
- **📝 Documentation Generation**: Generate comprehensive JavaDoc comments for classes and methods
- **🧪 Test Generation**: Create JUnit tests automatically for selected classes or methods
- **🐛 Error Fixing**: Get AI-powered guidance to fix compilation errors
- **💬 Code Discussion**: Discuss and analyze code with LLMs in real-time
- **📋 Git Commit Messages**: Generate meaningful commit messages based on staged files
- **🔌 MCP Support**: Full Model Context Protocol client implementation
- **🌐 Multi-Model Support**: Support for OpenAI, Anthropic, Google, DeepSeek, and more
- **🎨 Rich UI**: Modern chat interface with syntax highlighting and markdown support
- **📎 File Attachments**: Drag and drop files, images, and code snippets
- **🔄 Real-time Streaming**: Live response streaming from AI models

### Supported LLM Providers

| Provider | Protocol | Example Models | MCP/Tools | Vision | Notes |
|----------|----------|----------------|-----------|--------|-------|
| OpenAI | OpenAI API | gpt-4o, gpt-4-turbo, gpt-3.5-turbo | ✅ | ✅ | Default integration |
| Anthropic | Claude API | claude-3-5-sonnet, claude-3-opus | ✅ | ✅ | Native Claude API |
| Google | Gemini API | gemini-2.0-flash, gemini-1.5-pro | ✅ | ✅ | Dedicated integration |
| DeepSeek | DeepSeek API | deepseek-chat | ✅ | ❌ | Specialized integration |
| Groq | OpenAI API | qwen-qwq-32b, llama3-70b-8192 | ✅ | ✅ | OpenAI-compatible API |
| Local/Self-hosted | OpenAI API | Any local model via Ollama, LM Studio | Variable | Variable | OpenAI-compatible endpoints |

### Project Structure

```
eclipse-assistai-maven/
├── pom.xml                                    # Parent POM file
├── eclipse-assistai-main/                     # Main plugin module
│   ├── pom.xml
│   ├── META-INF/MANIFEST.MF                   # OSGi manifest
│   └── src/main/
│       ├── java/                              # Java source code
│       │   └── com/github/gradusnikov/eclipse/assistai/
│       │       ├── Activator.java             # Plugin activator
│       │       ├── chat/                      # Chat system components
│       │       ├── handlers/                  # Command handlers
│       │       ├── mcp/                       # MCP client implementation
│       │       ├── network/                   # Network clients
│       │       ├── preferences/               # Preference pages
│       │       ├── view/                      # UI components
│       │       └── tools/                     # Utility classes
│       └── resources/                         # Resource files
│           ├── css/                           # Stylesheets
│           ├── fonts/                         # Font files
│           ├── icons/                         # Icon resources
│           ├── js/                            # JavaScript files
│           ├── prompts/                       # Prompt templates
│           ├── OSGI-INF/                      # OSGi configuration
│           ├── plugin.xml                     # Plugin configuration
│           └── fragment.e4xmi                 # E4 fragment
├── eclipse-assistai-feature/                  # Feature module
│   ├── pom.xml
│   └── src/main/resources/
│       ├── feature.xml                        # Feature definition
│       └── build.properties                   # Build properties
└── eclipse-assistai-site/                     # Update site module
    ├── pom.xml
    └── src/main/resources/
        ├── site.xml                           # Site configuration
        ├── index.html                         # Site homepage
        └── app.yaml                           # Application config
```

### Build Requirements

- **Java**: 21 or higher
- **Maven**: 3.6 or higher
- **Eclipse Tycho**: Latest version
- **Eclipse IDE**: For RCP and RAP Developers (recommended for development)

### Build Commands

```bash
# Clean and build entire project
mvn clean install

# Build only main plugin
mvn clean install -pl eclipse-assistai-main

# Build update site
mvn clean install -pl eclipse-assistai-site

# Skip tests during build
mvn clean install -DskipTests

# Build with specific profile
mvn clean install -P release
```

### Development Setup

1. **Install Eclipse IDE for RCP and RAP Developers**
2. **Import Maven project into Eclipse**
   ```bash
   File → Import → Existing Maven Projects
   ```
3. **Configure Target Platform**
   - Open `target-platform/eclipse-assistai.target`
   - Set as active target platform
4. **Run Plugin for Testing**
   - Right-click on project → Run As → Eclipse Application

### Installation

#### From Update Site
1. Open Eclipse IDE
2. Go to `Help → Install New Software`
3. Add update site URL: `[Your update site URL]`
4. Select "Eclipse AssistAI" and install

#### From Source
1. Clone the repository
2. Build the project using Maven
3. Install the generated plugin from the update site

### Configuration

1. **Open Preferences**: `Window → Preferences → AssistAI`
2. **Configure API Keys**: Add your LLM provider API keys
3. **Set Model Preferences**: Choose your preferred models
4. **Configure MCP Servers**: Add custom MCP server configurations
5. **Customize Prompts**: Modify default prompts for different operations

### Usage

#### Code Refactoring
1. Select code in editor
2. Right-click → AssistAI → Refactor Code
3. Review and apply suggestions

#### Documentation Generation
1. Place cursor on class/method
2. Right-click → AssistAI → Generate Documentation
3. Review generated JavaDoc

#### Chat Interface
1. Open AssistAI view: `Window → Show View → Other → AssistAI → Chat`
2. Type your questions or drag files into the chat
3. Get real-time AI responses

### Architecture Overview

The plugin follows a modular architecture with clear separation of concerns:

- **Core Layer**: Plugin activation, dependency injection, and core services
- **Network Layer**: HTTP clients for different LLM providers
- **MCP Layer**: Model Context Protocol implementation for tool calling
- **UI Layer**: Eclipse views, editors, and preference pages
- **Handler Layer**: Command handlers for various AI operations

### Migration from Original Plugin

This Maven version includes several improvements over the original Eclipse plugin:

1. **Build System**: Migrated from Eclipse PDE to Maven + Tycho
2. **Dependency Management**: Maven-based dependency resolution
3. **Modularization**: Split into logical Maven modules
4. **Standardization**: Follows Maven standard directory layout
5. **CI/CD Ready**: Easy integration with continuous integration systems

### Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch
3. Make your changes with proper tests
4. Submit a pull request

### License

This project is licensed under the same license as the original project.

### Support

- **Issues**: Report bugs and feature requests on GitHub
- **Documentation**: Check the wiki for detailed documentation
- **Community**: Join our discussions for help and feedback

---

## 中文

### 概述

Eclipse AssistAI 是一个强大的 Eclipse IDE 插件，集成了大型语言模型 (LLM) 来增强您的编码体验。这个 Maven 版本将原始的 Eclipse 插件项目迁移到 Maven 构建系统，提供更好的依赖管理和标准化的项目结构。

### 主要功能

- **🔧 代码重构**: 使用 LLM 智能重构选定的代码片段
- **📝 文档生成**: 为类和方法生成全面的 JavaDoc 注释
- **🧪 测试生成**: 自动为选定的类或方法创建 JUnit 测试
- **🐛 错误修复**: 获得 AI 驱动的指导来修复编译错误
- **💬 代码讨论**: 与 LLM 实时讨论和分析代码
- **📋 Git 提交信息**: 基于暂存文件生成有意义的提交信息
- **🔌 MCP 支持**: 完整的模型上下文协议客户端实现
- **🌐 多模型支持**: 支持 OpenAI、Anthropic、Google、DeepSeek 等
- **🎨 丰富的 UI**: 具有语法高亮和 Markdown 支持的现代聊天界面
- **📎 文件附件**: 拖放文件、图像和代码片段
- **🔄 实时流式传输**: 来自 AI 模型的实时响应流

### 支持的 LLM 提供商

| 提供商 | 协议 | 示例模型 | MCP/工具 | 视觉 | 备注 |
|--------|------|----------|----------|------|------|
| OpenAI | OpenAI API | gpt-4o, gpt-4-turbo, gpt-3.5-turbo | ✅ | ✅ | 默认集成 |
| Anthropic | Claude API | claude-3-5-sonnet, claude-3-opus | ✅ | ✅ | 原生 Claude API |
| Google | Gemini API | gemini-2.0-flash, gemini-1.5-pro | ✅ | ✅ | 专用集成 |
| DeepSeek | DeepSeek API | deepseek-chat | ✅ | ❌ | 专门集成 |
| Groq | OpenAI API | qwen-qwq-32b, llama3-70b-8192 | ✅ | ✅ | OpenAI 兼容 API |
| 本地/自托管 | OpenAI API | 通过 Ollama、LM Studio 的任何本地模型 | 可变 | 可变 | OpenAI 兼容端点 |

### 项目结构

```
eclipse-assistai-maven/
├── pom.xml                                    # 父级 POM 文件
├── eclipse-assistai-main/                     # 主插件模块
│   ├── pom.xml
│   ├── META-INF/MANIFEST.MF                   # OSGi 清单
│   └── src/main/
│       ├── java/                              # Java 源代码
│       │   └── com/github/gradusnikov/eclipse/assistai/
│       │       ├── Activator.java             # 插件激活器
│       │       ├── chat/                      # 聊天系统组件
│       │       ├── handlers/                  # 命令处理器
│       │       ├── mcp/                       # MCP 客户端实现
│       │       ├── network/                   # 网络客户端
│       │       ├── preferences/               # 首选项页面
│       │       ├── view/                      # UI 组件
│       │       └── tools/                     # 实用工具类
│       └── resources/                         # 资源文件
│           ├── css/                           # 样式表
│           ├── fonts/                         # 字体文件
│           ├── icons/                         # 图标资源
│           ├── js/                            # JavaScript 文件
│           ├── prompts/                       # 提示模板
│           ├── OSGI-INF/                      # OSGi 配置
│           ├── plugin.xml                     # 插件配置
│           └── fragment.e4xmi                 # E4 片段
├── eclipse-assistai-feature/                  # 特性模块
│   ├── pom.xml
│   └── src/main/resources/
│       ├── feature.xml                        # 特性定义
│       └── build.properties                   # 构建属性
└── eclipse-assistai-site/                     # 更新站点模块
    ├── pom.xml
    └── src/main/resources/
        ├── site.xml                           # 站点配置
        ├── index.html                         # 站点主页
        └── app.yaml                           # 应用配置
```

### 构建要求

- **Java**: 21 或更高版本
- **Maven**: 3.6 或更高版本
- **Eclipse Tycho**: 最新版本
- **Eclipse IDE**: RCP 和 RAP 开发者版本（推荐用于开发）

### 构建命令

```bash
# 清理并构建整个项目
mvn clean install

# 仅构建主插件
mvn clean install -pl eclipse-assistai-main

# 构建更新站点
mvn clean install -pl eclipse-assistai-site

# 构建时跳过测试
mvn clean install -DskipTests

# 使用特定配置文件构建
mvn clean install -P release
```

### 开发环境设置

1. **安装 Eclipse IDE for RCP and RAP Developers**
2. **将 Maven 项目导入 Eclipse**
   ```bash
   文件 → 导入 → 现有 Maven 项目
   ```
3. **配置目标平台**
   - 打开 `target-platform/eclipse-assistai.target`
   - 设置为活动目标平台
4. **运行插件进行测试**
   - 右键点击项目 → 运行为 → Eclipse 应用程序

### 安装

#### 从更新站点安装
1. 打开 Eclipse IDE
2. 转到 `帮助 → 安装新软件`
3. 添加更新站点 URL: `[您的更新站点 URL]`
4. 选择 "Eclipse AssistAI" 并安装

#### 从源码安装
1. 克隆仓库
2. 使用 Maven 构建项目
3. 从更新站点安装生成的插件

### 配置

1. **打开首选项**: `窗口 → 首选项 → AssistAI`
2. **配置 API 密钥**: 添加您的 LLM 提供商 API 密钥
3. **设置模型首选项**: 选择您偏好的模型
4. **配置 MCP 服务器**: 添加自定义 MCP 服务器配置
5. **自定义提示**: 修改不同操作的默认提示

### 使用方法

#### 代码重构
1. 在编辑器中选择代码
2. 右键点击 → AssistAI → 重构代码
3. 查看并应用建议

#### 文档生成
1. 将光标放在类/方法上
2. 右键点击 → AssistAI → 生成文档
3. 查看生成的 JavaDoc

#### 聊天界面
1. 打开 AssistAI 视图: `窗口 → 显示视图 → 其他 → AssistAI → 聊天`
2. 输入您的问题或将文件拖入聊天
3. 获得实时 AI 响应

### 架构概述

插件采用模块化架构，具有清晰的关注点分离：

- **核心层**: 插件激活、依赖注入和核心服务
- **网络层**: 不同 LLM 提供商的 HTTP 客户端
- **MCP 层**: 用于工具调用的模型上下文协议实现
- **UI 层**: Eclipse 视图、编辑器和首选项页面
- **处理器层**: 各种 AI 操作的命令处理器

### 从原始插件迁移

这个 Maven 版本相比原始 Eclipse 插件包含了几项改进：

1. **构建系统**: 从 Eclipse PDE 迁移到 Maven + Tycho
2. **依赖管理**: 基于 Maven 的依赖解析
3. **模块化**: 拆分为逻辑 Maven 模块
4. **标准化**: 遵循 Maven 标准目录布局
5. **CI/CD 就绪**: 易于与持续集成系统集成

### 贡献

我们欢迎贡献！请遵循以下步骤：

1. Fork 仓库
2. 创建功能分支
3. 进行更改并添加适当的测试
4. 提交拉取请求

### 许可证

本项目采用与原始项目相同的许可证。

### 支持

- **问题**: 在 GitHub 上报告错误和功能请求
- **文档**: 查看 wiki 获取详细文档
- **社区**: 加入我们的讨论获取帮助和反馈

