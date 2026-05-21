package com.sharon.agentplatform.resume.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.resume.dto.JobPostingReadRequest;
import com.sharon.agentplatform.resume.dto.JobPostingReadResponse;
import com.sharon.agentplatform.resume.persistence.entity.JobPostingEntity;
import com.sharon.agentplatform.resume.persistence.repository.JobPostingRepository;
import com.sharon.agentplatform.resume.web.JobPageReader;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class JobPostingService {

    private static final int PREVIEW_LENGTH = 500;

    private final JobPostingRepository jobPostingRepository;
    private final JobPageReader jobPageReader;

    public JobPostingService(JobPostingRepository jobPostingRepository, JobPageReader jobPageReader) {
        this.jobPostingRepository = jobPostingRepository;
        this.jobPageReader = jobPageReader;
    }

    public JobPostingReadResponse readAndSave(JobPostingReadRequest request) {
        if (request == null || request.getJobUrl() == null || request.getJobUrl().isBlank()) {
            throw new BusinessException("Job url is required");
        }

        String jobUrl = request.getJobUrl().trim();
        if (!jobUrl.startsWith("http://") && !jobUrl.startsWith("https://")) {
            throw new BusinessException("Job url must start with http:// or https://");
        }

        JobPageReader.JobPageReadResult readResult = jobPageReader.read(jobUrl);
        LocalDateTime now = LocalDateTime.now();

        JobPostingEntity entity = new JobPostingEntity();
        entity.setJobUrl(readResult.getUrl());
        entity.setPageTitle(readResult.getTitle());
        entity.setRawText(readResult.getText());
        entity.setRequirementSummary(null);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        JobPostingEntity savedEntity = jobPostingRepository.save(entity);
        return toResponse(savedEntity);
    }

    private JobPostingReadResponse toResponse(JobPostingEntity entity) {
        String rawText = entity.getRawText();

        JobPostingReadResponse response = new JobPostingReadResponse();
        response.setJobPostingId(entity.getId());
        response.setJobUrl(entity.getJobUrl());
        response.setPageTitle(entity.getPageTitle());
        response.setTextLength(rawText == null ? 0 : rawText.length());
        response.setPreview(createPreview(rawText));
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    private String createPreview(String rawText) {
        if (rawText == null || rawText.length() <= PREVIEW_LENGTH) {
            return rawText;
        }

        return rawText.substring(0, PREVIEW_LENGTH);
    }
}
