package com.sharon.agentplatform.mcp.external.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "mcp_external_tool")
public class McpExternalToolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tool_id", nullable = false, unique = true, length = 64)
    private String toolId;
    @Column(name = "server_id", nullable = false, length = 64)
    private String serverId;
    @Column(name = "remote_name", nullable = false, length = 255)
    private String remoteName;
    @Column(name = "local_name", nullable = false, length = 500)
    private String localName;
    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Lob
    @Column(name = "input_schema_json", columnDefinition = "LONGTEXT")
    private String inputSchemaJson;
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getInputSchemaJson() { return inputSchemaJson; }
    public void setInputSchemaJson(String inputSchemaJson) { this.inputSchemaJson = inputSchemaJson; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
