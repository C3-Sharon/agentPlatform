package com.sharon.agentplatform.mcp.external.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.mcp.external.dto.McpExternalCheckStepResponse;
import com.sharon.agentplatform.mcp.external.dto.McpExternalServerCreateRequest;
import com.sharon.agentplatform.mcp.external.dto.McpExternalServerHealthResponse;
import com.sharon.agentplatform.mcp.external.dto.McpExternalServerResponse;
import com.sharon.agentplatform.mcp.external.dto.McpExternalToolCallRequest;
import com.sharon.agentplatform.mcp.external.dto.McpExternalToolCallResponse;
import com.sharon.agentplatform.mcp.external.dto.McpExternalToolResponse;
import com.sharon.agentplatform.mcp.external.entity.McpExternalServerEntity;
import com.sharon.agentplatform.mcp.external.entity.McpExternalToolEntity;
import com.sharon.agentplatform.mcp.external.repository.McpExternalServerRepository;
import com.sharon.agentplatform.mcp.external.repository.McpExternalToolRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class McpExternalServerService {

    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_SYNCED = "SYNCED";
    private static final String STATUS_FAILED = "FAILED";

    private final McpExternalServerRepository serverRepository;
    private final McpExternalToolRepository toolRepository;
    private final McpExternalHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public McpExternalServerService(McpExternalServerRepository serverRepository,
                                    McpExternalToolRepository toolRepository,
                                    McpExternalHttpClient httpClient,
                                    ObjectMapper objectMapper) {
        this.serverRepository = serverRepository;
        this.toolRepository = toolRepository;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public McpExternalServerResponse registerServer(McpExternalServerCreateRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException("External MCP server name is required");
        }
        if (request.getBaseUrl() == null || request.getBaseUrl().isBlank()) {
            throw new BusinessException("External MCP server baseUrl is required");
        }
        if (serverRepository.findByName(request.getName().trim()).isPresent()) {
            throw new BusinessException("External MCP server name already exists: " + request.getName().trim());
        }

        LocalDateTime now = LocalDateTime.now();
        McpExternalServerEntity entity = new McpExternalServerEntity();
        entity.setServerId(UUID.randomUUID().toString().replace("-", ""));
        entity.setName(request.getName().trim());
        entity.setBaseUrl(request.getBaseUrl().trim());
        entity.setStatus(STATUS_CREATED);
        entity.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return toServerResponse(serverRepository.save(entity));
    }

    public List<McpExternalServerResponse> listServers() {
        return serverRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toServerResponse)
                .toList();
    }

    public McpExternalServerResponse getServer(String serverId) {
        return toServerResponse(findServer(serverId));
    }

    public McpExternalServerHealthResponse healthCheck(String serverId) {
        McpExternalServerEntity server = findServer(serverId);
        McpExternalServerHealthResponse response = createHealthResponse(server);

        tryInitialize(server, response);
        tryPing(server, response);
        tryListTools(server, response);

        boolean healthy = Boolean.TRUE.equals(response.getToolsListOk());
        response.setHealthy(healthy);
        response.setStatus(healthy ? "UP" : "DOWN");
        if (!healthy) {
            response.setErrorMessage(response.getToolsListError());
        }
        return response;
    }

    public McpExternalServerResponse syncTools(String serverId) {
        McpExternalServerEntity server = findServer(serverId);
        McpExternalServerHealthResponse health = createHealthResponse(server);
        List<String> warnings = new ArrayList<>();
        tryInitialize(server, health);
        tryPing(server, health);
        if (!Boolean.TRUE.equals(health.getInitializeOk())) {
            warnings.add("External MCP initialize failed or is not supported: " + health.getInitializeError());
        }
        if (!Boolean.TRUE.equals(health.getPingOk())) {
            warnings.add("External MCP ping failed or is not supported: " + health.getPingError());
        }

        try {
            Map<String, Object> response = httpClient.listTools(server.getBaseUrl());
            List<?> tools = readTools(response);
            LocalDateTime now = LocalDateTime.now();
            List<String> syncedToolNames = new ArrayList<>();
            for (Object item : tools) {
                if (item instanceof Map<?, ?> toolMap) {
                    String remoteName = stringValue(toolMap.get("name"));
                    upsertTool(server, toolMap, now);
                    if (remoteName != null && !remoteName.isBlank()) {
                        syncedToolNames.add(remoteName);
                    }
                }
            }
            markToolsListHealthy(health, tools, syncedToolNames);
            server.setStatus(STATUS_SYNCED);
            server.setErrorMessage(null);
            server.setLastSyncedAt(now);
            server.setUpdatedAt(now);
            McpExternalServerResponse serverResponse = toServerResponse(serverRepository.save(server));
            serverResponse.setHealth(health);
            serverResponse.setWarnings(warnings);
            serverResponse.setSyncedToolCount(syncedToolNames.size());
            serverResponse.setSyncedToolNames(syncedToolNames);
            return serverResponse;
        } catch (RuntimeException exception) {
            markToolsListFailed(health, exception);
            server.setStatus(STATUS_FAILED);
            server.setErrorMessage(exception.getMessage());
            server.setUpdatedAt(LocalDateTime.now());
            serverRepository.save(server);
            throw exception;
        }
    }

    public McpExternalToolCallResponse callTool(String toolId, McpExternalToolCallRequest request) {
        McpExternalToolEntity tool = toolRepository.findByToolId(toolId)
                .orElseThrow(() -> new BusinessException("External MCP tool not found: " + toolId));
        McpExternalServerEntity server = findServer(tool.getServerId());
        if (!Boolean.TRUE.equals(server.getEnabled())) {
            throw new BusinessException("External MCP server is disabled: " + server.getServerId());
        }
        if (!Boolean.TRUE.equals(tool.getEnabled())) {
            throw new BusinessException("External MCP tool is disabled: " + tool.getToolId());
        }

        Map<String, Object> response = httpClient.callTool(server.getBaseUrl(), tool.getRemoteName(), request == null ? null : request.getArguments());
        Object result = response.get("result");
        McpExternalToolCallResponse callResponse = new McpExternalToolCallResponse();
        callResponse.setToolId(tool.getToolId());
        callResponse.setServerId(tool.getServerId());
        callResponse.setRemoteName(tool.getRemoteName());
        callResponse.setLocalName(tool.getLocalName());
        callResponse.setResult(result);
        callResponse.setSuccess(true);
        callResponse.setErrorMessage(null);
        return callResponse;
    }

    private McpExternalServerEntity findServer(String serverId) {
        return serverRepository.findByServerId(serverId)
                .orElseThrow(() -> new BusinessException("External MCP server not found: " + serverId));
    }

    private McpExternalServerHealthResponse createHealthResponse(McpExternalServerEntity server) {
        McpExternalServerHealthResponse response = new McpExternalServerHealthResponse();
        response.setServerId(server.getServerId());
        response.setName(server.getName());
        response.setBaseUrl(server.getBaseUrl());
        response.setEnabled(server.getEnabled());
        response.setCheckedAt(LocalDateTime.now());
        response.setChecks(new ArrayList<>());
        return response;
    }

    private void tryInitialize(McpExternalServerEntity server, McpExternalServerHealthResponse health) {
        try {
            Map<String, Object> response = httpClient.initialize(server.getBaseUrl());
            Map<String, Object> result = readResultMap(response);
            health.setInitializeOk(true);
            health.setRawInitializeResult(result);
            health.setProtocolVersion(stringValue(result.get("protocolVersion")));
            health.setServerInfo(result.get("serverInfo"));
            health.setCapabilities(result.get("capabilities"));
            addCheck(
                    health,
                    "initialize",
                    true,
                    "initialize succeeded",
                    null
            );
        } catch (RuntimeException exception) {
            health.setInitializeOk(false);
            health.setInitializeError(exception.getMessage());
            addCheck(
                    health,
                    "initialize",
                    false,
                    exception.getMessage(),
                    "The external MCP server may only support tools/list and tools/call. This does not block tool sync if tools/list is available."
            );
        }
    }

    private void tryPing(McpExternalServerEntity server, McpExternalServerHealthResponse health) {
        try {
            Map<String, Object> response = httpClient.ping(server.getBaseUrl());
            health.setPingOk(true);
            health.setRawPingResult(readResultMap(response));
            addCheck(
                    health,
                    "ping",
                    true,
                    "ping succeeded",
                    null
            );
        } catch (RuntimeException exception) {
            health.setPingOk(false);
            health.setPingError(exception.getMessage());
            addCheck(
                    health,
                    "ping",
                    false,
                    exception.getMessage(),
                    "The external MCP server may not implement ping. This is acceptable for the current HTTP JSON-RPC MVP if tools/list is available."
            );
        }
    }

    private void tryListTools(McpExternalServerEntity server, McpExternalServerHealthResponse health) {
        try {
            Map<String, Object> response = httpClient.listTools(server.getBaseUrl());
            List<?> tools = readTools(response);
            List<String> toolNames = new ArrayList<>();
            for (Object item : tools) {
                if (item instanceof Map<?, ?> toolMap) {
                    String name = stringValue(toolMap.get("name"));
                    if (name != null && !name.isBlank()) {
                        toolNames.add(name);
                    }
                }
            }
            health.setToolsListOk(true);
            health.setToolCount(tools.size());
            health.setToolNames(toolNames);
            addCheck(
                    health,
                    "tools/list",
                    true,
                    "tools/list returned " + tools.size() + " tools",
                    null
            );
        } catch (RuntimeException exception) {
            health.setToolsListOk(false);
            health.setToolsListError(exception.getMessage());
            health.setToolCount(0);
            health.setToolNames(List.of());
            addCheck(
                    health,
                    "tools/list",
                    false,
                    exception.getMessage(),
                    "Check the external MCP endpoint URL and verify that it supports JSON-RPC method tools/list."
            );
        }
    }

    private void markToolsListHealthy(McpExternalServerHealthResponse health, List<?> tools, List<String> toolNames) {
        health.setToolsListOk(true);
        health.setToolsListError(null);
        health.setToolCount(tools.size());
        health.setToolNames(toolNames);
        health.setHealthy(true);
        health.setStatus("UP");
        health.setErrorMessage(null);
        addCheck(
                health,
                "tools/list",
                true,
                "tools/list returned " + tools.size() + " tools",
                null
        );
    }

    private void markToolsListFailed(McpExternalServerHealthResponse health, RuntimeException exception) {
        health.setToolsListOk(false);
        health.setToolsListError(exception.getMessage());
        health.setToolCount(0);
        health.setToolNames(List.of());
        health.setHealthy(false);
        health.setStatus("DOWN");
        health.setErrorMessage(exception.getMessage());
        addCheck(
                health,
                "tools/list",
                false,
                exception.getMessage(),
                "Check the external MCP endpoint URL and verify that it supports JSON-RPC method tools/list."
        );
    }

    private void addCheck(McpExternalServerHealthResponse health,
                          String stage,
                          boolean success,
                          String message,
                          String suggestion) {
        McpExternalCheckStepResponse check = new McpExternalCheckStepResponse();
        check.setStage(stage);
        check.setSuccess(success);
        check.setMessage(message);
        check.setSuggestion(suggestion);
        if (health.getChecks() == null) {
            health.setChecks(new ArrayList<>());
        }
        health.getChecks().add(check);
    }

    private void upsertTool(McpExternalServerEntity server, Map<?, ?> toolMap, LocalDateTime now) {
        String remoteName = stringValue(toolMap.get("name"));
        if (remoteName == null || remoteName.isBlank()) {
            return;
        }
        McpExternalToolEntity entity = toolRepository.findByServerIdAndRemoteName(server.getServerId(), remoteName)
                .orElseGet(() -> {
                    McpExternalToolEntity created = new McpExternalToolEntity();
                    created.setToolId(UUID.randomUUID().toString().replace("-", ""));
                    created.setServerId(server.getServerId());
                    created.setRemoteName(remoteName);
                    created.setCreatedAt(now);
                    return created;
                });
        entity.setLocalName("external." + server.getName() + "." + remoteName);
        entity.setDescription(stringValue(toolMap.get("description")));
        entity.setInputSchemaJson(toJson(toolMap.get("inputSchema")));
        entity.setEnabled(true);
        entity.setLastSyncedAt(now);
        entity.setUpdatedAt(now);
        toolRepository.save(entity);
    }

    @SuppressWarnings("unchecked")
    private List<?> readTools(Map<String, Object> response) {
        Object result = response.get("result");
        if (result instanceof Map<?, ?> resultMap) {
            Object tools = resultMap.get("tools");
            if (tools instanceof List<?> toolList) {
                return toolList;
            }
        }
        throw new BusinessException("External MCP tools/list response missing result.tools");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readResultMap(Map<String, Object> response) {
        Object result = response.get("result");
        if (result instanceof Map<?, ?> resultMap) {
            return (Map<String, Object>) resultMap;
        }
        return Map.of();
    }

    private McpExternalServerResponse toServerResponse(McpExternalServerEntity entity) {
        McpExternalServerResponse response = new McpExternalServerResponse();
        response.setServerId(entity.getServerId());
        response.setName(entity.getName());
        response.setBaseUrl(entity.getBaseUrl());
        response.setStatus(entity.getStatus());
        response.setEnabled(entity.getEnabled());
        response.setErrorMessage(entity.getErrorMessage());
        response.setLastSyncedAt(entity.getLastSyncedAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setTools(toolRepository.findByServerIdOrderByCreatedAtAsc(entity.getServerId()).stream().map(this::toToolResponse).toList());
        return response;
    }

    private McpExternalToolResponse toToolResponse(McpExternalToolEntity entity) {
        McpExternalToolResponse response = new McpExternalToolResponse();
        response.setToolId(entity.getToolId());
        response.setServerId(entity.getServerId());
        response.setRemoteName(entity.getRemoteName());
        response.setLocalName(entity.getLocalName());
        response.setDescription(entity.getDescription());
        response.setInputSchema(parseJson(entity.getInputSchemaJson()));
        response.setEnabled(entity.getEnabled());
        response.setLastSyncedAt(entity.getLastSyncedAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            return String.valueOf(value);
        }
    }

    private Object parseJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception exception) {
            return json;
        }
    }
}
