package com.sharon.agentplatform.agent.history.repository;

import com.sharon.agentplatform.agent.history.entity.AgentRunTraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentRunTraceRepository extends JpaRepository<AgentRunTraceEntity, Long> {

    List<AgentRunTraceEntity> findByRunIdOrderByStepOrderAsc(String runId);

    List<AgentRunTraceEntity> findTop500ByStepOrderByCreatedAtDesc(String step);
}
