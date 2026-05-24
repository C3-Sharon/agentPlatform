package com.sharon.agentplatform.plugin.repository;

import com.sharon.agentplatform.plugin.entity.PluginPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PluginPackageRepository extends JpaRepository<PluginPackageEntity, Long> {

    Optional<PluginPackageEntity> findByPluginId(String pluginId);

    List<PluginPackageEntity> findTop50ByOrderByCreatedAtDesc();

    List<PluginPackageEntity> findByStatus(String status);
}
