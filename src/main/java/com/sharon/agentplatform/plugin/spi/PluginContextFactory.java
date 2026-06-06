package com.sharon.agentplatform.plugin.spi;

import com.sharon.agentplatform.mcp.core.McpToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class PluginContextFactory {

    private static final Logger log = LoggerFactory.getLogger(PluginContextFactory.class);

    private final String platformVersion;
    private final McpToolRegistry mcpToolRegistry;

    public PluginContextFactory(@Value("${agentplatform.platform-version:0.0.1}") String platformVersion,
                                McpToolRegistry mcpToolRegistry) {
        this.platformVersion = platformVersion;
        this.mcpToolRegistry = mcpToolRegistry;
    }

    public PluginContext create(String pluginId, Path jarPath) {
        return create(pluginId, jarPath, List.of());
    }

    public PluginContext create(String pluginId, Path jarPath, List<String> permissions) {
        DefaultPluginContext context = new DefaultPluginContext();
        context.setPluginId(pluginId);
        context.setJarPath(jarPath == null ? null : jarPath.toString());
        context.setPlatformVersion(platformVersion);
        context.setLogger(new Slf4jPluginLogger(pluginId));
        context.setPermissions(permissions);
        context.setMcpClient(new DefaultPluginMcpClient(pluginId, context, mcpToolRegistry));
        return context;
    }

    private static class Slf4jPluginLogger implements PluginLogger {

        private final String pluginId;

        private Slf4jPluginLogger(String pluginId) {
            this.pluginId = pluginId;
        }

        public void info(String message) {
            log.info("[pluginId={}] {}", pluginId, message);
        }

        public void warn(String message) {
            log.warn("[pluginId={}] {}", pluginId, message);
        }

        public void error(String message) {
            log.error("[pluginId={}] {}", pluginId, message);
        }
    }
}
