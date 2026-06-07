package com.sharon.agentplatform.mcp.rpc.handler;

import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class McpPromptsRpcHandler {

    private static final int INVALID_PARAMS = -32602;
    private static final String PROMPT_SKILL_CALL_HELP = "agent.skill-call-help";
    private static final String PROMPT_RESUME_OPTIMIZE_OUTLINE = "resume.optimize-outline";
    private static final String PROMPT_MCP_TOOL_DEBUG = "mcp.tool-debug";

    public McpJsonRpcResponse listPrompts(Object id) {
        return McpJsonRpcResponse.success(id, Map.of(
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

    public McpJsonRpcResponse getPrompt(Object id, Map<String, Object> params) {
        Object nameValue = params == null ? null : params.get("name");
        String name = nameValue == null ? null : nameValue.toString();
        if (name == null || name.isBlank()) {
            return McpJsonRpcResponse.error(id, INVALID_PARAMS, "Missing required param: name");
        }

        Map<String, Object> arguments = readPromptArguments(params);
        return switch (name) {
            case PROMPT_SKILL_CALL_HELP -> McpJsonRpcResponse.success(id, promptResult(
                    "用于帮助用户按清晰参数调用 Skill 的提示模板。",
                    buildSkillCallHelpPrompt(arguments)
            ));
            case PROMPT_RESUME_OPTIMIZE_OUTLINE -> McpJsonRpcResponse.success(id, promptResult(
                    "用于生成简历优化报告结构的提示模板。",
                    buildResumeOptimizeOutlinePrompt(arguments)
            ));
            case PROMPT_MCP_TOOL_DEBUG -> McpJsonRpcResponse.success(id, promptResult(
                    "用于调试 MCP 工具调用的提示模板。",
                    buildMcpToolDebugPrompt(arguments)
            ));
            default -> McpJsonRpcResponse.error(id, INVALID_PARAMS, "MCP prompt not found: " + name);
        };
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
}
