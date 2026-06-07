package com.sharon.agentplatform.mcp.rpc.handler;

import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolRegistry;
import com.sharon.agentplatform.mcp.core.McpToolRequest;
import com.sharon.agentplatform.mcp.core.McpToolResponse;
import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class McpToolsRpcHandler {

    private static final int INVALID_PARAMS = -32602;
    private static final int TOOL_EXECUTION_ERROR = -32000;

    private final McpToolRegistry mcpToolRegistry;

    public McpToolsRpcHandler(McpToolRegistry mcpToolRegistry) {
        this.mcpToolRegistry = mcpToolRegistry;
    }

    public McpJsonRpcResponse listTools(Object id) {
        return McpJsonRpcResponse.success(id, Map.of(
                "tools", mcpToolRegistry.list()
        ));
    }

    public McpJsonRpcResponse callTool(Object id, Map<String, Object> params) {
        Object nameValue = params == null ? null : params.get("name");
        String name = nameValue == null ? null : nameValue.toString();
        if (name == null || name.isBlank()) {
            return McpJsonRpcResponse.error(id, INVALID_PARAMS, "Missing required param: name");
        }

        McpTool tool = mcpToolRegistry.get(name).orElse(null);
        if (tool == null) {
            return McpJsonRpcResponse.error(id, INVALID_PARAMS, "MCP tool not found: " + name);
        }

        McpToolRequest toolRequest = new McpToolRequest();
        toolRequest.setParams(readArguments(params));
        McpToolResponse toolResponse = tool.call(toolRequest);
        if (!toolResponse.isSuccess()) {
            return McpJsonRpcResponse.error(id, TOOL_EXECUTION_ERROR, toolResponse.getErrorMessage());
        }

        return McpJsonRpcResponse.success(id, Map.of(
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
