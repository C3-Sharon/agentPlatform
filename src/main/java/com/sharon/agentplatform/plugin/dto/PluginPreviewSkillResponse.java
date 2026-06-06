package com.sharon.agentplatform.plugin.dto;

import java.util.List;

public class PluginPreviewSkillResponse {

    private String skillName;
    private String displayName;
    private String description;
    private String version;
    private String className;
    private Object parameterSchema;
    private List<String> dependencies;
    private Object marketMetadata;
    private Object permissionDetails;
    private String permissionRiskLevel;
    private Boolean conflict;
    private String conflictMessage;

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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public Object getMarketMetadata() {
        return marketMetadata;
    }

    public void setMarketMetadata(Object marketMetadata) {
        this.marketMetadata = marketMetadata;
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

    public Boolean getConflict() {
        return conflict;
    }

    public void setConflict(Boolean conflict) {
        this.conflict = conflict;
    }

    public String getConflictMessage() {
        return conflictMessage;
    }

    public void setConflictMessage(String conflictMessage) {
        this.conflictMessage = conflictMessage;
    }
}
