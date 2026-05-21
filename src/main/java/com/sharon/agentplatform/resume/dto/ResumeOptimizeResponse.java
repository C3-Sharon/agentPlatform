package com.sharon.agentplatform.resume.dto;

public class ResumeOptimizeResponse {

    private Long taskId;
    private Long resultId;
    private String jobRequirementSummary;
    private String matchAnalysis;
    private String optimizationSuggestions;
    private String optimizedResume;
    private String interviewSuggestions;
    private String rawModelResponse;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public String getJobRequirementSummary() {
        return jobRequirementSummary;
    }

    public void setJobRequirementSummary(String jobRequirementSummary) {
        this.jobRequirementSummary = jobRequirementSummary;
    }

    public String getMatchAnalysis() {
        return matchAnalysis;
    }

    public void setMatchAnalysis(String matchAnalysis) {
        this.matchAnalysis = matchAnalysis;
    }

    public String getOptimizationSuggestions() {
        return optimizationSuggestions;
    }

    public void setOptimizationSuggestions(String optimizationSuggestions) {
        this.optimizationSuggestions = optimizationSuggestions;
    }

    public String getOptimizedResume() {
        return optimizedResume;
    }

    public void setOptimizedResume(String optimizedResume) {
        this.optimizedResume = optimizedResume;
    }

    public String getInterviewSuggestions() {
        return interviewSuggestions;
    }

    public void setInterviewSuggestions(String interviewSuggestions) {
        this.interviewSuggestions = interviewSuggestions;
    }

    public String getRawModelResponse() {
        return rawModelResponse;
    }

    public void setRawModelResponse(String rawModelResponse) {
        this.rawModelResponse = rawModelResponse;
    }
}
