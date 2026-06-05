package com.sharon.agentplatform.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class PluginPreviewResponse {

    private String fileName;
    private Boolean installable;
    private Object manifest;
    private List<PluginPreviewSkillResponse> skills = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getInstallable() {
        return installable;
    }

    public void setInstallable(Boolean installable) {
        this.installable = installable;
    }

    public Object getManifest() {
        return manifest;
    }

    public void setManifest(Object manifest) {
        this.manifest = manifest;
    }

    public List<PluginPreviewSkillResponse> getSkills() {
        return skills;
    }

    public void setSkills(List<PluginPreviewSkillResponse> skills) {
        this.skills = skills;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
