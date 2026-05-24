package com.sharon.agentplatform.plugin.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PluginPackageResponse {

    private String pluginId;
    private String fileName;
    private String jarPath;
    private String status;
    private String errorMessage;
    private LocalDateTime uploadedAt;
    private LocalDateTime loadedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PluginSkillResponse> skills;

    public String getPluginId() { return pluginId; }
    public void setPluginId(String pluginId) { this.pluginId = pluginId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getJarPath() { return jarPath; }
    public void setJarPath(String jarPath) { this.jarPath = jarPath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public LocalDateTime getLoadedAt() { return loadedAt; }
    public void setLoadedAt(LocalDateTime loadedAt) { this.loadedAt = loadedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<PluginSkillResponse> getSkills() { return skills; }
    public void setSkills(List<PluginSkillResponse> skills) { this.skills = skills; }
}
