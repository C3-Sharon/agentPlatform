package com.sharon.agentplatform.mcp.external.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.common.exception.BusinessException;
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
        McpExternalServerHealthResponse response = new McpExternalServerHealthResponse();
        response.setServerId(server.getServerId());
        response.setName(server.getName());
        response.setBaseUrl(server.getBaseUrl());
        response.setEnabled(server.getEnabled());
        response.setCheckedAt(LocalDateTime.now());

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
        try {
            Map<String, Object> response = httpClient.listTools(server.getBaseUrl());
            List<?> tools = readTools(response);
            LocalDateTime now = LocalDateTime.now();
            for (Object item : tools) {
                if (item instanceof Map<?, ?> toolMap) {
                    upsertTool(server, toolMap, now);
                }
            }
            server.setStatus(STATUS_SYNCED);
            server.setErrorMessage(null);
            server.setLastSyncedAt(now);
            server.setUpdatedAt(now);
            return toServerResponse(serverRepository.save(server));
        } catch (RuntimeException exception) {
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

    private void tryInitialize(McpExternalServerEntity server, McpExternalServerHealthResponse health) {
        try {
            Map<String, Object> response = httpClient.initialize(server.getBaseUrl());
            Map<String, Object> result = readResultMap(response);
            health.setInitializeOk(true);
            health.setRawInitializeResult(result);
            health.setProtocolVersion(stringValue(result.get("protocolVersion")));
            health.setServerInfo(result.get("serverInfo"));
            health.setCapabilities(result.get("capabilities"));
        } catch (RuntimeException exception) {
            health.setInitializeOk(false);
            health.setInitializeError(exception.getMessage());
        }
    }

    private void tryPing(McpExternalServerEntity server, McpExternalServerHealthResponse health) {
        try {
            Map<String, Object> response = httpClient.ping(server.getBaseUrl());
            health.setPingOk(true);
            health.setRawPingResult(readResultMap(response));
        } catch (RuntimeException exception) {
            health.setPingOk(false);
            health.setPingError(exception.getMessage());
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
        } catch (RuntimeException exception) {
            health.setToolsListOk(false);
            health.setToolsListError(exception.getMessage());
            health.setToolCount(0);
            health.setToolNames(List.of());
        }
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
