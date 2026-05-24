package com.sharon.agentplatform.mcp.external.repository;

import com.sharon.agentplatform.mcp.external.entity.McpExternalServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface McpExternalServerRepository extends JpaRepository<McpExternalServerEntity, Long> {
    Optional<McpExternalServerEntity> findByServerId(String serverId);
    Optional<McpExternalServerEntity> findByName(String name);
    List<McpExternalServerEntity> findTop50ByOrderByCreatedAtDesc();
    List<McpExternalServerEntity> findByEnabledTrue();
}
