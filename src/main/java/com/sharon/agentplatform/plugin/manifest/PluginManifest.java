package com.sharon.agentplatform.plugin.manifest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginManifest {

    private PluginManifestInfo plugin;
    private List<PluginManifestSkill> skills;

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
