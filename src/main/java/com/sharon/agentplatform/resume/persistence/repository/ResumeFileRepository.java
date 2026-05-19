package com.sharon.agentplatform.resume.persistence.repository;

import com.sharon.agentplatform.resume.persistence.entity.ResumeFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeFileRepository extends JpaRepository<ResumeFileEntity, Long> {

    Optional<ResumeFileEntity> findByFileId(String fileId);
}
