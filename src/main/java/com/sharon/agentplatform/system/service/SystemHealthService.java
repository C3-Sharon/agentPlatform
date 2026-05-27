package com.sharon.agentplatform.system.service;

import com.sharon.agentplatform.mcp.core.McpToolMetadata;
import com.sharon.agentplatform.mcp.core.McpToolRegistry;
import com.sharon.agentplatform.mcp.external.entity.McpExternalServerEntity;
import com.sharon.agentplatform.mcp.external.repository.McpExternalServerRepository;
import com.sharon.agentplatform.model.core.ModelConfig;
import com.sharon.agentplatform.model.core.ModelConfigStore;
import com.sharon.agentplatform.plugin.entity.PluginPackageEntity;
import com.sharon.agentplatform.plugin.repository.PluginPackageRepository;
import com.sharon.agentplatform.plugin.runtime.PluginRuntime;
import com.sharon.agentplatform.plugin.runtime.PluginRuntimeRegistry;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillRegistry;
import com.sharon.agentplatform.system.dto.SystemHealthResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SystemHealthService {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DEGRADED = "DEGRADED";

    private final ModelConfigStore modelConfigStore;
    private final SkillRegistry skillRegistry;
    private final PluginPackageRepository pluginPackageRepository;
    private final PluginRuntimeRegistry pluginRuntimeRegistry;
    private final McpToolRegistry mcpToolRegistry;
    private final McpExternalServerRepository mcpExternalServerRepository;

    public SystemHealthService(ModelConfigStore modelConfigStore,
                               SkillRegistry skillRegistry,
                               PluginPackageRepository pluginPackageRepository,
                               PluginRuntimeRegistry pluginRuntimeRegistry,
                               McpToolRegistry mcpToolRegistry,
                               McpExternalServerRepository mcpExternalServerRepository) {
        this.modelConfigStore = modelConfigStore;
        this.skillRegistry = skillRegistry;
        this.pluginPackageRepository = pluginPackageRepository;
        this.pluginRuntimeRegistry = pluginRuntimeRegistry;
        this.mcpToolRegistry = mcpToolRegistry;
        this.mcpExternalServerRepository = mcpExternalServerRepository;
    }

    public SystemHealthResponse check() {
        SystemHealthResponse response = new SystemHealthResponse();
        response.setStatus(STATUS_UP);
        response.setCheckedAt(LocalDateTime.now());

        response.setModels(safeBuild(response, this::buildModelHealth, emptyModelHealth()));
        response.setSkills(safeBuild(response, this::buildSkillHealth, emptySkillHealth()));
        response.setPlugins(safeBuild(response, this::buildPluginHealth, emptyPluginHealth()));
        response.setMcp(safeBuild(response, this::buildMcpHealth, emptyMcpHealth()));
        response.setExternalMcp(safeBuild(response, this::buildExternalMcpHealth, emptyExternalMcpHealth()));
        response.setMemory(buildMemoryHealth());
        response.setConsole(buildConsoleHealth());
        return response;
    }

    private <T> T safeBuild(SystemHealthResponse response, HealthBuilder<T> builder, T fallback) {
        try {
            return builder.build();
        } catch (Exception exception) {
            response.setStatus(STATUS_DEGRADED);
            return fallback;
        }
    }

    private SystemHealthResponse.ModelHealth buildModelHealth() {
        List<ModelConfig> models = modelConfigStore.listAll();
        List<String> enabledModelIds = models.stream()
                .filter(ModelConfig::isEnabled)
                .map(ModelConfig::getId)
                .sorted()
                .toList();

        int visionCapable = (int) models.stream()
                .filter(this::isVisionCapable)
                .count();

        SystemHealthResponse.ModelHealth health = new SystemHealthResponse.ModelHealth();
        health.setTotal(models.size());
        health.setEnabled(enabledModelIds.size());
        health.setDisabled(models.size() - enabledModelIds.size());
        health.setVisionCapable(visionCapable);
        health.setEnabledModelIds(enabledModelIds);
        return health;
    }

    private boolean isVisionCapable(ModelConfig modelConfig) {
        return modelConfig.getCapabilities().stream()
                .anyMatch(capability -> "vision".equalsIgnoreCase(capability)
                        || "multimodal".equalsIgnoreCase(capability));
    }

    private SystemHealthResponse.SkillHealth buildSkillHealth() {
        List<String> skillNames = skillRegistry.listAll().stream()
                .map(Skill::metadata)
                .map(metadata -> metadata.getName())
                .filter(name -> name != null && !name.isBlank())
                .sorted()
                .toList();

        List<String> disabledSkillNames = skillNames.stream()
                .filter(name -> !skillRegistry.isEnabled(name))
                .sorted()
                .toList();

        SystemHealthResponse.SkillHealth health = new SystemHealthResponse.SkillHealth();
        health.setTotal(skillNames.size());
        health.setDisabled(disabledSkillNames.size());
        health.setEnabled(skillNames.size() - disabledSkillNames.size());
        health.setSkillNames(skillNames);
        health.setDisabledSkillNames(disabledSkillNames);
        return health;
    }

    private SystemHealthResponse.PluginHealth buildPluginHealth() {
        List<PluginPackageEntity> packages = pluginPackageRepository.findAll();
        List<PluginRuntime> runtimes = pluginRuntimeRegistry.list();

        SystemHealthResponse.PluginHealth health = new SystemHealthResponse.PluginHealth();
        health.setPackages(packages.size());
        health.setEnabledPackages(countPackagesByStatus(packages, "ENABLED"));
        health.setDisabledPackages(countPackagesByStatus(packages, "DISABLED"));
        health.setFailedPackages(countPackagesByStatus(packages, "FAILED"));
        health.setRuntimeLoaded(runtimes.size());
        health.setRuntimePluginIds(runtimes.stream()
                .map(PluginRuntime::getPluginId)
                .sorted()
                .toList());
        return health;
    }

    private int countPackagesByStatus(List<PluginPackageEntity> packages, String status) {
        return (int) packages.stream()
                .filter(pluginPackage -> status.equalsIgnoreCase(pluginPackage.getStatus()))
                .count();
    }

    private SystemHealthResponse.McpHealth buildMcpHealth() {
        List<String> tools = mcpToolRegistry.list().stream()
                .map(McpToolMetadata::getName)
                .filter(name -> name != null && !name.isBlank())
                .sorted()
                .toList();

        SystemHealthResponse.McpHealth health = new SystemHealthResponse.McpHealth();
        health.setInternalToolCount(tools.size());
        health.setTools(tools);
        return health;
    }

    private SystemHealthResponse.ExternalMcpHealth buildExternalMcpHealth() {
        List<McpExternalServerEntity> servers = mcpExternalServerRepository.findAll();
        List<String> serverNames = servers.stream()
                .map(McpExternalServerEntity::getName)
                .filter(name -> name != null && !name.isBlank())
                .sorted()
                .toList();

        int enabledServers = (int) servers.stream()
                .filter(server -> Boolean.TRUE.equals(server.getEnabled()))
                .count();

        SystemHealthResponse.ExternalMcpHealth health = new SystemHealthResponse.ExternalMcpHealth();
        health.setServers(servers.size());
        health.setEnabledServers(enabledServers);
        health.setDisabledServers(servers.size() - enabledServers);
        health.setServerNames(serverNames);
        return health;
    }

    private SystemHealthResponse.MemoryHealth buildMemoryHealth() {
        SystemHealthResponse.MemoryHealth health = new SystemHealthResponse.MemoryHealth();
        health.setShortTerm("mysql: conversation_message");
        health.setLongTerm("file: FileLongTermMemoryStore");
        health.setRunHistory("mysql: agent_run / agent_run_trace");
        return health;
    }

    private SystemHealthResponse.ConsoleHealth buildConsoleHealth() {
        SystemHealthResponse.ConsoleHealth health = new SystemHealthResponse.ConsoleHealth();
        health.setWeb("/console.html");
        health.setCli("scripts/");
        return health;
    }

    private SystemHealthResponse.ModelHealth emptyModelHealth() {
        SystemHealthResponse.ModelHealth health = new SystemHealthResponse.ModelHealth();
        health.setTotal(0);
        health.setEnabled(0);
        health.setDisabled(0);
        health.setVisionCapable(0);
        health.setEnabledModelIds(List.of());
        return health;
    }

    private SystemHealthResponse.SkillHealth emptySkillHealth() {
        SystemHealthResponse.SkillHealth health = new SystemHealthResponse.SkillHealth();
        health.setTotal(0);
        health.setEnabled(0);
        health.setDisabled(0);
        health.setSkillNames(new ArrayList<>());
        health.setDisabledSkillNames(new ArrayList<>());
        return health;
    }

    private SystemHealthResponse.PluginHealth emptyPluginHealth() {
        SystemHealthResponse.PluginHealth health = new SystemHealthResponse.PluginHealth();
        health.setPackages(0);
        health.setEnabledPackages(0);
        health.setDisabledPackages(0);
        health.setFailedPackages(0);
        health.setRuntimeLoaded(0);
        health.setRuntimePluginIds(List.of());
        return health;
    }

    private SystemHealthResponse.McpHealth emptyMcpHealth() {
        SystemHealthResponse.McpHealth health = new SystemHealthResponse.McpHealth();
        health.setInternalToolCount(0);
        health.setTools(List.of());
        return health;
    }

    private SystemHealthResponse.ExternalMcpHealth emptyExternalMcpHealth() {
        SystemHealthResponse.ExternalMcpHealth health = new SystemHealthResponse.ExternalMcpHealth();
        health.setServers(0);
        health.setEnabledServers(0);
        health.setDisabledServers(0);
        health.setServerNames(List.of());
        return health;
    }

    @FunctionalInterface
    private interface HealthBuilder<T> {
        T build();
    }
}
