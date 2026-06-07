package com.sharon.agentplatform.mcp.rpc.service;

import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcRequest;
import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcResponse;
import com.sharon.agentplatform.mcp.rpc.handler.McpLifecycleRpcHandler;
import com.sharon.agentplatform.mcp.rpc.handler.McpPromptsRpcHandler;
import com.sharon.agentplatform.mcp.rpc.handler.McpResourcesRpcHandler;
import com.sharon.agentplatform.mcp.rpc.handler.McpToolsRpcHandler;
import org.springframework.stereotype.Service;

@Service
public class McpJsonRpcService {

    private static final String JSON_RPC_VERSION = "2.0";
    private static final int INVALID_REQUEST = -32600;
    private static final int METHOD_NOT_FOUND = -32601;

    private final McpLifecycleRpcHandler lifecycleRpcHandler;
    private final McpToolsRpcHandler toolsRpcHandler;
    private final McpResourcesRpcHandler resourcesRpcHandler;
    private final McpPromptsRpcHandler promptsRpcHandler;

    public McpJsonRpcService(McpLifecycleRpcHandler lifecycleRpcHandler,
                             McpToolsRpcHandler toolsRpcHandler,
                             McpResourcesRpcHandler resourcesRpcHandler,
                             McpPromptsRpcHandler promptsRpcHandler) {
        this.lifecycleRpcHandler = lifecycleRpcHandler;
        this.toolsRpcHandler = toolsRpcHandler;
        this.resourcesRpcHandler = resourcesRpcHandler;
        this.promptsRpcHandler = promptsRpcHandler;
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
            case "initialize" -> lifecycleRpcHandler.initialize(request.getId());
            case "ping" -> lifecycleRpcHandler.ping(request.getId());
            case "tools/list" -> toolsRpcHandler.listTools(request.getId());
            case "tools/call" -> toolsRpcHandler.callTool(request.getId(), request.getParams());
            case "resources/list" -> resourcesRpcHandler.listResources(request.getId());
            case "resources/read" -> resourcesRpcHandler.readResource(request);
            case "prompts/list" -> promptsRpcHandler.listPrompts(request.getId());
            case "prompts/get" -> promptsRpcHandler.getPrompt(request.getId(), request.getParams());
            default -> McpJsonRpcResponse.error(request.getId(), METHOD_NOT_FOUND, "Method not found: " + request.getMethod());
        };
    }
}
