package com.sharon.agentplatform.plugin.dto;

import com.sharon.agentplatform.plugin.core.LoadedPluginSkill;

import java.util.List;

public class PluginUploadResponse {

    private List<LoadedPluginSkill> loadedSkills;

    public List<LoadedPluginSkill> getLoadedSkills() {
        return loadedSkills;
    }

    public void setLoadedSkills(List<LoadedPluginSkill> loadedSkills) {
        this.loadedSkills = loadedSkills;
    }
}
