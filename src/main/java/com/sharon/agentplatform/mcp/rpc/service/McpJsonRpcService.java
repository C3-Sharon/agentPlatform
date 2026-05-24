package com.sharon.agentplatform.mcp.rpc.service;

import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolRegistry;
import com.sharon.agentplatform.mcp.core.McpToolRequest;
import com.sharon.agentplatform.mcp.core.McpToolResponse;
import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcRequest;
import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class McpJsonRpcService {

    private static final String JSON_RPC_VERSION = "2.0";
    private static final int INVALID_REQUEST = -32600;
    private static final int METHOD_NOT_FOUND = -32601;
    private static final int INVALID_PARAMS = -32602;
    private static final int TOOL_EXECUTION_ERROR = -32000;

    private final McpToolRegistry mcpToolRegistry;

    public McpJsonRpcService(McpToolRegistry mcpToolRegistry) {
        this.mcpToolRegistry = mcpToolRegistry;
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
            case "tools/list" -> handleToolsList(request);
            case "tools/call" -> handleToolsCall(request);
            default -> McpJsonRpcResponse.error(request.getId(), METHOD_NOT_FOUND, "Method not found: " + request.getMethod());
        };
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
}
