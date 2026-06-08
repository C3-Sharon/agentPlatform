package com.sharon.agentplatform.mcp.external.dto;

public class McpExternalCheckStepResponse {
    private String stage;
    private Boolean success;
    private String message;
    private String suggestion;

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
}
