# AgentPlatform：可扩展的 AI Agent 平台

基于 **Java 17、Spring Boot 3.5.6 和 MySQL** 构建，围绕 Agent 编排、多模型路由、记忆、Skill 插件、MCP 工具接入和运行追踪展开。

本项目最初作为 AI Agent 平台课程考核项目开发。核心目标不是实现单一聊天机器人或简历优化工具，而是验证一个 Agent 平台如何统一组织模型、记忆、能力、工具和执行记录。

**resume_optimize** 是平台中的复杂业务 Skill，用于验证文件解析、网页读取、模型调用、异步任务和结构化结果返回，不是项目的唯一业务方向。

> 当前版本属于可运行、可演示的后端工程 MVP，不宣称已经达到生产级多租户、高可用或安全沙箱水平。

[观看 Agent 平台 MVP 演示视频](https://www.bilibili.com/video/BV1RE5F6cEy6)

---

## 1. 项目目标

### 1.1 Agent 如何组织多个能力

平台通过 **AgentRuntime** 统一处理用户消息、记忆加载、模型选择、Skill 决策、参数补全、能力调用、回答生成和运行记录。

### 1.2 Agent 能力如何扩展

平台同时支持内置 Skill、外部 Jar Skill、MCP 风格工具、复杂业务 Skill 和受控插件上下文。业务能力不需要全部写入 Agent 核心。

### 1.3 Agent 执行过程如何验证

平台保存 Agent Run 和 Trace，并提供 Workflow、Plan、Decision、Action、Timeline、Debug、Explanation 和 Reflection 等只读视图，使一次执行可以被回看和解释。

---

## 2. 展示重点

| 展示重点 | 当前实现 | 验证入口 |
| --- | --- | --- |
| Agent 编排 | AgentRuntime 负责消息、记忆、模型、Skill 和结果编排 | POST /api/chat |
| 多模型路由 | 通过 modelId 选择模型，并校验能力与启用状态 | GET /api/models |
| 对话记忆 | MySQL 保存短期消息，文件存储保存长期记忆 | GET /api/memory/{conversationId} |
| Skill Runtime | 统一 Skill 元数据、注册、启用、禁用和调用 | GET /api/skills |
| 插件扩展 | 外部 Jar 预览、校验、加载、注册和运行时管理 | POST /api/plugins/skills/upload |
| MCP 工具 | 内部 Tool Registry、JSON-RPC 风格接口和外部 MCP 接入 | GET /api/mcp/tools |
| 执行追踪 | 保存 Agent Run、Trace 和多层只读观察视图 | GET /api/agent/runs |
| 复杂业务 Skill | 简历解析、职位读取、同步/异步优化和结构化结果 | POST /api/resume/optimize |
| 系统自检 | 聚合模型、Skill、插件、MCP、Memory 和 Console 状态 | GET /api/system/health-check |

---

## 3. 总体架构

~~~mermaid
flowchart TB
    USER["用户 / Apifox / Web Console / CLI"] --> API["Spring Boot REST API"]
    API --> RUNTIME["AgentRuntime"]

    RUNTIME --> MEMORY["MemoryService"]
    RUNTIME --> MODEL["ModelService / ModelRouter"]
    RUNTIME --> SKILL["SkillRegistry"]
    RUNTIME --> TRACE["Agent Run / Trace"]

    SKILL --> BUILTIN["Built-in Skills"]
    SKILL --> PLUGIN["Plugin Skills"]
    SKILL --> MCP["MCP-backed Skills"]
    SKILL --> BUSINESS["Business Skills"]

    API --> PLUGINAPI["Plugin API"]
    PLUGINAPI --> LOADER["Validator / URLClassLoader"]
    LOADER --> SKILL

    API --> MCPAPI["MCP REST / JSON-RPC"]
    MCPAPI --> MCPREG["McpToolRegistry"]
    MCPAPI --> EXTERNAL["External MCP Client"]

    API --> RESUME["Resume Optimize Service"]
    RESUME --> MODEL

    MEMORY --> MYSQL[("MySQL")]
    TRACE --> MYSQL
    PLUGINAPI --> MYSQL
    RESUME --> MYSQL
    MEMORY --> FILE["File Long-term Memory"]
~~~

架构边界：

- Controller 负责 REST API 入口。
- Service 负责编排业务流程。
- Repository 负责数据库访问。
- Skill 是 Agent 可调用的能力包装。
- ModelService 和 ModelRouter 负责模型选择与调用。
- MCP Client 负责外部工具连接。
- ResumeOptimizeSkill 保持轻量，具体业务由 ResumeOptimizeService 编排。

---

## 4. Agent 核心执行链路

**POST /api/chat** 是 Agent 的主要入口。

~~~text
接收用户消息
→ 加载短期与长期记忆
→ 根据 modelId 选择模型
→ 判断是否需要调用 Skill
→ 校验并补全 Skill 参数
→ 执行 Built-in Skill / Plugin Skill / MCP Tool
→ 生成回答或直接返回结构化结果
→ 保存消息、Agent Run 和 Trace
~~~

当用户意图明确，但 Skill 必要参数不足时，平台会保存 Pending Skill Call、追问缺失参数，并在下一轮合并参数后继续调用原 Skill。

**resume_optimize** 等报告型 Skill 内部已经调用模型并生成结构化结果。平台会直接返回这类结果，避免二次总结增加延迟或破坏报告结构。

---

## 5. 核心能力

### 5.1 多模型管理

平台通过 **ModelService** 和 **ModelRouter** 管理模型配置与调用，模型元数据包括 id、displayName、provider、type、enabled 和 capabilities。

~~~text
GET  /api/models
GET  /api/models/{modelId}
POST /api/models/test-chat
POST /api/models/vision-chat
POST /api/chat
~~~

Vision Chat 支持图片输入，并根据模型的 vision 或 multimodal capability 判断是否允许调用。真实模型调用需要配置对应 API Key；没有凭据时仍可使用 mock model 和不依赖真实模型的演示能力。

### 5.2 Memory

平台区分短期记忆、长期记忆和 Run History：

- 短期记忆保存在 MySQL 的 conversation_message 表。
- 长期记忆通过 FileLongTermMemoryStore 保存。
- Run History 用于审计和复盘，不等同于下一轮推理使用的 Memory。
- 服务重启后，同一 conversationId 可以恢复已持久化的对话。

~~~text
GET  /api/memory/{conversationId}
GET  /api/memory/conversations/{conversationId}/messages
POST /api/memory/{conversationId}/long-term
~~~

### 5.3 Skill Runtime

Skill 通过统一接口向 Agent 暴露能力：

~~~java
SkillMetadata metadata();

SkillResult execute(SkillContext context);
~~~

Skill Metadata 描述名称、展示名称、说明、版本、参数 Schema 和依赖。

~~~text
GET  /api/skills
GET  /api/skills/market
GET  /api/skills/{skillName}
POST /api/skills/{skillName}/call
POST /api/skills/{skillName}/enable
POST /api/skills/{skillName}/disable
~~~

当前内置或示例能力覆盖计算、天气、文件搜索、简历优化、文本处理和 MCP 调用等方向。

### 5.4 插件 Skill

插件处理流程：

~~~text
上传或预览 Jar
→ 检查 Skill 实现与 Metadata
→ 读取 Skill Manifest
→ 检查权限声明与完整度
→ 通过 URLClassLoader 加载
→ 注册到 SkillRegistry
→ 记录插件和运行时状态
~~~

插件 Skill 当前不支持普通 Spring Bean 注入。需要平台能力的插件可以通过受控 PluginContext 获取允许的能力。

~~~text
POST /api/plugins/skills/preview
POST /api/plugins/skills/upload
GET  /api/plugins
GET  /api/plugins/runtime
POST /api/plugins/{pluginId}/enable
POST /api/plugins/{pluginId}/disable
~~~

### 5.5 MCP 工具接入

平台提供内部 McpToolRegistry、HTTP JSON-RPC 风格 Adapter 和 External MCP HTTP Client。MCP 是比 Skill 更底层的工具连接机制，并不是所有 Skill 都需要使用 MCP。

~~~text
GET  /api/mcp/tools
POST /api/mcp/tools/{toolName}/call
POST /api/mcp/rpc
POST /api/mcp/external/servers
POST /api/mcp/external/servers/{serverId}/sync-tools
POST /api/mcp/external/tools/{toolId}/call
~~~

### 5.6 Agent Run 与 Trace

每次 Agent 执行可以记录输入输出摘要、模型、Skill、结果、错误、Trace 阶段、耗时和状态。

~~~text
INTENT_DETECTION
SELECT_SKILL
CALL_SKILL
GENERATE_ANSWER
~~~

~~~text
GET /api/agent/runs
GET /api/agent/runs/{runId}
GET /api/agent/runs/{runId}/workflow
GET /api/agent/runs/{runId}/decisions
GET /api/agent/runs/{runId}/actions
GET /api/agent/runs/{runId}/timeline
GET /api/agent/runs/{runId}/debug
~~~

这些视图用于解释已有 Run 和 Trace，目前不代表已经实现独立的多 Agent 工作流引擎。

---

## 6. 复杂业务示例：简历优化 Skill

~~~text
上传 PDF / DOCX 简历
→ 解析简历文本
→ 读取职位页面
→ 创建优化任务
→ 构建 Prompt
→ 调用模型
→ 解析结构化结果
→ 保存并返回结果
~~~

~~~text
POST /api/resume/files
POST /api/resume/files/{fileId}/parse
POST /api/resume/job-postings/read
POST /api/resume/optimize
POST /api/resume/optimize/async
GET  /api/resume/tasks/{taskId}
~~~

优化内容必须基于原始简历事实。模型原始响应会尽量保留，以便排查解析问题。

演示时建议优先使用 DOCX。复杂排版、扫描件或特殊字体 PDF 的文本提取稳定性相对较低。

---

## 7. 技术栈

### 后端

- Java 17
- Spring Boot 3.5.6
- Spring MVC
- Spring Data JPA
- Spring AI 1.0.3
- Flyway
- Maven

### 数据与文档处理

- MySQL
- Apache PDFBox
- Apache POI
- Jsoup
- 文件型长期记忆

### 插件与工具

- Java URLClassLoader
- Skill SPI 与 PluginContext
- JSON-RPC 风格 MCP Adapter
- External MCP HTTP Client
- PowerShell CLI
- Static HTML Web Console

---

## 8. 项目结构

~~~text
agentPlatform/
├─ src/main/java/com/sharon/agentplatform/
│  ├─ agent/          Agent 编排、Run 和 Trace
│  ├─ model/          模型配置、路由与调用
│  ├─ memory/         短期与长期记忆
│  ├─ conversation/   会话资源和附件
│  ├─ skill/          Skill 接口、Registry 和内置 Skill
│  ├─ plugin/         插件预览、加载和运行时管理
│  ├─ mcp/            MCP Tool 与外部 MCP 接入
│  ├─ resume/         简历优化业务
│  └─ system/         系统健康检查
├─ src/main/resources/
│  ├─ application.yml
│  ├─ db/migration/   Flyway 数据库迁移
│  └─ static/         Web Console
└─ scripts/           CLI 演示脚本
~~~

---

## 9. 快速启动

### 9.1 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8.x

### 9.2 配置环境变量

~~~powershell
$env:MYSQL_URL = "jdbc:mysql://localhost:3306/agent_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "your-password"
$env:OPENAI_API_KEY = "your-api-key"
~~~

OPENAI_API_KEY 只在调用真实 OpenAI-compatible 模型时需要。不要将 API Key 或数据库密码写入源码或提交到 Git。

### 9.3 启动项目

~~~powershell
git clone https://github.com/C3-Sharon/agentPlatform.git
cd agentPlatform
mvn spring-boot:run
~~~

默认地址：

~~~text
http://localhost:8080
~~~

Web Console：

~~~text
http://localhost:8080/console.html
~~~

Flyway 会在启动时执行数据库迁移。

### 9.4 编译验证

~~~powershell
mvn compile
~~~

---

## 10. 建议演示路线

### 基础链路

1. 打开 Web Console。
2. 访问系统健康检查。
3. 查看模型列表和 Skill 市场。
4. 调用一个简单 Skill。
5. 通过 /api/chat 触发 Skill。
6. 查看对应 Agent Run 和 Trace。

~~~text
GET  /api/system/health-check
GET  /api/models
GET  /api/skills
GET  /api/skills/market
POST /api/skills/{skillName}/call
POST /api/chat
GET  /api/agent/runs
~~~

### 插件链路

1. 准备可信的示例 Skill Jar。
2. 调用插件预览接口。
3. 检查 Metadata、Manifest 和权限提示。
4. 上传插件并在 Skill 市场中确认。
5. 调用插件 Skill。
6. 查看插件运行时状态。

### MCP 链路

1. 查看内部 MCP Tools。
2. 通过 REST 或 JSON-RPC 调用 Tool。
3. 可选启动并注册独立 MCP Demo Server。
4. 同步并调用外部 Tool。

### 复杂业务链路

1. 上传 DOCX 简历。
2. 解析简历。
3. 读取职位页面。
4. 调用同步或异步简历优化。
5. 查看结构化结果。

---

## 11. 当前边界

- MCP 是 HTTP JSON-RPC 风格 MVP，不是完整标准 MCP transport。
- 暂未完整支持 stdio、SSE / Streamable HTTP 和 OAuth。
- External MCP Client 不负责管理外部 Server 的进程生命周期。
- Jar 插件通过 URLClassLoader 加载，不构成安全沙箱。
- 关闭 URLClassLoader 不保证 JVM 立即卸载已经加载的插件类。
- 外部插件不支持普通 Spring Bean 注入。
- 插件尚未实现完整依赖隔离和权限沙箱。
- 部分模型和外部工具能力需要网络与 Provider Secret。
- Vision Chat 当前不支持音频或视频。
- Web Console 是静态开发控制台，不是完整产品前端。
- Workflow 等视图基于 Run History 和 Trace，是只读观察视图。
- 简历异步优化使用单机线程池，服务重启后的 RUNNING 任务不会自动恢复。
- 当前尚未实现完整 RAG、Agent Team、生产级高可用和多租户治理。

---

## 12. 项目原则

- 不把规划写成已经完成的能力。
- 不把单机实验写成生产高可用。
- 不把 URLClassLoader 写成安全沙箱。
- 不让 AgentRuntime 承担具体业务实现。
- 不在 Skill 中重复 Service 业务逻辑。
- 不将 Secret 写入源码、日志或示例。
- 简历优化不能编造原始简历中不存在的经历。
- 大报告 Skill 避免不必要的二次模型总结。

---

## 13. 文档与脚本

演示和 CLI 脚本说明：

- [scripts/README.md](scripts/README.md)

常用命令：

~~~powershell
mvn compile
mvn test
git status --short
git diff --stat
~~~

---

## 14. 后续方向

完成当前 Agent 平台 MVP 后，项目计划继续向个人知识与学习 Agent 平台演进，重点包括：

1. Conversation Context 压缩与缓存
2. Knowledge Workspace
3. Java RAG Kernel 与 Retrieval Inspector
4. Study Workspace、Knowledge Atlas 和 Learner Model
5. Learning Skill 标准化
6. Web Search MCP 与来源审批
7. Domain Radar
8. 统一异步 Agent Job 与事件流
9. 插件进程级隔离
10. 产品前端与个人部署

这些内容属于后续方向，不代表当前 GitHub main 已经实现。
