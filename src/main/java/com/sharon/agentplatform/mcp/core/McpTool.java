package com.sharon.agentplatform.mcp.core;

import java.util.Map;

public class McpTool {

    private String name;
    private String title;
    private String description;
    private Map<String, Object> inputSchema;

    public McpTool() {
    }

    public McpTool(
            String name,
            String title,
            String description,
            Map<String, Object> inputSchema
    ) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInputSchema(Map<String, Object> inputSchema) {
        this.inputSchema = inputSchema;
    }
}
