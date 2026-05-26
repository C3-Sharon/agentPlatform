# AI Agent Platform CLI Console

这些 PowerShell 脚本是 AI Agent Platform 的 CLI 控制台，用于考核演示核心能力。脚本只调用 HTTP API，不修改 Java 后端代码，不包含 API Key、数据库密码或其他敏感信息。

## 前置条件

启动主项目：

```powershell
cd D:\agent\agentPlatform
mvn spring-boot:run
```

如需演示 External MCP，请启动独立 demo server：

```powershell
cd D:\agent\mcp-demo-server
mvn spring-boot:run
```

如需 Vision Demo：

- 配置真实可用的 vision model，并启用该模型。
- 准备测试图片：`scripts\demo-assets\vision_test_scene.jpg`
- 或修改 `demo-vision.ps1` 中的 `$ImagePath`。

如需 Plugin Demo：

- 准备 plugin-demo Jar。
- 默认路径：`D:\agent\plugin-demo\target\plugin-demo-1.0.0.jar`
- 或修改 `demo-plugin-market.ps1` 中的 `$PluginJar`。

## 推荐运行顺序

1. `.\scripts\demo-models.ps1`
2. `.\scripts\demo-vision.ps1`
3. `.\scripts\demo-skills.ps1`
4. `.\scripts\demo-plugin-market.ps1`
5. `.\scripts\demo-mcp.ps1`
6. `.\scripts\demo-external-mcp.ps1`
7. `.\scripts\demo-memory.ps1`
8. `.\scripts\demo-agent-runs.ps1`
9. `.\scripts\demo-chat.ps1`

## 能力点对应

- `demo-models.ps1`：多模型配置、模型元数据、运行时切换、disabled model 拦截。
- `demo-vision.ps1`：Vision Chat，多模态图片输入，vision/multimodal capability 校验。
- `demo-skills.ps1`：Skill Registry、Skill 调用统计、统一 Skill 启用/禁用。
- `demo-plugin-market.ps1`：插件 Jar 上传、插件市场列表、插件启用/禁用、插件 Skill 注册。
- `demo-mcp.ps1`：内部 MCP Tool Registry、REST MCP 调用、JSON-RPC tools/list 和 tools/call。
- `demo-external-mcp.ps1`：注册外部 MCP Server、同步外部工具、调用外部工具。
- `demo-memory.ps1`：短期记忆 MySQL 持久化、会话消息查询。
- `demo-agent-runs.ps1`：Agent Run History 和 trace 复盘。
- `demo-chat.ps1`：普通聊天、自然语言 Skill 调用、显式 Skill 调用追问。

## 常见问题

- API Key 未配置：真实模型调用会失败；可先用列表类接口或 mock/disabled 场景演示。
- vision model disabled：`demo-vision.ps1` 会返回 `Model is disabled`，这是可接受的 MVP 校验结果。
- plugin jar 不存在：先构建 plugin-demo Jar，或修改 `$PluginJar`。
- external MCP server 未启动：先启动 `D:\agent\mcp-demo-server`。
- Skill disabled 导致调用失败：运行 `POST /api/skills/{skillName}/enable` 或使用 `demo-skills.ps1` 恢复 calculator。

## 修改变量

每个脚本顶部都有可调整变量：

- `$BaseUrl = "http://localhost:8080"`
- `$PluginJar = "D:\agent\plugin-demo\target\plugin-demo-1.0.0.jar"`
- `$ImagePath = Join-Path $PSScriptRoot "demo-assets\vision_test_scene.jpg"`
- `$ExternalMcpUrl = "http://localhost:8090/mcp/rpc"`

如果后端端口或文件路径不同，直接修改脚本顶部变量即可。
