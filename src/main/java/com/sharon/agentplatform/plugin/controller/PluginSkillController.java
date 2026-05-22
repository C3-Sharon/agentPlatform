package com.sharon.agentplatform.plugin.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.plugin.core.LoadedPluginSkill;
import com.sharon.agentplatform.plugin.dto.PluginUploadResponse;
import com.sharon.agentplatform.plugin.service.PluginSkillService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/plugins/skills")
public class PluginSkillController {

    private final PluginSkillService pluginSkillService;

    public PluginSkillController(PluginSkillService pluginSkillService) {
        this.pluginSkillService = pluginSkillService;
    }

    @PostMapping("/upload")
    public ApiResponse<PluginUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        List<LoadedPluginSkill> loadedSkills = pluginSkillService.uploadAndLoad(file);

        PluginUploadResponse response = new PluginUploadResponse();
        response.setLoadedSkills(loadedSkills);
        return ApiResponse.success(response);
    }
}
