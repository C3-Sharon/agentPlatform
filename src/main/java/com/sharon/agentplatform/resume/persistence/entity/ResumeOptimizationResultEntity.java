package com.sharon.agentplatform.resume.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_optimization_result")
public class ResumeOptimizationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Lob
    @Column(name = "job_requirement_summary", columnDefinition = "LONGTEXT")
    private String jobRequirementSummary;

    @Lob
    @Column(name = "match_analysis", columnDefinition = "LONGTEXT")
    private String matchAnalysis;

    @Lob
    @Column(name = "optimization_suggestions", columnDefinition = "LONGTEXT")
    private String optimizationSuggestions;

    @Lob
    @Column(name = "optimized_resume", columnDefinition = "LONGTEXT")
    private String optimizedResume;

    @Lob
    @Column(name = "interview_suggestions", columnDefinition = "LONGTEXT")
    private String interviewSuggestions;

    @Lob
    @Column(name = "raw_model_response", columnDefinition = "LONGTEXT")
    private String rawModelResponse;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
