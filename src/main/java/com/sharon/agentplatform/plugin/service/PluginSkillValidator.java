package com.sharon.agentplatform.plugin.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.plugin.manifest.PluginManifest;
import com.sharon.agentplatform.plugin.manifest.PluginManifestSkill;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillRegistry;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PluginSkillValidator {

    private final SkillRegistry skillRegistry;

    public PluginSkillValidator(SkillRegistry skillRegistry) {
        this.skillRegistry = skillRegistry;
    }

    public void validateForUpload(List<Skill> skills) {
        validateBasic(skills);
        validateManifest(null, skills);
        for (Skill skill : skills) {
            SkillMetadata metadata = skill.metadata();
            String skillName = metadata.getName();
            if (skillRegistry.getSkill(skillName).isPresent()) {
                throw new BusinessException("Skill name already exists: " + skillName);
            }
        }
    }

    public void validateForUpload(List<Skill> skills, PluginManifest manifest) {
        validateBasic(skills);
        validateManifest(manifest, skills);
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
        validateManifest(null, skills);
    }

    public void validateForBootstrap(List<Skill> skills, PluginManifest manifest) {
        validateBasic(skills);
        validateManifest(manifest, skills);
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

    private void validateManifest(PluginManifest manifest, List<Skill> skills) {
        if (manifest == null || manifest.getSkills() == null || manifest.getSkills().isEmpty()) {
            return;
        }

        Set<String> loadedSkillNames = skills.stream()
                .map(Skill::metadata)
                .map(SkillMetadata::getName)
                .collect(Collectors.toSet());

        Set<String> manifestSkillNames = new HashSet<>();
        for (PluginManifestSkill manifestSkill : manifest.getSkills()) {
            if (manifestSkill == null) {
                throw new BusinessException("Plugin manifest skill must not be null");
            }

            String skillName = manifestSkill.getName();
            if (skillName == null || skillName.isBlank()) {
                throw new BusinessException("Plugin manifest skill.name must not be blank");
            }

            if (!manifestSkillNames.add(skillName)) {
                throw new BusinessException("Duplicate skill name in plugin manifest: " + skillName);
            }

            if (!loadedSkillNames.contains(skillName)) {
                throw new BusinessException("Plugin manifest skill not found in jar: " + skillName);
            }
        }
    }
}
