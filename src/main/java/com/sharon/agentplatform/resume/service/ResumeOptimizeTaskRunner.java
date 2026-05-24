package com.sharon.agentplatform.resume.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.model.service.ModelService;
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
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ResumeOptimizeTaskRunner {

    private static final String SYSTEM_PROMPT = "\u4f60\u662f\u4e00\u540d\u4e13\u4e1a\u7684\u6821\u56ed\u62db\u8058\u7b80\u5386\u4f18\u5316\u987e\u95ee\uff0c\u64c5\u957f\u6839\u636e\u5c97\u4f4d\u8981\u6c42\u4f18\u5316\u4e2d\u6587\u7b80\u5386\uff0c\u5e76\u7ed9\u51fa\u9762\u8bd5\u51c6\u5907\u5efa\u8bae\u3002";

    private final ResumeFileRepository resumeFileRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeAnalysisTaskRepository resumeAnalysisTaskRepository;
    private final ResumeOptimizationResultRepository resumeOptimizationResultRepository;
    private final ResumePromptBuilder resumePromptBuilder;
    private final ResumeOptimizeResultParser resumeOptimizeResultParser;
    private final ModelService modelService;

    public ResumeOptimizeTaskRunner(ResumeFileRepository resumeFileRepository,
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

    public ResumeOptimizeResponse runExistingTask(Long taskId) {
        ResumeAnalysisTaskEntity task = resumeAnalysisTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Resume analysis task not found: " + taskId));

        try {
            ResumeFileEntity resumeFile = resumeFileRepository.findById(task.getResumeFileId())
                    .orElseThrow(() -> new BusinessException("Resume file not found: " + task.getResumeFileId()));
            if (resumeFile.getParsedText() == null || resumeFile.getParsedText().isBlank()) {
                throw new BusinessException("Resume file has not been parsed: " + task.getResumeFileId());
            }

            JobPostingEntity jobPosting = jobPostingRepository.findById(task.getJobPostingId())
                    .orElseThrow(() -> new BusinessException("Job posting not found: " + task.getJobPostingId()));
            if (jobPosting.getRawText() == null || jobPosting.getRawText().isBlank()) {
                throw new BusinessException("Job posting text is empty: " + task.getJobPostingId());
            }

            String userPrompt = resumePromptBuilder.buildOptimizePrompt(jobPosting.getRawText(), resumeFile.getParsedText());
            String rawModelResponse = modelService.chatWithContext(task.getModelId(), SYSTEM_PROMPT, userPrompt);
            if (rawModelResponse == null || rawModelResponse.isBlank()) {
                throw new BusinessException("Resume optimize model response is empty");
            }

            ResumeOptimizeResponse parsedResponse = resumeOptimizeResultParser.parse(task.getId(), null, rawModelResponse);
            ResumeOptimizationResultEntity result = createResult(task.getId(), parsedResponse, rawModelResponse);
            parsedResponse.setResultId(result.getId());

            task.setStatus(ResumeAnalysisStatus.SUCCESS.name());
            task.setErrorMessage(null);
            task.setUpdatedAt(LocalDateTime.now());
            resumeAnalysisTaskRepository.save(task);

            return parsedResponse;
        } catch (RuntimeException exception) {
            markTaskFailed(task, exception);
            throw exception;
        }
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
        task.setStatus(ResumeAnalysisStatus.FAILED.name());
        task.setErrorMessage(getErrorMessage(exception));
        task.setUpdatedAt(LocalDateTime.now());
        resumeAnalysisTaskRepository.save(task);
    }

    private String getErrorMessage(RuntimeException exception) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "Resume optimize task failed";
        }

        return exception.getMessage();
    }
}
