package com.sharon.agentplatform.mcp.rpc.handler;

import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class McpLifecycleRpcHandler {

    private static final String MCP_PROTOCOL_VERSION = "2024-11-05";
    private static final String MCP_SERVER_NAME = "agent-platform-mcp";

    private final String platformVersion;

    public McpLifecycleRpcHandler(@Value("${agentplatform.platform-version:0.0.1}") String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public McpJsonRpcResponse initialize(Object id) {
        return McpJsonRpcResponse.success(id, Map.of(
                "protocolVersion", MCP_PROTOCOL_VERSION,
                "serverInfo", Map.of(
                        "name", MCP_SERVER_NAME,
                        "version", platformVersion
                ),
                "capabilities", Map.of(
                        "tools", Map.of(),
                        "resources", Map.of(),
                        "prompts", Map.of()
                )
        ));
    }

    public McpJsonRpcResponse ping(Object id) {
        return McpJsonRpcResponse.success(id, Map.of(
                "pong", true,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
