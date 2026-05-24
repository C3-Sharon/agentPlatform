package com.sharon.agentplatform.plugin.repository;

import com.sharon.agentplatform.plugin.entity.PluginSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PluginSkillRepository extends JpaRepository<PluginSkillEntity, Long> {

    List<PluginSkillEntity> findByPluginId(String pluginId);

    List<PluginSkillEntity> findByEnabledTrue();

    Optional<PluginSkillEntity> findByPluginIdAndSkillName(String pluginId, String skillName);
}
