package com.sharon.agentplatform.skill.core;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class SkillMetadata {

    private String name;
    private String displayName;
    private String description;
    private String version;
    private Map<String, Object> parameterSchema;
    private List<String> dependencies;

    public SkillMetadata() {
    }

    public SkillMetadata(
            String name,
            String displayName,
            String description,
            String version,
            Map<String, Object> parameterSchema,
            List<String> dependencies
    ) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.version = version;
        this.parameterSchema = parameterSchema;
        this.dependencies = dependencies;
    }


}