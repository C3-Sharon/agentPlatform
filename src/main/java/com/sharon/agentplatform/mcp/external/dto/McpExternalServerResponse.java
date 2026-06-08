package com.sharon.agentplatform.mcp.external.dto;

import java.time.LocalDateTime;
import java.util.List;

public class McpExternalServerResponse {
    private String serverId;
    private String name;
    private String baseUrl;
    private String status;
    private Boolean enabled;
    private String errorMessage;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<McpExternalToolResponse> tools;
    private McpExternalServerHealthResponse health;
    private List<String> warnings;
    private Integer syncedToolCount;
    private List<String> syncedToolNames;
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<McpExternalToolResponse> getTools() { return tools; }
    public void setTools(List<McpExternalToolResponse> tools) { this.tools = tools; }
    public McpExternalServerHealthResponse getHealth() { return health; }
    public void setHealth(McpExternalServerHealthResponse health) { this.health = health; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    public Integer getSyncedToolCount() { return syncedToolCount; }
    public void setSyncedToolCount(Integer syncedToolCount) { this.syncedToolCount = syncedToolCount; }
    public List<String> getSyncedToolNames() { return syncedToolNames; }
    public void setSyncedToolNames(List<String> syncedToolNames) { this.syncedToolNames = syncedToolNames; }
}
