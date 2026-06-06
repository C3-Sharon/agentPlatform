package com.sharon.agentplatform.skill.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SkillMarketResponse {

    private String skillName;
    private String displayName;
    private String description;
    private String version;
    private String sourceType;
    private Boolean enabled;
    private Boolean registered;
    private String pluginId;
    private String pluginStatus;
    private Boolean runtimeLoaded;
    private String className;
    private String pluginName;
    private String pluginDisplayName;
    private String pluginAuthor;
    private String pluginHomepage;
    private String pluginLicense;
    private String pluginRepository;
    private String pluginMinPlatformVersion;
    private String manifestSchemaVersion;
    private String category;
    private List<String> tags;
    private Object examples;
    private List<String> permissions;
    private Object permissionDetails;
    private String permissionRiskLevel;
    private Object parameterSchema;
    private List<String> dependencies;
    private SkillStatsResponse stats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getPluginStatus() {
        return pluginStatus;
    }

    public void setPluginStatus(String pluginStatus) {
        this.pluginStatus = pluginStatus;
    }

    public Boolean getRuntimeLoaded() {
        return runtimeLoaded;
    }

    public void setRuntimeLoaded(Boolean runtimeLoaded) {
        this.runtimeLoaded = runtimeLoaded;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginDisplayName() {
        return pluginDisplayName;
    }

    public void setPluginDisplayName(String pluginDisplayName) {
        this.pluginDisplayName = pluginDisplayName;
    }

    public String getPluginAuthor() {
        return pluginAuthor;
    }

    public void setPluginAuthor(String pluginAuthor) {
        this.pluginAuthor = pluginAuthor;
    }

    public String getPluginHomepage() {
        return pluginHomepage;
    }

    public void setPluginHomepage(String pluginHomepage) {
        this.pluginHomepage = pluginHomepage;
    }

    public String getPluginLicense() {
        return pluginLicense;
    }

    public void setPluginLicense(String pluginLicense) {
        this.pluginLicense = pluginLicense;
    }

    public String getPluginRepository() {
        return pluginRepository;
    }

    public void setPluginRepository(String pluginRepository) {
        this.pluginRepository = pluginRepository;
    }

    public String getPluginMinPlatformVersion() {
        return pluginMinPlatformVersion;
    }

    public void setPluginMinPlatformVersion(String pluginMinPlatformVersion) {
        this.pluginMinPlatformVersion = pluginMinPlatformVersion;
    }

    public String getManifestSchemaVersion() {
        return manifestSchemaVersion;
    }

    public void setManifestSchemaVersion(String manifestSchemaVersion) {
        this.manifestSchemaVersion = manifestSchemaVersion;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Object getExamples() {
        return examples;
    }

    public void setExamples(Object examples) {
        this.examples = examples;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Object getPermissionDetails() {
        return permissionDetails;
    }

    public void setPermissionDetails(Object permissionDetails) {
        this.permissionDetails = permissionDetails;
    }

    public String getPermissionRiskLevel() {
        return permissionRiskLevel;
    }

    public void setPermissionRiskLevel(String permissionRiskLevel) {
        this.permissionRiskLevel = permissionRiskLevel;
    }

    public Object getParameterSchema() {
        return parameterSchema;
    }

    public void setParameterSchema(Object parameterSchema) {
        this.parameterSchema = parameterSchema;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public SkillStatsResponse getStats() {
        return stats;
    }

    public void setStats(SkillStatsResponse stats) {
        this.stats = stats;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
