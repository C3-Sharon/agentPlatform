package com.sharon.agentplatform.mcp.external.repository;

import com.sharon.agentplatform.mcp.external.entity.McpExternalToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface McpExternalToolRepository extends JpaRepository<McpExternalToolEntity, Long> {
    Optional<McpExternalToolEntity> findByToolId(String toolId);
    Optional<McpExternalToolEntity> findByServerIdAndRemoteName(String serverId, String remoteName);
    List<McpExternalToolEntity> findByServerIdOrderByCreatedAtAsc(String serverId);
    List<McpExternalToolEntity> findByEnabledTrue();
}
