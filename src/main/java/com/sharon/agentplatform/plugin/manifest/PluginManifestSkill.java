package com.sharon.agentplatform.plugin.manifest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginManifestSkill {

    private String name;
    private String category;
    private List<String> tags;
    private List<PluginManifestExample> examples;
    private List<String> permissions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<PluginManifestExample> getExamples() {
        return examples;
    }

    public void setExamples(List<PluginManifestExample> examples) {
        this.examples = examples;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
