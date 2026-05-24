package com.sharon.agentplatform.mcp.tool;

import com.sharon.agentplatform.agent.history.entity.AgentRunEntity;
import com.sharon.agentplatform.agent.history.repository.AgentRunRepository;
import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolMetadata;
import com.sharon.agentplatform.mcp.core.McpToolRequest;
import com.sharon.agentplatform.mcp.core.McpToolResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseRecentAgentRunsMcpTool implements McpTool {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final AgentRunRepository agentRunRepository;

    public DatabaseRecentAgentRunsMcpTool(AgentRunRepository agentRunRepository) {
        this.agentRunRepository = agentRunRepository;
    }

    @Override
    public McpToolMetadata metadata() {
        McpToolMetadata metadata = new McpToolMetadata();
        metadata.setName("database.recent_agent_runs");
        metadata.setDisplayName("最近 Agent 运行记录");
        metadata.setDescription("查询最近的 Agent Run History，用于演示 MCP 数据库查询工具。");
        metadata.setVersion("1.0.0");
        metadata.setInputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                        "limit", Map.of(
                                "type", "number",
                                "description", "返回数量，默认 10，最大 50"
                        )
                ),
                "required", List.of()
        ));
        return metadata;
    }

    @Override
    public McpToolResponse call(McpToolRequest request) {
        try {
            int limit = resolveLimit(request == null ? null : request.getParams());
            List<Map<String, Object>> runs = agentRunRepository.findTop50ByOrderByCreatedAtDesc()
                    .stream()
                    .limit(limit)
                    .map(this::toRunSummary)
                    .toList();

            return McpToolResponse.success(Map.of(
                    "limit", limit,
                    "count", runs.size(),
                    "runs", runs
            ));
        } catch (Exception exception) {
            return McpToolResponse.fail("Failed to query recent agent runs: " + exception.getMessage());
        }
    }

    private int resolveLimit(Map<String, Object> params) {
        Object value = params == null ? null : params.get("limit");
        int limit = parseLimit(value);
        if (limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private int parseLimit(Object value) {
        if (value == null) {
            return DEFAULT_LIMIT;
        }
        try {
            if (value instanceof Integer integerValue) {
                return integerValue;
            }
            if (value instanceof Long longValue) {
                return longValue.intValue();
            }
            if (value instanceof Double doubleValue) {
                return doubleValue.intValue();
            }
            if (value instanceof Number numberValue) {
                return numberValue.intValue();
            }
            if (value instanceof String stringValue) {
                return Integer.parseInt(stringValue.trim());
            }
        } catch (RuntimeException ignored) {
            return DEFAULT_LIMIT;
        }
        return DEFAULT_LIMIT;
    }

    private Map<String, Object> toRunSummary(AgentRunEntity entity) {
        return Map.of(
                "runId", valueOrEmpty(entity.getRunId()),
                "conversationId", valueOrEmpty(entity.getConversationId()),
                "modelId", valueOrEmpty(entity.getModelId()),
                "usedModel", valueOrEmpty(entity.getUsedModel()),
                "usedSkillsJson", valueOrEmpty(entity.getUsedSkillsJson()),
                "status", valueOrEmpty(entity.getStatus()),
                "errorMessage", valueOrEmpty(entity.getErrorMessage()),
                "durationMs", entity.getDurationMs() == null ? 0L : entity.getDurationMs(),
                "createdAt", entity.getCreatedAt() == null ? "" : entity.getCreatedAt().toString(),
                "updatedAt", entity.getUpdatedAt() == null ? "" : entity.getUpdatedAt().toString()
        );
    }

    private String valueOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
