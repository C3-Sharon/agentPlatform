package com.sharon.agentplatform.mcp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class McpToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpToolRegistry.class);

    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();

    public McpToolRegistry(List<McpTool> mcpTools) {
        for (McpTool tool : mcpTools) {
            register(tool);
        }
    }

    public List<McpToolMetadata> list() {
        return new ArrayList<>(tools.values()).stream()
                .map(McpTool::metadata)
                .sorted(Comparator.comparing(McpToolMetadata::getName))
                .toList();
    }

    public Optional<McpTool> get(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(tools.get(name));
    }

    public void register(McpTool tool) {
        if (tool == null || tool.metadata() == null || tool.metadata().getName() == null || tool.metadata().getName().isBlank()) {
            throw new IllegalArgumentException("MCP tool name is required");
        }

        String name = tool.metadata().getName();
        McpTool previous = tools.put(name, tool);
        if (previous == null) {
            log.info("Registered MCP tool: {}", name);
        } else {
            log.warn("Overriding MCP tool: {}", name);
        }
    }
}
