package com.sharon.agentplatform.resume.persistence.repository;

import com.sharon.agentplatform.resume.persistence.entity.ResumeOptimizationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeOptimizationResultRepository extends JpaRepository<ResumeOptimizationResultEntity, Long> {

    Optional<ResumeOptimizationResultEntity> findFirstByTaskIdOrderByIdDesc(Long taskId);
}
