package com.sharon.agentplatform.plugin.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.plugin.core.LoadedPluginSkill;
import com.sharon.agentplatform.plugin.dto.PluginPackageResponse;
import com.sharon.agentplatform.plugin.dto.PluginUploadResponse;
import com.sharon.agentplatform.plugin.service.PluginSkillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/plugins")
public class PluginSkillController {

    private final PluginSkillService pluginSkillService;

    public PluginSkillController(PluginSkillService pluginSkillService) {
        this.pluginSkillService = pluginSkillService;
    }

    @PostMapping("/skills/upload")
    public ApiResponse<PluginUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        PluginSkillService.LoadedPluginUpload upload = pluginSkillService.uploadAndLoadWithPluginId(file);
        List<LoadedPluginSkill> loadedSkills = upload.loadedSkills();

        PluginUploadResponse response = new PluginUploadResponse();
        response.setPluginId(upload.pluginId());
        response.setLoadedSkills(loadedSkills);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<PluginPackageResponse>> listPlugins() {
        return ApiResponse.success(pluginSkillService.listPlugins());
    }

    @PostMapping("/{pluginId}/enable")
    public ApiResponse<PluginPackageResponse> enablePlugin(@PathVariable String pluginId) {
        return ApiResponse.success(pluginSkillService.enablePlugin(pluginId));
    }

    @PostMapping("/{pluginId}/disable")
    public ApiResponse<PluginPackageResponse> disablePlugin(@PathVariable String pluginId) {
        return ApiResponse.success(pluginSkillService.disablePlugin(pluginId));
    }
}
