package com.sharon.agentplatform.plugin.manifest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginManifest {

    private String schemaVersion;
    private PluginManifestInfo plugin;
    private List<PluginManifestSkill> skills;

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public PluginManifestInfo getPlugin() {
        return plugin;
    }

    public void setPlugin(PluginManifestInfo plugin) {
        this.plugin = plugin;
    }

    public List<PluginManifestSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<PluginManifestSkill> skills) {
        this.skills = skills;
    }
}
