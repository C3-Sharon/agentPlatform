package com.sharon.agentplatform.plugin.spi;

import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolRegistry;
import com.sharon.agentplatform.mcp.core.McpToolRequest;
import com.sharon.agentplatform.mcp.core.McpToolResponse;

import java.util.HashMap;
import java.util.Map;

public class DefaultPluginMcpClient implements PluginMcpClient {

    private static final String MCP_CALL_PERMISSION = "mcp:call";

    private final String pluginId;
    private final PluginContext permissionContext;
    private final McpToolRegistry mcpToolRegistry;

    public DefaultPluginMcpClient(String pluginId,
                                  PluginContext permissionContext,
                                  McpToolRegistry mcpToolRegistry) {
        this.pluginId = pluginId;
        this.permissionContext = permissionContext;
        this.mcpToolRegistry = mcpToolRegistry;
    }

    public Object callTool(String toolName, Map<String, Object> arguments) {
        permissionContext.requirePermission(MCP_CALL_PERMISSION);
        if (toolName == null || toolName.isBlank()) {
            throw new PluginMcpException("MCP tool name is required");
        }

        McpTool tool = mcpToolRegistry.get(toolName)
                .orElseThrow(() -> new PluginMcpException("MCP tool not found: " + toolName));

        McpToolRequest request = new McpToolRequest();
        request.setParams(arguments == null ? new HashMap<>() : new HashMap<>(arguments));
        McpToolResponse response = tool.call(request);
        if (response == null) {
            throw new PluginMcpException("MCP tool returned null response: " + toolName);
        }
        if (!response.isSuccess()) {
            throw new PluginMcpException("MCP tool execution failed for " + toolName + ": " + response.getErrorMessage());
        }
        return response.getResult();
    }

    public String getPluginId() {
        return pluginId;
    }
}
