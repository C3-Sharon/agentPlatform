package com.sharon.agentplatform.plugin.permission;

public class PluginPermissionDescriptor {

    private String name;
    private String displayName;
    private String description;
    private String riskLevel;
    private Boolean known;

    public PluginPermissionDescriptor() {
    }

    public PluginPermissionDescriptor(String name, String displayName, String description, String riskLevel, Boolean known) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.riskLevel = riskLevel;
        this.known = known;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Boolean getKnown() {
        return known;
    }

    public void setKnown(Boolean known) {
        this.known = known;
    }
}
