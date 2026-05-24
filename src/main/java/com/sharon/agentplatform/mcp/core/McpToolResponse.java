package com.sharon.agentplatform.mcp.core;

public class McpToolResponse {

    private boolean success;
    private Object result;
    private String errorMessage;

    public static McpToolResponse success(Object result) {
        McpToolResponse response = new McpToolResponse();
        response.setSuccess(true);
        response.setResult(result);
        response.setErrorMessage(null);
        return response;
    }

    public static McpToolResponse fail(String errorMessage) {
        McpToolResponse response = new McpToolResponse();
        response.setSuccess(false);
        response.setResult(null);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
