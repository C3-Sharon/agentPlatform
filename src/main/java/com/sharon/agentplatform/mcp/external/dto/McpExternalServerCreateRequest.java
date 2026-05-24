package com.sharon.agentplatform.mcp.external.dto;

public class McpExternalServerCreateRequest {
    private String name;
    private String baseUrl;
    private Boolean enabled;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
