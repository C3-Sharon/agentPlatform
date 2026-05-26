package com.sharon.agentplatform.plugin.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillRegistry;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PluginSkillValidator {

    private final SkillRegistry skillRegistry;

    public PluginSkillValidator(SkillRegistry skillRegistry) {
        this.skillRegistry = skillRegistry;
    }

    public void validateForUpload(List<Skill> skills) {
        validateBasic(skills);
        for (Skill skill : skills) {
            SkillMetadata metadata = skill.metadata();
            String skillName = metadata.getName();
            if (skillRegistry.getSkill(skillName).isPresent()) {
                throw new BusinessException("Skill name already exists: " + skillName);
            }
        }
    }

    public void validateForBootstrap(List<Skill> skills) {
        validateBasic(skills);
    }

    private void validateBasic(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            throw new BusinessException("Plugin must contain at least one Skill");
        }

        Set<String> skillNames = new HashSet<>();
        for (Skill skill : skills) {
            if (skill == null) {
                throw new BusinessException("Skill instance must not be null");
            }

            String className = skill.getClass().getName();
            SkillMetadata metadata = skill.metadata();
            if (metadata == null) {
                throw new BusinessException("Skill metadata must not be null: " + className);
            }

            String skillName = metadata.getName();
            if (skillName == null || skillName.isBlank()) {
                throw new BusinessException("Skill metadata.name must not be blank: " + className);
            }

            if (metadata.getDescription() == null || metadata.getDescription().isBlank()) {
                throw new BusinessException("Skill metadata.description must not be blank: " + skillName);
            }

            if (metadata.getVersion() == null || metadata.getVersion().isBlank()) {
                throw new BusinessException("Skill metadata.version must not be blank: " + skillName);
            }

            if (metadata.getParameterSchema() == null) {
                throw new BusinessException("Skill metadata.parameterSchema must not be null: " + skillName);
            }

            if (!skillNames.add(skillName)) {
                throw new BusinessException("Duplicate skill name in plugin jar: " + skillName);
            }
        }
    }
}
