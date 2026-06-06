package com.sharon.agentplatform.plugin.spi;

import java.util.Map;

public interface PluginMcpClient {

    Object callTool(String toolName, Map<String, Object> arguments);
}
