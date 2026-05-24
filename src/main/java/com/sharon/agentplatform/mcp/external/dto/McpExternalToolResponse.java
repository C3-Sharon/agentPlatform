package com.sharon.agentplatform.mcp.external.dto;

import java.time.LocalDateTime;

public class McpExternalToolResponse {
    private String toolId;
    private String serverId;
    private String remoteName;
    private String localName;
    private String description;
    private Object inputSchema;
    private Boolean enabled;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public String getToolId() { return toolId; }
    public void setToolId(String toolId) { this.toolId = toolId; }
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public String getRemoteName() { return remoteName; }
    public void setRemoteName(String remoteName) { this.remoteName = remoteName; }
    public String getLocalName() { return localName; }
    public void setLocalName(String localName) { this.localName = localName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Object getInputSchema() { return inputSchema; }
    public void setInputSchema(Object inputSchema) { this.inputSchema = inputSchema; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
