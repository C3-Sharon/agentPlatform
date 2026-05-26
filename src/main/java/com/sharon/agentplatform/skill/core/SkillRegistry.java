package com.sharon.agentplatform.skill.core;

import com.sharon.agentplatform.skill.entity.SkillSettingEntity;
import com.sharon.agentplatform.skill.repository.SkillSettingRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SkillRegistry {

    private static final Logger log = LoggerFactory.getLogger(SkillRegistry.class);

    private final Collection<Skill> skillBeans;

    private final SkillSettingRepository skillSettingRepository;

    private final Map<String, Skill> skills = new ConcurrentHashMap<>();

    private final Map<String, Boolean> enabledStatus = new ConcurrentHashMap<>();

    public SkillRegistry(Collection<Skill> skillBeans, SkillSettingRepository skillSettingRepository) {
        this.skillBeans = skillBeans;
        this.skillSettingRepository = skillSettingRepository;
    }

    @PostConstruct
    public void init() {
        for (Skill skill : skillBeans) {
            register(skill);
        }
    }

    public void register(Skill skill) {
        String name = skill.metadata().getName();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Skill name must not be blank");
        }

        if (skills.containsKey(name)) {
            log.info("Overwrite existing skill registration: {}", name);
        }

        skills.put(name, skill);
        enabledStatus.put(name, true);
    }

    public void unregister(String skillName) {
        if (skillName == null || skillName.isBlank()) {
            return;
        }

        Skill removed = skills.remove(skillName);
        enabledStatus.remove(skillName);
        if (removed != null) {
            log.info("Unregistered skill: {}", skillName);
        }
    }

    public Collection<Skill> listAll() {
        return Collections.unmodifiableCollection(new ArrayList<>(skills.values()));
    }

    public Collection<Skill> listEnabled() {
        return skills.values()
                .stream()
                .filter(skill -> isEnabled(skill.metadata().getName()))
                .toList();
    }

    public Optional<Skill> getSkill(String name) {
        return Optional.ofNullable(skills.get(name));
    }

    public boolean isEnabled(String name) {
        if (!enabledStatus.getOrDefault(name, false)) {
            return false;
        }

        return skillSettingRepository.findBySkillName(name)
                .map(SkillSettingEntity::getEnabled)
                .orElse(true);
    }

    public boolean enable(String name) {
        if (!skills.containsKey(name)) {
            return false;
        }
        enabledStatus.put(name, true);
        return true;
    }

    public boolean disable(String name) {
        if (!skills.containsKey(name)) {
            return false;
        }
        enabledStatus.put(name, false);
        return true;
    }

    public SkillResult call(String name, SkillContext context) {
        Skill skill = skills.get(name);

        if (skill == null) {
            return SkillResult.fail("Skill not found: " + name);
        }

        if (!isEnabled(name)) {
            return SkillResult.fail("Skill is disabled: " + name);
        }

        try {
            return skill.execute(context);
        } catch (Exception e) {
            return SkillResult.fail("Skill execution failed: " + e.getMessage());
        }
    }
}
