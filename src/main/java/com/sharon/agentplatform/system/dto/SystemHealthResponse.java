package com.sharon.agentplatform.system.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SystemHealthResponse {

    private String status;
    private LocalDateTime checkedAt;
    private ModelHealth models;
    private SkillHealth skills;
    private PluginHealth plugins;
    private McpHealth mcp;
    private ExternalMcpHealth externalMcp;
    private MemoryHealth memory;
    private PendingSkillHealth pendingSkill;
    private ConsoleHealth console;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public ModelHealth getModels() {
        return models;
    }

    public void setModels(ModelHealth models) {
        this.models = models;
    }

    public SkillHealth getSkills() {
        return skills;
    }

    public void setSkills(SkillHealth skills) {
        this.skills = skills;
    }

    public PluginHealth getPlugins() {
        return plugins;
    }

    public void setPlugins(PluginHealth plugins) {
        this.plugins = plugins;
    }

    public McpHealth getMcp() {
        return mcp;
    }

    public void setMcp(McpHealth mcp) {
        this.mcp = mcp;
    }

    public ExternalMcpHealth getExternalMcp() {
        return externalMcp;
    }

    public void setExternalMcp(ExternalMcpHealth externalMcp) {
        this.externalMcp = externalMcp;
    }

    public MemoryHealth getMemory() {
        return memory;
    }

    public void setMemory(MemoryHealth memory) {
        this.memory = memory;
    }

    public PendingSkillHealth getPendingSkill() {
        return pendingSkill;
    }

    public void setPendingSkill(PendingSkillHealth pendingSkill) {
        this.pendingSkill = pendingSkill;
    }

    public ConsoleHealth getConsole() {
        return console;
    }

    public void setConsole(ConsoleHealth console) {
        this.console = console;
    }

    public static class ModelHealth {
        private Integer total;
        private Integer enabled;
        private Integer disabled;
        private Integer visionCapable;
        private List<String> enabledModelIds;

        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }
        public Integer getEnabled() { return enabled; }
        public void setEnabled(Integer enabled) { this.enabled = enabled; }
        public Integer getDisabled() { return disabled; }
        public void setDisabled(Integer disabled) { this.disabled = disabled; }
        public Integer getVisionCapable() { return visionCapable; }
        public void setVisionCapable(Integer visionCapable) { this.visionCapable = visionCapable; }
        public List<String> getEnabledModelIds() { return enabledModelIds; }
        public void setEnabledModelIds(List<String> enabledModelIds) { this.enabledModelIds = enabledModelIds; }
    }

    public static class SkillHealth {
        private Integer total;
        private Integer enabled;
        private Integer disabled;
        private List<String> skillNames;
        private List<String> disabledSkillNames;

        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }
        public Integer getEnabled() { return enabled; }
        public void setEnabled(Integer enabled) { this.enabled = enabled; }
        public Integer getDisabled() { return disabled; }
        public void setDisabled(Integer disabled) { this.disabled = disabled; }
        public List<String> getSkillNames() { return skillNames; }
        public void setSkillNames(List<String> skillNames) { this.skillNames = skillNames; }
        public List<String> getDisabledSkillNames() { return disabledSkillNames; }
        public void setDisabledSkillNames(List<String> disabledSkillNames) { this.disabledSkillNames = disabledSkillNames; }
    }

    public static class PluginHealth {
        private Integer packages;
        private Integer enabledPackages;
        private Integer disabledPackages;
        private Integer failedPackages;
        private Integer runtimeLoaded;
        private List<String> runtimePluginIds;

        public Integer getPackages() { return packages; }
        public void setPackages(Integer packages) { this.packages = packages; }
        public Integer getEnabledPackages() { return enabledPackages; }
        public void setEnabledPackages(Integer enabledPackages) { this.enabledPackages = enabledPackages; }
        public Integer getDisabledPackages() { return disabledPackages; }
        public void setDisabledPackages(Integer disabledPackages) { this.disabledPackages = disabledPackages; }
        public Integer getFailedPackages() { return failedPackages; }
        public void setFailedPackages(Integer failedPackages) { this.failedPackages = failedPackages; }
        public Integer getRuntimeLoaded() { return runtimeLoaded; }
        public void setRuntimeLoaded(Integer runtimeLoaded) { this.runtimeLoaded = runtimeLoaded; }
        public List<String> getRuntimePluginIds() { return runtimePluginIds; }
        public void setRuntimePluginIds(List<String> runtimePluginIds) { this.runtimePluginIds = runtimePluginIds; }
    }

    public static class McpHealth {
        private Integer internalToolCount;
        private List<String> tools;

        public Integer getInternalToolCount() { return internalToolCount; }
        public void setInternalToolCount(Integer internalToolCount) { this.internalToolCount = internalToolCount; }
        public List<String> getTools() { return tools; }
        public void setTools(List<String> tools) { this.tools = tools; }
    }

    public static class ExternalMcpHealth {
        private Integer servers;
        private Integer enabledServers;
        private Integer disabledServers;
        private List<String> serverNames;

        public Integer getServers() { return servers; }
        public void setServers(Integer servers) { this.servers = servers; }
        public Integer getEnabledServers() { return enabledServers; }
        public void setEnabledServers(Integer enabledServers) { this.enabledServers = enabledServers; }
        public Integer getDisabledServers() { return disabledServers; }
        public void setDisabledServers(Integer disabledServers) { this.disabledServers = disabledServers; }
        public List<String> getServerNames() { return serverNames; }
        public void setServerNames(List<String> serverNames) { this.serverNames = serverNames; }
    }

    public static class MemoryHealth {
        private String shortTerm;
        private String longTerm;
        private String runHistory;

        public String getShortTerm() { return shortTerm; }
        public void setShortTerm(String shortTerm) { this.shortTerm = shortTerm; }
        public String getLongTerm() { return longTerm; }
        public void setLongTerm(String longTerm) { this.longTerm = longTerm; }
        public String getRunHistory() { return runHistory; }
        public void setRunHistory(String runHistory) { this.runHistory = runHistory; }
    }

    public static class PendingSkillHealth {
        private String storeType;
        private Long ttlMinutes;
        private String keyPattern;

        public String getStoreType() { return storeType; }
        public void setStoreType(String storeType) { this.storeType = storeType; }
        public Long getTtlMinutes() { return ttlMinutes; }
        public void setTtlMinutes(Long ttlMinutes) { this.ttlMinutes = ttlMinutes; }
        public String getKeyPattern() { return keyPattern; }
        public void setKeyPattern(String keyPattern) { this.keyPattern = keyPattern; }
    }

    public static class ConsoleHealth {
        private String web;
        private String cli;

        public String getWeb() { return web; }
        public void setWeb(String web) { this.web = web; }
        public String getCli() { return cli; }
        public void setCli(String cli) { this.cli = cli; }
    }
}
