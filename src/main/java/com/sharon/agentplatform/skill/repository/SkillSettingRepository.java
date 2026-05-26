package com.sharon.agentplatform.skill.repository;

import com.sharon.agentplatform.skill.entity.SkillSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillSettingRepository extends JpaRepository<SkillSettingEntity, Long> {

    Optional<SkillSettingEntity> findBySkillName(String skillName);

    List<SkillSettingEntity> findByEnabledFalse();
}
