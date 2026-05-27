package com.sharon.agentplatform.plugin.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PluginRuntimeResponse {

    private String pluginId;
    private String jarPath;
    private List<String> skillNames;
    private String classLoaderType;
    private LocalDateTime loadedAt;
    private Boolean closed;

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public List<String> getSkillNames() {
        return skillNames;
    }

    public void setSkillNames(List<String> skillNames) {
        this.skillNames = skillNames;
    }

    public String getClassLoaderType() {
        return classLoaderType;
    }

    public void setClassLoaderType(String classLoaderType) {
        this.classLoaderType = classLoaderType;
    }

    public LocalDateTime getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(LocalDateTime loadedAt) {
        this.loadedAt = loadedAt;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }
}
