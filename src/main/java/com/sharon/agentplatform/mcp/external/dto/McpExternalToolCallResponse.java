package com.sharon.agentplatform.mcp.external.dto;

public class McpExternalToolCallResponse {
    private String toolId;
    private String serverId;
    private String remoteName;
    private String localName;
    private Object result;
    private String errorMessage;
    private Boolean success;
    public String getToolId() { return toolId; }
    public void setToolId(String toolId) { this.toolId = toolId; }
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public String getRemoteName() { return remoteName; }
    public void setRemoteName(String remoteName) { this.remoteName = remoteName; }
    public String getLocalName() { return localName; }
    public void setLocalName(String localName) { this.localName = localName; }
    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
}
