package com.sharon.agentplatform.resume.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.resume.dto.ResumeAnalysisTaskStatusResponse;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeAsyncResponse;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeRequest;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeResponse;
import com.sharon.agentplatform.resume.persistence.ResumeAnalysisStatus;
import com.sharon.agentplatform.resume.persistence.entity.JobPostingEntity;
import com.sharon.agentplatform.resume.persistence.entity.ResumeAnalysisTaskEntity;
import com.sharon.agentplatform.resume.persistence.entity.ResumeFileEntity;
import com.sharon.agentplatform.resume.persistence.entity.ResumeOptimizationResultEntity;
import com.sharon.agentplatform.resume.persistence.repository.JobPostingRepository;
import com.sharon.agentplatform.resume.persistence.repository.ResumeAnalysisTaskRepository;
import com.sharon.agentplatform.resume.persistence.repository.ResumeFileRepository;
import com.sharon.agentplatform.resume.persistence.repository.ResumeOptimizationResultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ResumeOptimizeService {

    private static final String DEFAULT_MODEL_ID = "siliconflow-qwen";

    private final ResumeFileRepository resumeFileRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeAnalysisTaskRepository resumeAnalysisTaskRepository;
    private final ResumeOptimizationResultRepository resumeOptimizationResultRepository;
    private final ResumeOptimizeTaskRunner resumeOptimizeTaskRunner;
    private final ResumeOptimizeAsyncExecutor resumeOptimizeAsyncExecutor;

    public ResumeOptimizeService(ResumeFileRepository resumeFileRepository,
                                 JobPostingRepository jobPostingRepository,
                                 ResumeAnalysisTaskRepository resumeAnalysisTaskRepository,
                                 ResumeOptimizationResultRepository resumeOptimizationResultRepository,
                                 ResumeOptimizeTaskRunner resumeOptimizeTaskRunner,
                                 ResumeOptimizeAsyncExecutor resumeOptimizeAsyncExecutor) {
        this.resumeFileRepository = resumeFileRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.resumeAnalysisTaskRepository = resumeAnalysisTaskRepository;
        this.resumeOptimizationResultRepository = resumeOptimizationResultRepository;
        this.resumeOptimizeTaskRunner = resumeOptimizeTaskRunner;
        this.resumeOptimizeAsyncExecutor = resumeOptimizeAsyncExecutor;
    }

    public ResumeOptimizeResponse optimize(ResumeOptimizeRequest request) {
        validateRequest(request);

        String modelId = getModelId(request);
        ResumeFileEntity resumeFile = findReadyResumeFile(request);
        validateReadyJobPosting(request);
        ResumeAnalysisTaskEntity task = createTask(request, modelId, resumeFile);

        return resumeOptimizeTaskRunner.runExistingTask(task.getId());
    }

    public ResumeOptimizeAsyncResponse submitAsync(ResumeOptimizeRequest request) {
        validateRequest(request);

        String modelId = getModelId(request);
        ResumeFileEntity resumeFile = findReadyResumeFile(request);
        validateReadyJobPosting(request);
        ResumeAnalysisTaskEntity task = createTask(request, modelId, resumeFile);

        resumeOptimizeAsyncExecutor.execute(task.getId());

        ResumeOptimizeAsyncResponse response = new ResumeOptimizeAsyncResponse();
        response.setTaskId(task.getId());
        response.setStatus(task.getStatus());
        response.setMessage("Resume optimize task submitted");
        return response;
    }

    public ResumeAnalysisTaskStatusResponse getTaskStatus(Long taskId) {
        ResumeAnalysisTaskEntity task = resumeAnalysisTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Resume analysis task not found: " + taskId));

        ResumeAnalysisTaskStatusResponse response = new ResumeAnalysisTaskStatusResponse();
        response.setTaskId(task.getId());
        response.setStatus(task.getStatus());
        response.setErrorMessage(task.getErrorMessage());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        if (ResumeAnalysisStatus.SUCCESS.name().equals(task.getStatus())) {
            ResumeOptimizationResultEntity result = resumeOptimizationResultRepository.findFirstByTaskIdOrderByIdDesc(task.getId())
                    .orElseThrow(() -> new BusinessException("Resume optimization result not found for task: " + task.getId()));
            response.setResult(toOptimizeResponse(result));
        }

        return response;
    }

    private void validateRequest(ResumeOptimizeRequest request) {
        if (request == null) {
            throw new BusinessException("Resume optimize request is required");
        }

        if (request.getResumeFileId() == null || request.getResumeFileId().isBlank()) {
            throw new BusinessException("Resume file id is required");
        }

        if (request.getJobPostingId() == null) {
            throw new BusinessException("Job posting id is required");
        }
    }

    private String getModelId(ResumeOptimizeRequest request) {
        if (request.getModelId() == null || request.getModelId().isBlank()) {
            return DEFAULT_MODEL_ID;
        }

        return request.getModelId().trim();
    }

    private ResumeFileEntity findReadyResumeFile(ResumeOptimizeRequest request) {
        ResumeFileEntity resumeFile = resumeFileRepository.findByFileId(request.getResumeFileId())
                .orElseThrow(() -> new BusinessException("Resume file not found: " + request.getResumeFileId()));
        if (resumeFile.getParsedText() == null || resumeFile.getParsedText().isBlank()) {
            throw new BusinessException("Resume file has not been parsed: " + request.getResumeFileId());
        }

        return resumeFile;
    }

    private void validateReadyJobPosting(ResumeOptimizeRequest request) {
        JobPostingEntity jobPosting = jobPostingRepository.findById(request.getJobPostingId())
                .orElseThrow(() -> new BusinessException("Job posting not found: " + request.getJobPostingId()));
        if (jobPosting.getRawText() == null || jobPosting.getRawText().isBlank()) {
            throw new BusinessException("Job posting text is empty: " + request.getJobPostingId());
        }
    }

    private ResumeAnalysisTaskEntity createTask(ResumeOptimizeRequest request, String modelId, ResumeFileEntity resumeFile) {
        LocalDateTime now = LocalDateTime.now();

        ResumeAnalysisTaskEntity task = new ResumeAnalysisTaskEntity();
        task.setConversationId(request.getConversationId());
        task.setModelId(modelId);
        task.setResumeFileId(resumeFile.getId());
        task.setJobPostingId(request.getJobPostingId());
        task.setStatus(ResumeAnalysisStatus.RUNNING.name());
        task.setErrorMessage(null);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return resumeAnalysisTaskRepository.save(task);
    }

    private ResumeOptimizeResponse toOptimizeResponse(ResumeOptimizationResultEntity result) {
        ResumeOptimizeResponse response = new ResumeOptimizeResponse();
        response.setTaskId(result.getTaskId());
        response.setResultId(result.getId());
        response.setJobRequirementSummary(result.getJobRequirementSummary());
        response.setMatchAnalysis(result.getMatchAnalysis());
        response.setOptimizationSuggestions(result.getOptimizationSuggestions());
        response.setOptimizedResume(result.getOptimizedResume());
        response.setInterviewSuggestions(result.getInterviewSuggestions());
        response.setRawModelResponse(result.getRawModelResponse());
        return response;
    }
}
