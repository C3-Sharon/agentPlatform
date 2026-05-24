package com.sharon.agentplatform.mcp.core;

import java.util.List;
import java.util.Map;

public interface McpClient {

    List<McpToolMetadata> listTools();

    McpToolResult callTool(String toolName, Map<String, Object> arguments);
}
