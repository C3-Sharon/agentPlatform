package com.sharon.agentplatform.skill.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.plugin.repository.PluginSkillRepository;
import com.sharon.agentplatform.skill.core.SkillRegistry;
import com.sharon.agentplatform.skill.dto.SkillEnableResponse;
import com.sharon.agentplatform.skill.entity.SkillSettingEntity;
import com.sharon.agentplatform.skill.repository.SkillSettingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SkillSettingService {

    private static final String SOURCE_TYPE_BUILTIN = "BUILTIN";
    private static final String SOURCE_TYPE_PLUGIN = "PLUGIN";

    private final SkillSettingRepository skillSettingRepository;
    private final PluginSkillRepository pluginSkillRepository;
    private final SkillRegistry skillRegistry;

    public SkillSettingService(SkillSettingRepository skillSettingRepository,
                               PluginSkillRepository pluginSkillRepository,
                               SkillRegistry skillRegistry) {
        this.skillSettingRepository = skillSettingRepository;
        this.pluginSkillRepository = pluginSkillRepository;
        this.skillRegistry = skillRegistry;
    }

    public boolean isEnabled(String skillName) {
        return skillSettingRepository.findBySkillName(skillName)
                .map(SkillSettingEntity::getEnabled)
                .orElse(true);
    }

    public SkillEnableResponse enableSkill(String skillName) {
        if (skillRegistry.getSkill(skillName).isEmpty()) {
            throw new BusinessException("Skill not found or plugin disabled: " + skillName);
        }

        return upsert(skillName, true);
    }

    public SkillEnableResponse disableSkill(String skillName) {
        if (skillRegistry.getSkill(skillName).isEmpty()) {
            throw new BusinessException("Skill not found: " + skillName);
        }

        return upsert(skillName, false);
    }

    public String detectSourceType(String skillName) {
        if (pluginSkillRepository.existsBySkillName(skillName)) {
            return SOURCE_TYPE_PLUGIN;
        }
        return SOURCE_TYPE_BUILTIN;
    }

    private SkillEnableResponse upsert(String skillName, boolean enabled) {
        LocalDateTime now = LocalDateTime.now();
        SkillSettingEntity entity = skillSettingRepository.findBySkillName(skillName)
                .orElseGet(() -> {
                    SkillSettingEntity created = new SkillSettingEntity();
                    created.setSkillName(skillName);
                    created.setCreatedAt(now);
                    return created;
                });
        entity.setEnabled(enabled);
        entity.setSourceType(detectSourceType(skillName));
        entity.setUpdatedAt(now);

        return toResponse(skillSettingRepository.save(entity));
    }

    private SkillEnableResponse toResponse(SkillSettingEntity entity) {
        SkillEnableResponse response = new SkillEnableResponse();
        response.setSkillName(entity.getSkillName());
        response.setEnabled(entity.getEnabled());
        response.setSourceType(entity.getSourceType());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
