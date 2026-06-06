package com.sharon.agentplatform.skill.dto;

public class SkillMarketQuery {

    private String sourceType;
    private String category;
    private String tag;
    private Boolean enabled;
    private Boolean registered;
    private Boolean runtimeLoaded;
    private String pluginStatus;
    private String permissionRiskLevel;
    private String keyword;

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public Boolean getRuntimeLoaded() {
        return runtimeLoaded;
    }

    public void setRuntimeLoaded(Boolean runtimeLoaded) {
        this.runtimeLoaded = runtimeLoaded;
    }

    public String getPluginStatus() {
        return pluginStatus;
    }

    public void setPluginStatus(String pluginStatus) {
        this.pluginStatus = pluginStatus;
    }

    public String getPermissionRiskLevel() {
        return permissionRiskLevel;
    }

    public void setPermissionRiskLevel(String permissionRiskLevel) {
        this.permissionRiskLevel = permissionRiskLevel;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
