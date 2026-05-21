package com.sharon.agentplatform.resume.skill;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeRequest;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeResponse;
import com.sharon.agentplatform.resume.service.ResumeOptimizeService;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillContext;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ResumeOptimizeSkill implements Skill {

    private static final String DEFAULT_MODEL_ID = "siliconflow-qwen";

    private final ResumeOptimizeService resumeOptimizeService;

    public ResumeOptimizeSkill(ResumeOptimizeService resumeOptimizeService) {
        this.resumeOptimizeService = resumeOptimizeService;
    }

    @Override
    public SkillMetadata metadata() {
        return new SkillMetadata(
                "resume_optimize",
                "简历优化",
                "根据已上传并解析的简历文件和已读取的招聘岗位信息，生成岗位要求摘要、简历匹配分析、简历优化建议、优化后的简历和面试准备建议。",
                "1.0.0",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "conversationId", Map.of(
                                        "type", "string",
                                        "description", "可选，会话 ID"
                                ),
                                "modelId", Map.of(
                                        "type", "string",
                                        "description", "可选，模型 ID，默认 siliconflow-qwen",
                                        "default", DEFAULT_MODEL_ID
                                ),
                                "resumeFileId", Map.of(
                                        "type", "string",
                                        "description", "必填，上传简历后返回的 fileId"
                                ),
                                "jobPostingId", Map.of(
                                        "type", "number",
                                        "description", "必填，岗位网页读取后返回的 jobPostingId"
                                )
                        ),
                        "required", List.of("resumeFileId", "jobPostingId")
                ),
                List.of("resume_file", "job_posting", "mysql", "llm")
        );
    }

    @Override
    public SkillResult execute(SkillContext context) {
        String conversationId = trimToNull(context.getStringParam("conversationId"));
        String modelId = trimToNull(context.getStringParam("modelId"));
        String resumeFileId = trimToNull(context.getStringParam("resumeFileId"));

        if (resumeFileId == null) {
            return SkillResult.fail("resumeFileId is required");
        }

        Long jobPostingId;
        try {
            jobPostingId = toLong(context.getParam("jobPostingId"));
        } catch (IllegalArgumentException exception) {
            return SkillResult.fail("jobPostingId must be a number");
        }

        if (jobPostingId == null) {
            return SkillResult.fail("jobPostingId is required");
        }

        ResumeOptimizeRequest request = new ResumeOptimizeRequest();
        request.setConversationId(conversationId);
        request.setModelId(modelId == null ? DEFAULT_MODEL_ID : modelId);
        request.setResumeFileId(resumeFileId);
        request.setJobPostingId(jobPostingId);

        try {
            ResumeOptimizeResponse response = resumeOptimizeService.optimize(request);
            return SkillResult.success(formatResponse(response));
        } catch (BusinessException exception) {
            return SkillResult.fail(exception.getMessage());
        } catch (Exception exception) {
            return SkillResult.fail("Failed to optimize resume: " + exception.getMessage());
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long longValue) {
            return longValue;
        }

        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }

        if (value instanceof Double doubleValue) {
            if (!Double.isFinite(doubleValue) || doubleValue % 1 != 0) {
                throw new IllegalArgumentException("Not an integer number");
            }
            return doubleValue.longValue();
        }

        if (value instanceof String stringValue) {
            String trimmed = stringValue.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(trimmed);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Not a number", exception);
            }
        }

        throw new IllegalArgumentException("Unsupported type");
    }

    private String formatResponse(ResumeOptimizeResponse response) {
        return """
                简历优化任务已完成。
                任务ID：%s
                结果ID：%s

                【岗位要求摘要】
                %s

                【简历匹配分析】
                %s

                【简历优化建议】
                %s

                【优化后的简历】
                %s

                【面试准备建议】
                %s
                """.formatted(
                valueOrEmpty(response.getTaskId()),
                valueOrEmpty(response.getResultId()),
                valueOrEmpty(response.getJobRequirementSummary()),
                valueOrEmpty(response.getMatchAnalysis()),
                valueOrEmpty(response.getOptimizationSuggestions()),
                valueOrEmpty(response.getOptimizedResume()),
                valueOrEmpty(response.getInterviewSuggestions())
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String valueOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
