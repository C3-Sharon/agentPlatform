package com.sharon.agentplatform.resume.service;

import com.sharon.agentplatform.resume.dto.ResumeOptimizeResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResumeOptimizeResultParser {

    private static final String JOB_REQUIREMENT_SUMMARY_TITLE = "## 岗位要求摘要";
    private static final String MATCH_ANALYSIS_TITLE = "## 简历匹配分析";
    private static final String OPTIMIZATION_SUGGESTIONS_TITLE = "## 简历优化建议";
    private static final String OPTIMIZED_RESUME_TITLE = "## 优化后的简历";
    private static final String INTERVIEW_SUGGESTIONS_TITLE = "## 面试准备建议";
    private static final String UNEXPECTED_FORMAT_NOTICE = "模型返回内容未按预期标题格式输出，以下为原始返回内容：";
    private static final List<String> TITLES = List.of(
            JOB_REQUIREMENT_SUMMARY_TITLE,
            MATCH_ANALYSIS_TITLE,
            OPTIMIZATION_SUGGESTIONS_TITLE,
            OPTIMIZED_RESUME_TITLE,
            INTERVIEW_SUGGESTIONS_TITLE
    );

    public ResumeOptimizeResponse parse(Long taskId, Long resultId, String rawModelResponse) {
        ResumeOptimizeResponse response = new ResumeOptimizeResponse();
        response.setTaskId(taskId);
        response.setResultId(resultId);
        response.setJobRequirementSummary(extractSection(rawModelResponse, JOB_REQUIREMENT_SUMMARY_TITLE));
        response.setMatchAnalysis(extractSection(rawModelResponse, MATCH_ANALYSIS_TITLE));
        response.setOptimizationSuggestions(extractSection(rawModelResponse, OPTIMIZATION_SUGGESTIONS_TITLE));
        response.setOptimizedResume(extractSection(rawModelResponse, OPTIMIZED_RESUME_TITLE));
        response.setInterviewSuggestions(extractSection(rawModelResponse, INTERVIEW_SUGGESTIONS_TITLE));
        response.setRawModelResponse(rawModelResponse);

        if (hasRawContent(rawModelResponse) && allSectionsEmpty(response)) {
            response.setMatchAnalysis(UNEXPECTED_FORMAT_NOTICE + "\n\n" + rawModelResponse.trim());
        }

        return response;
    }

    private String extractSection(String markdown, String title) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        int start = markdown.indexOf(title);
        if (start < 0) {
            return "";
        }

        int contentStart = start + title.length();
        int contentEnd = markdown.length();
        for (String nextTitle : TITLES) {
            if (nextTitle.equals(title)) {
                continue;
            }

            int nextIndex = markdown.indexOf(nextTitle, contentStart);
            if (nextIndex >= 0 && nextIndex < contentEnd) {
                contentEnd = nextIndex;
            }
        }

        return removeLeadingTitleLines(markdown.substring(contentStart, contentEnd), title).trim();
    }

    private String removeLeadingTitleLines(String content, String title) {
        String trimmed = content.trim();
        String plainTitle = title.replaceFirst("^#+\\s*", "");

        boolean removedTitle;
        do {
            removedTitle = false;
            String firstLine = firstLine(trimmed).trim();
            if (firstLine.equals(title) || firstLine.equals(plainTitle)) {
                trimmed = trimmed.substring(firstLine(trimmed).length()).trim();
                removedTitle = true;
            }
        } while (removedTitle);

        return trimmed;
    }

    private String firstLine(String content) {
        int lineEnd = content.indexOf('\n');
        if (lineEnd < 0) {
            return content;
        }
        return content.substring(0, lineEnd);
    }

    private boolean allSectionsEmpty(ResumeOptimizeResponse response) {
        return isBlank(response.getJobRequirementSummary())
                && isBlank(response.getMatchAnalysis())
                && isBlank(response.getOptimizationSuggestions())
                && isBlank(response.getOptimizedResume())
                && isBlank(response.getInterviewSuggestions());
    }

    private boolean hasRawContent(String rawModelResponse) {
        return rawModelResponse != null && !rawModelResponse.isBlank();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
