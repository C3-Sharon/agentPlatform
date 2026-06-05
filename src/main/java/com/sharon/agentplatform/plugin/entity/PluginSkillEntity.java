package com.sharon.agentplatform.plugin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "plugin_skill")
public class PluginSkillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plugin_id", nullable = false, length = 64)
    private String pluginId;

    @Column(name = "skill_name", nullable = false, length = 128)
    private String skillName;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "version", length = 64)
    private String version;

    @Column(name = "class_name", nullable = false, length = 500)
    private String className;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Lob
    @Column(name = "metadata_json", columnDefinition = "LONGTEXT")
    private String metadataJson;

    @Lob
    @Column(name = "market_metadata_json", columnDefinition = "LONGTEXT")
    private String marketMetadataJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPluginId() { return pluginId; }
    public void setPluginId(String pluginId) { this.pluginId = pluginId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
    public String getMarketMetadataJson() { return marketMetadataJson; }
    public void setMarketMetadataJson(String marketMetadataJson) { this.marketMetadataJson = marketMetadataJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
