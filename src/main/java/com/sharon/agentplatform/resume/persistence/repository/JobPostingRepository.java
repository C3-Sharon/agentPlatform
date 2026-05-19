package com.sharon.agentplatform.resume.persistence.repository;

import com.sharon.agentplatform.resume.persistence.entity.JobPostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPostingEntity, Long> {

    Optional<JobPostingEntity> findFirstByJobUrlOrderByIdDesc(String jobUrl);
}
