package com.sharon.agentplatform.resume.persistence.repository;

import com.sharon.agentplatform.resume.persistence.entity.ResumeAnalysisTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeAnalysisTaskRepository extends JpaRepository<ResumeAnalysisTaskEntity, Long> {

    List<ResumeAnalysisTaskEntity> findByConversationIdOrderByCreatedAtDesc(String conversationId);
}
