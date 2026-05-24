package com.sharon.agentplatform.agent.history.repository;

import com.sharon.agentplatform.agent.history.entity.AgentRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentRunRepository extends JpaRepository<AgentRunEntity, Long> {

    Optional<AgentRunEntity> findByRunId(String runId);

    List<AgentRunEntity> findTop50ByOrderByCreatedAtDesc();
}
