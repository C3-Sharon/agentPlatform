package com.sharon.agentplatform.resume.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.model.service.ModelService;
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
import com.sharon.agentplatform.resume.prompt.ResumePromptBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ResumeOptimizeService {

    private static final String DEFAULT_MODEL_ID = "siliconflow-qwen";
    private static final String SYSTEM_PROMPT = "你是一名专业的校园招聘简历优化顾问，擅长根据岗位要求优化中文简历，并给出面试准备建议。";

    private final ResumeFileRepository resumeFileRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeAnalysisTaskRepository resumeAnalysisTaskRepository;
    private final ResumeOptimizationResultRepository resumeOptimizationResultRepository;
    private final ResumePromptBuilder resumePromptBuilder;
    private final ResumeOptimizeResultParser resumeOptimizeResultParser;
    private final ModelService modelService;

    public ResumeOptimizeService(ResumeFileRepository resumeFileRepository,
                                 JobPostingRepository jobPostingRepository,
                                 ResumeAnalysisTaskRepository resumeAnalysisTaskRepository,
                                 ResumeOptimizationResultRepository resumeOptimizationResultRepository,
                                 ResumePromptBuilder resumePromptBuilder,
                                 ResumeOptimizeResultParser resumeOptimizeResultParser,
                                 ModelService modelService) {
        this.resumeFileRepository = resumeFileRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.resumeAnalysisTaskRepository = resumeAnalysisTaskRepository;
        this.resumeOptimizationResultRepository = resumeOptimizationResultRepository;
        this.resumePromptBuilder = resumePromptBuilder;
        this.resumeOptimizeResultParser = resumeOptimizeResultParser;
        this.modelService = modelService;
    }

    public ResumeOptimizeResponse optimize(ResumeOptimizeRequest request) {
        ResumeAnalysisTaskEntity task = null;

        try {
            validateRequest(request);

            String modelId = getModelId(request);
            ResumeFileEntity resumeFile = resumeFileRepository.findByFileId(request.getResumeFileId())
                    .orElseThrow(() -> new BusinessException("Resume file not found: " + request.getResumeFileId()));
            if (resumeFile.getParsedText() == null || resumeFile.getParsedText().isBlank()) {
                throw new BusinessException("Resume file has not been parsed: " + request.getResumeFileId());
            }

            JobPostingEntity jobPosting = jobPostingRepository.findById(request.getJobPostingId())
                    .orElseThrow(() -> new BusinessException("Job posting not found: " + request.getJobPostingId()));
            if (jobPosting.getRawText() == null || jobPosting.getRawText().isBlank()) {
                throw new BusinessException("Job posting text is empty: " + request.getJobPostingId());
            }

            task = createTask(request, modelId, resumeFile);

            String userPrompt = resumePromptBuilder.buildOptimizePrompt(jobPosting.getRawText(), resumeFile.getParsedText());
            String rawModelResponse = modelService.chatWithContext(modelId, SYSTEM_PROMPT, userPrompt);
            if (rawModelResponse == null || rawModelResponse.isBlank()) {
                throw new BusinessException("Resume optimize model response is empty");
            }

            ResumeOptimizeResponse parsedResponse = resumeOptimizeResultParser.parse(task.getId(), null, rawModelResponse);

            ResumeOptimizationResultEntity result = createResult(task.getId(), parsedResponse, rawModelResponse);
            parsedResponse.setResultId(result.getId());

            task.setStatus(ResumeAnalysisStatus.SUCCESS.name());
            task.setUpdatedAt(LocalDateTime.now());
            resumeAnalysisTaskRepository.save(task);

            return parsedResponse;
        } catch (RuntimeException exception) {
            markTaskFailed(task, exception);
            throw exception;
        }
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

    private ResumeOptimizationResultEntity createResult(Long taskId,
                                                        ResumeOptimizeResponse parsedResponse,
                                                        String rawModelResponse) {
        ResumeOptimizationResultEntity result = new ResumeOptimizationResultEntity();
        result.setTaskId(taskId);
        result.setJobRequirementSummary(parsedResponse.getJobRequirementSummary());
        result.setMatchAnalysis(parsedResponse.getMatchAnalysis());
        result.setOptimizationSuggestions(parsedResponse.getOptimizationSuggestions());
        result.setOptimizedResume(parsedResponse.getOptimizedResume());
        result.setInterviewSuggestions(parsedResponse.getInterviewSuggestions());
        result.setRawModelResponse(rawModelResponse);
        result.setCreatedAt(LocalDateTime.now());
        return resumeOptimizationResultRepository.save(result);
    }

    private void markTaskFailed(ResumeAnalysisTaskEntity task, RuntimeException exception) {
        if (task == null) {
            return;
        }

        task.setStatus(ResumeAnalysisStatus.FAILED.name());
        task.setErrorMessage(exception.getMessage());
        task.setUpdatedAt(LocalDateTime.now());
        resumeAnalysisTaskRepository.save(task);
    }
}
