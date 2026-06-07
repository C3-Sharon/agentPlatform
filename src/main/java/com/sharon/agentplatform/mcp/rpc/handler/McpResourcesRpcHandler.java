package com.sharon.agentplatform.mcp.rpc.handler;

import com.sharon.agentplatform.agent.history.entity.AgentRunEntity;
import com.sharon.agentplatform.agent.history.repository.AgentRunRepository;
import com.sharon.agentplatform.conversation.resource.service.ConversationResourceService;
import com.sharon.agentplatform.memory.service.MemoryService;
import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcRequest;
import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcResponse;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class McpResourcesRpcHandler {

    private static final int INVALID_PARAMS = -32602;
    private static final int TOOL_EXECUTION_ERROR = -32000;
    private static final String RESOURCE_RECENT_RUNS = "agent://runs/recent";
    private static final String RESOURCE_LONG_TERM_MEMORY = "agent://memory/long-term";
    private static final String RESOURCE_CONVERSATION_RESOURCES = "agent://conversations/resources";

    private final AgentRunRepository agentRunRepository;
    private final MemoryService memoryService;
    private final ConversationResourceService conversationResourceService;

    public McpResourcesRpcHandler(AgentRunRepository agentRunRepository,
                                  MemoryService memoryService,
                                  ConversationResourceService conversationResourceService) {
        this.agentRunRepository = agentRunRepository;
        this.memoryService = memoryService;
        this.conversationResourceService = conversationResourceService;
    }

    public McpJsonRpcResponse listResources(Object id) {
        return McpJsonRpcResponse.success(id, Map.of(
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

    public McpJsonRpcResponse listResourceTemplates(Object id) {
        return McpJsonRpcResponse.success(id, Map.of(
                "resourceTemplates", List.of(
                        resourceTemplateDescriptor(
                                RESOURCE_RECENT_RUNS + "?limit={limit}",
                                "Recent Agent Runs",
                                "Read recent Agent Run History summaries. The limit defaults to 10 and is capped at 50.",
                                "application/json"
                        ),
                        resourceTemplateDescriptor(
                                RESOURCE_LONG_TERM_MEMORY + "?conversationId={conversationId}",
                                "Long-Term Memory By Conversation",
                                "Read FileLongTermMemoryStore entries for a conversationId.",
                                "application/json"
                        ),
                        resourceTemplateDescriptor(
                                RESOURCE_CONVERSATION_RESOURCES + "?conversationId={conversationId}",
                                "Conversation Resources",
                                "Read resources attached to a conversationId.",
                                "application/json"
                        )
                )
        ));
    }

    public McpJsonRpcResponse readResource(McpJsonRpcRequest request) {
        Map<String, Object> params = request.getParams();
        Object uriValue = params == null ? null : params.get("uri");
        String uri = uriValue == null ? null : uriValue.toString();
        if (uri == null || uri.isBlank()) {
            return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Missing required param: uri");
        }

        try {
            if (uri.startsWith(RESOURCE_RECENT_RUNS)) {
                int limit = parseLimit(queryParam(uri, "limit"), 10, 50);
                return resourceReadSuccess(request.getId(), uri, readRecentRuns(limit));
            }
            if (uri.startsWith(RESOURCE_LONG_TERM_MEMORY)) {
                String conversationId = queryParam(uri, "conversationId");
                if (conversationId == null || conversationId.isBlank()) {
                    return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Missing required query param: conversationId");
                }
                return resourceReadSuccess(request.getId(), uri, Map.of(
                        "conversationId", conversationId,
                        "items", memoryService.getLongTermMemories(conversationId)
                ));
            }
            if (uri.startsWith(RESOURCE_CONVERSATION_RESOURCES)) {
                String conversationId = queryParam(uri, "conversationId");
                if (conversationId == null || conversationId.isBlank()) {
                    return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Missing required query param: conversationId");
                }
                return resourceReadSuccess(request.getId(), uri, Map.of(
                        "conversationId", conversationId,
                        "resources", conversationResourceService.listResources(conversationId)
                ));
            }
        } catch (RuntimeException exception) {
            return McpJsonRpcResponse.error(request.getId(), TOOL_EXECUTION_ERROR, "Failed to read MCP resource: " + exception.getMessage());
        }

        return McpJsonRpcResponse.error(request.getId(), INVALID_PARAMS, "MCP resource not found: " + uri);
    }

    private Map<String, Object> resourceDescriptor(String uri, String name, String description, String mimeType) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("uri", uri);
        resource.put("name", name);
        resource.put("description", description);
        resource.put("mimeType", mimeType);
        return resource;
    }

    private Map<String, Object> resourceTemplateDescriptor(String uriTemplate, String name, String description, String mimeType) {
        Map<String, Object> resourceTemplate = new LinkedHashMap<>();
        resourceTemplate.put("uriTemplate", uriTemplate);
        resourceTemplate.put("name", name);
        resourceTemplate.put("description", description);
        resourceTemplate.put("mimeType", mimeType);
        return resourceTemplate;
    }

    private McpJsonRpcResponse resourceReadSuccess(Object id, String uri, Object data) {
        return McpJsonRpcResponse.success(id, Map.of(
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
