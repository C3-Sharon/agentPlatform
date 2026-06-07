package com.sharon.agentplatform.mcp.external.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class McpExternalServerHealthResponse {
    private String serverId;
    private String name;
    private String baseUrl;
    private Boolean enabled;
    private Boolean healthy;
    private String status;
    private String errorMessage;
    private LocalDateTime checkedAt;
    private String protocolVersion;
    private Object serverInfo;
    private Object capabilities;
    private Boolean initializeOk;
    private String initializeError;
    private Boolean pingOk;
    private String pingError;
    private Boolean toolsListOk;
    private String toolsListError;
    private Integer toolCount;
    private List<String> toolNames;
    private Map<String, Object> rawInitializeResult;
    private Map<String, Object> rawPingResult;

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getHealthy() { return healthy; }
    public void setHealthy(Boolean healthy) { this.healthy = healthy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
    public String getProtocolVersion() { return protocolVersion; }
    public void setProtocolVersion(String protocolVersion) { this.protocolVersion = protocolVersion; }
    public Object getServerInfo() { return serverInfo; }
    public void setServerInfo(Object serverInfo) { this.serverInfo = serverInfo; }
    public Object getCapabilities() { return capabilities; }
    public void setCapabilities(Object capabilities) { this.capabilities = capabilities; }
    public Boolean getInitializeOk() { return initializeOk; }
    public void setInitializeOk(Boolean initializeOk) { this.initializeOk = initializeOk; }
    public String getInitializeError() { return initializeError; }
    public void setInitializeError(String initializeError) { this.initializeError = initializeError; }
    public Boolean getPingOk() { return pingOk; }
    public void setPingOk(Boolean pingOk) { this.pingOk = pingOk; }
    public String getPingError() { return pingError; }
    public void setPingError(String pingError) { this.pingError = pingError; }
    public Boolean getToolsListOk() { return toolsListOk; }
    public void setToolsListOk(Boolean toolsListOk) { this.toolsListOk = toolsListOk; }
    public String getToolsListError() { return toolsListError; }
    public void setToolsListError(String toolsListError) { this.toolsListError = toolsListError; }
    public Integer getToolCount() { return toolCount; }
    public void setToolCount(Integer toolCount) { this.toolCount = toolCount; }
    public List<String> getToolNames() { return toolNames; }
    public void setToolNames(List<String> toolNames) { this.toolNames = toolNames; }
    public Map<String, Object> getRawInitializeResult() { return rawInitializeResult; }
    public void setRawInitializeResult(Map<String, Object> rawInitializeResult) { this.rawInitializeResult = rawInitializeResult; }
    public Map<String, Object> getRawPingResult() { return rawPingResult; }
    public void setRawPingResult(Map<String, Object> rawPingResult) { this.rawPingResult = rawPingResult; }
}
