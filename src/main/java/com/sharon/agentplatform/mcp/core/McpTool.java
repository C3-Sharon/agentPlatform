package com.sharon.agentplatform.mcp.core;

public interface McpTool {

    McpToolMetadata metadata();

    McpToolResponse call(McpToolRequest request);
}
