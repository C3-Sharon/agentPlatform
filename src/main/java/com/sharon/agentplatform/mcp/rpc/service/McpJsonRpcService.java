package com.sharon.agentplatform.mcp.rpc.service;

import com.sharon.agentplatform.agent.history.entity.AgentRunEntity;
import com.sharon.agentplatform.agent.history.repository.AgentRunRepository;
import com.sharon.agentplatform.conversation.resource.service.ConversationResourceService;
import com.sharon.agentplatform.memory.service.MemoryService;
import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolRegistry;
import com.sharon.agentplatform.mcp.core.McpToolRequest;
import com.sharon.agentplatform.mcp.core.McpToolResponse;
import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcRequest;
import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class McpJsonRpcService {

    private static final String JSON_RPC_VERSION = "2.0";
    private static final int INVALID_REQUEST = -32600;
    private static final int METHOD_NOT_FOUND = -32601;
    private static final int INVALID_PARAMS = -32602;
    private static final int TOOL_EXECUTION_ERROR = -32000;
    private static final String MCP_PROTOCOL_VERSION = "2024-11-05";
    private static final String MCP_SERVER_NAME = "agent-platform-mcp";
    private static final String RESOURCE_RECENT_RUNS = "agent://runs/recent";
    private static final String RESOURCE_LONG_TERM_MEMORY = "agent://memory/long-term";
    private static final String RESOURCE_CONVERSATION_RESOURCES = "agent://conversations/resources";
    private static final String PROMPT_SKILL_CALL_HELP = "agent.skill-call-help";
    private static final String PROMPT_RESUME_OPTIMIZE_OUTLINE = "resume.optimize-outline";
    private static final String PROMPT_MCP_TOOL_DEBUG = "mcp.tool-debug";

    private final McpToolRegistry mcpToolRegistry;
    private final AgentRunRepository agentRunRepository;
    private final MemoryService memoryService;
    private final ConversationResourceService conversationResourceService;
    private final String platformVersion;

    public McpJsonRpcService(McpToolRegistry mcpToolRegistry,
                             AgentRunRepository agentRunRepository,
                             MemoryService memoryService,
                             ConversationResourceService conversationResourceService,
                             @Value("${agentplatform.platform-version:0.0.1}") String platformVersion) {
        this.mcpToolRegistry = mcpToolRegistry;
        this.agentRunRepository = agentRunRepository;
        this.memoryService = memoryService;
        this.conversationResourceService = conversationResourceService;
        this.platformVersion = platformVersion;
    }

    public McpJsonRpcResponse handle(McpJsonRpcRequest request) {
        if (request == null) {
            return McpJsonRpcResponse.error(null, INVALID_REQUEST, "Invalid Request");
        }

        if (request.getJsonrpc() != null && !JSON_RPC_VERSION.equals(request.getJsonrpc())) {
            return McpJsonRpcResponse.error(request.getId(), INVALID_REQUEST, "Invalid JSON-RPC version: " + request.getJsonrpc());
        }

        if (request.getMethod() == null || request.getMethod().isBlank()) {
            return McpJsonRpcResponse.error(request.getId(), INVALID_REQUEST, "Missing required field: method");
        }

        return switch (request.getMethod()) {
            case "initialize" -> handleInitialize(request);
            case "ping" -> handlePing(request);
            case "tools/list" -> handleToolsList(request);
            case "tools/call" -> handleToolsCall(request);
            case "resources/list" -> handleResourcesList(request);
            case "resources/read" -> handleResourcesRead(request);
            case "prompts/list" -> handlePromptsList(request);
            case "prompts/get" -> handlePromptsGet(request);
            default -> McpJsonRpcResponse.error(request.getId(), METHOD_NOT_FOUND, "Method not found: " + request.getMethod());
        };
    }

    private McpJsonRpcResponse handleInitialize(McpJsonRpcRequest request) {
        return McpJsonRpcResponse.success(request.getId(), Map.of(
                "protocolVersion", MCP_PROTOCOL_VERSION,
                "serverInfo", Map.of(
                        "name", MCP_SERVER_NAME,
                        "version", platformVersion
                ),
                "capabilities", Map.of(
                        "tools", Map.of(),
                        "resources", Map.of(),
                        "prompts", Map.of()
                )
        ));
    }

    private McpJsonRpcResponse handlePing(McpJsonRpcRequest request) {
        return McpJsonRpcResponse.success(request.getId(), Map.of(
                "pong", true,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    private McpJsonRpcResponse handleToolsList(McpJsonRpcRequest request) {
        return McpJsonRpcResponse.success(request.getId(), Map.of(
                "tools", mcpToolRegistry.list()
        ));
    }

    private McpJsonRpcResponse handleToolsCall(McpJsonRpcRequest request) {
        Map<String, Object> params = request.getParams();
        Object nameValue = params == null ? null : params.get("name");
        String name = nameValue == null ? null : nameValue.toString();
        if (name == null || name.isBlank()) {
            return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Missing required param: name");
        }

        McpTool tool = mcpToolRegistry.get(name).orElse(null);
        if (tool == null) {
            return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "MCP tool not found: " + name);
        }

        McpToolRequest toolRequest = new McpToolRequest();
        toolRequest.setParams(readArguments(params));
        McpToolResponse toolResponse = tool.call(toolRequest);
        if (!toolResponse.isSuccess()) {
            return McpJsonRpcResponse.error(request.getId(), TOOL_EXECUTION_ERROR, toolResponse.getErrorMessage());
        }

        return McpJsonRpcResponse.success(request.getId(), Map.of(
                "content", toolResponse.getResult()
        ));
    }

    private McpJsonRpcResponse handleResourcesList(McpJsonRpcRequest request) {
        return McpJsonRpcResponse.success(request.getId(), Map.of(
                "resources", List.of(
                        resourceDescriptor(
                                RESOURCE_RECENT_RUNS + "?limit=10",
                                "Recent Agent Runs",
                                "Read recent Agent Run History summaries.",
                                "application/json"
                        ),
                        resourceDescriptor(
                                RESOURCE_LONG_TERM_MEMORY + "?conversationId={conversationId}",
                                "Long-Term Memory By Conversation",
                                "Read FileLongTermMemoryStore entries for a conversationId.",
                                "application/json"
                        ),
                        resourceDescriptor(
                                RESOURCE_CONVERSATION_RESOURCES + "?conversationId={conversationId}",
                                "Conversation Resources",
                                "Read resources attached to a conversationId.",
                                "application/json"
                        )
                )
        ));
    }

    private McpJsonRpcResponse handleResourcesRead(McpJsonRpcRequest request) {
        Map<String, Object> params = request.getParams();
        Object uriValue = params == null ? null : params.get("uri");
        String uri = uriValue == null ? null : uriValue.toString();
        if (uri == null || uri.isBlank()) {
            return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Missing required param: uri");
        }

        try {
            if (uri.startsWith(RESOURCE_RECENT_RUNS)) {
                int limit = parseLimit(queryParam(uri, "limit"), 10, 50);
                return resourceReadSuccess(request, uri, readRecentRuns(limit));
            }
            if (uri.startsWith(RESOURCE_LONG_TERM_MEMORY)) {
                String conversationId = queryParam(uri, "conversationId");
                if (conversationId == null || conversationId.isBlank()) {
                    return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Missing required query param: conversationId");
                }
                return resourceReadSuccess(request, uri, Map.of(
                        "conversationId", conversationId,
                        "items", memoryService.getLongTermMemories(conversationId)
                ));
            }
            if (uri.startsWith(RESOURCE_CONVERSATION_RESOURCES)) {
                String conversationId = queryParam(uri, "conversationId");
                if (conversationId == null || conversationId.isBlank()) {
                    return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Missing required query param: conversationId");
                }
                return resourceReadSuccess(request, uri, Map.of(
                        "conversationId", conversationId,
                        "resources", conversationResourceService.listResources(conversationId)
                ));
            }
        } catch (RuntimeException exception) {
            return McpJsonRpcResponse.error(request.getId(), TOOL_EXECUTION_ERROR, "Failed to read MCP resource: " + exception.getMessage());
        }

        return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "MCP resource not found: " + uri);
    }

    private McpJsonRpcResponse handlePromptsList(McpJsonRpcRequest request) {
        return McpJsonRpcResponse.success(request.getId(), Map.of(
                "prompts", List.of(
                        promptDescriptor(
                                PROMPT_SKILL_CALL_HELP,
                                "Skill 调用辅助",
                                "生成面向用户的 Skill 调用参数说明。",
                                List.of(
                                        promptArgument("skillName", "需要调用的 Skill 名称。", true),
                                        promptArgument("userInput", "用户输入或任务描述。", true)
                                )
                        ),
                        promptDescriptor(
                                PROMPT_RESUME_OPTIMIZE_OUTLINE,
                                "简历优化大纲",
                                "根据简历文本和岗位文本生成简历优化报告大纲。",
                                List.of(
                                        promptArgument("resumeText", "解析后的简历文本。", true),
                                        promptArgument("jobPostingText", "岗位 JD 或招聘网页文本。", true)
                                )
                        ),
                        promptDescriptor(
                                PROMPT_MCP_TOOL_DEBUG,
                                "MCP 工具调试",
                                "生成用于排查 MCP 工具调用失败原因的诊断提示词。",
                                List.of(
                                        promptArgument("toolName", "MCP 工具名称。", true),
                                        promptArgument("argumentsJson", "传给工具的 JSON 参数。", false),
                                        promptArgument("errorMessage", "可选的工具错误信息。", false)
                                )
                        )
                )
        ));
    }

    private McpJsonRpcResponse handlePromptsGet(McpJsonRpcRequest request) {
        Map<String, Object> params = request.getParams();
        Object nameValue = params == null ? null : params.get("name");
        String name = nameValue == null ? null : nameValue.toString();
        if (name == null || name.isBlank()) {
            return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Missing required param: name");
        }

        Map<String, Object> arguments = readPromptArguments(params);
        return switch (name) {
            case PROMPT_SKILL_CALL_HELP -> McpJsonRpcResponse.success(request.getId(), promptResult(
                    "用于帮助用户按清晰参数调用 Skill 的提示模板。",
                    buildSkillCallHelpPrompt(arguments)
            ));
            case PROMPT_RESUME_OPTIMIZE_OUTLINE -> McpJsonRpcResponse.success(request.getId(), promptResult(
                    "用于生成简历优化报告结构的提示模板。",
                    buildResumeOptimizeOutlinePrompt(arguments)
            ));
            case PROMPT_MCP_TOOL_DEBUG -> McpJsonRpcResponse.success(request.getId(), promptResult(
                    "用于调试 MCP 工具调用的提示模板。",
                    buildMcpToolDebugPrompt(arguments)
            ));
            default -> McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "MCP prompt not found: " + name);
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readArguments(Map<String, Object> params) {
        if (params == null) {
            return Map.of();
        }

        Object arguments = params.get("arguments");
        if (arguments instanceof Map<?, ?> argumentMap) {
            return (Map<String, Object>) argumentMap;
        }

        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readPromptArguments(Map<String, Object> params) {
        if (params == null) {
            return Map.of();
        }

        Object arguments = params.get("arguments");
        if (arguments instanceof Map<?, ?> argumentMap) {
            return (Map<String, Object>) argumentMap;
        }

        return Map.of();
    }

    private Map<String, Object> resourceDescriptor(String uri, String name, String description, String mimeType) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("uri", uri);
        resource.put("name", name);
        resource.put("description", description);
        resource.put("mimeType", mimeType);
        return resource;
    }

    private Map<String, Object> promptDescriptor(String name,
                                                 String title,
                                                 String description,
                                                 List<Map<String, Object>> arguments) {
        Map<String, Object> prompt = new LinkedHashMap<>();
        prompt.put("name", name);
        prompt.put("title", title);
        prompt.put("description", description);
        prompt.put("arguments", arguments);
        return prompt;
    }

    private Map<String, Object> promptArgument(String name, String description, boolean required) {
        Map<String, Object> argument = new LinkedHashMap<>();
        argument.put("name", name);
        argument.put("description", description);
        argument.put("required", required);
        return argument;
    }

    private Map<String, Object> promptResult(String description, String text) {
        return Map.of(
                "description", description,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", Map.of(
                                "type", "text",
                                "text", text
                        )
                ))
        );
    }

    private String buildSkillCallHelpPrompt(Map<String, Object> arguments) {
        String skillName = argumentString(arguments, "skillName", "{skillName}");
        String userInput = argumentString(arguments, "userInput", "{userInput}");
        return """
                你正在帮助用户调用 AI Agent 平台中的 Skill。
                Skill 名称：%s
                用户输入：%s

                请清楚说明这个 Skill 需要哪些参数。
                优先给出明确的 key=value 示例。
                不要编造用户没有提供的业务参数。
                如果缺少必要参数，请提出简短、具体的追问。
                """.formatted(skillName, userInput).trim();
    }

    private String buildResumeOptimizeOutlinePrompt(Map<String, Object> arguments) {
        String resumeText = argumentString(arguments, "resumeText", "{resumeText}");
        String jobPostingText = argumentString(arguments, "jobPostingText", "{jobPostingText}");
        return """
                你正在准备一份简历优化报告。

                简历文本：
                %s

                岗位文本：
                %s

                请生成包含以下部分的报告大纲：
                1. 与岗位匹配的候选人优势
                2. 缺失或较弱的岗位要求
                3. 简历改写建议
                4. 面试准备重点
                5. 风险和事实边界

                不要编造简历文本中不存在的经历或事实。
                """.formatted(resumeText, jobPostingText).trim();
    }

    private String buildMcpToolDebugPrompt(Map<String, Object> arguments) {
        String toolName = argumentString(arguments, "toolName", "{toolName}");
        String argumentsJson = argumentString(arguments, "argumentsJson", "{}");
        String errorMessage = argumentString(arguments, "errorMessage", "");
        return """
                你正在调试一次 MCP 工具调用。
                工具名称：%s
                参数 JSON：%s
                错误信息：%s

                请检查：
                1. 工具名称是否有效
                2. 必填参数是否已经提供
                3. 参数类型是否符合 inputSchema
                4. 失败原因属于参数校验、传输问题还是工具执行问题
                5. 下一次应如何具体重试
                """.formatted(toolName, argumentsJson, errorMessage).trim();
    }

    private String argumentString(Map<String, Object> arguments, String key, String defaultValue) {
        if (arguments == null) {
            return defaultValue;
        }
        Object value = arguments.get(key);
        if (value == null || value.toString().isBlank()) {
            return defaultValue;
        }
        return value.toString();
    }

    private McpJsonRpcResponse resourceReadSuccess(McpJsonRpcRequest request, String uri, Object data) {
        return McpJsonRpcResponse.success(request.getId(), Map.of(
                "contents", List.of(Map.of(
                        "uri", uri,
                        "mimeType", "application/json",
                        "data", data
                ))
        ));
    }

    private Map<String, Object> readRecentRuns(int limit) {
        List<Map<String, Object>> runs = agentRunRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .map(this::toRunSummary)
                .toList();

        return Map.of(
                "limit", limit,
                "count", runs.size(),
                "runs", runs
        );
    }

    private Map<String, Object> toRunSummary(AgentRunEntity entity) {
        Map<String, Object> run = new LinkedHashMap<>();
        run.put("runId", entity.getRunId());
        run.put("conversationId", entity.getConversationId());
        run.put("modelId", entity.getModelId());
        run.put("usedModel", entity.getUsedModel());
        run.put("usedSkillsJson", entity.getUsedSkillsJson());
        run.put("status", entity.getStatus());
        run.put("errorMessage", entity.getErrorMessage());
        run.put("durationMs", entity.getDurationMs());
        run.put("createdAt", entity.getCreatedAt());
        return run;
    }

    private int parseLimit(String value, int defaultValue, int maxValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 1) {
                return defaultValue;
            }
            return Math.min(parsed, maxValue);
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    private String queryParam(String uri, String name) {
        int questionIndex = uri.indexOf('?');
        if (questionIndex < 0 || questionIndex == uri.length() - 1) {
            return null;
        }

        String query = uri.substring(questionIndex + 1);
        for (String pair : query.split("&")) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }
            String key = decode(pair.substring(0, equalsIndex));
            if (name.equals(key)) {
                return decode(pair.substring(equalsIndex + 1));
            }
        }
        return null;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
