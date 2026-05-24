package com.sharon.agentplatform.skill.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillContext;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillRegistry;
import com.sharon.agentplatform.skill.core.SkillResult;
import com.sharon.agentplatform.skill.dto.SkillCallRequest;
import com.sharon.agentplatform.skill.dto.SkillStatsResponse;
import com.sharon.agentplatform.skill.service.SkillStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillRegistry skillRegistry;
    private final SkillStatsService skillStatsService;

    public SkillController(SkillRegistry skillRegistry, SkillStatsService skillStatsService) {
        this.skillRegistry = skillRegistry;
        this.skillStatsService = skillStatsService;
    }

    @GetMapping
    public ApiResponse<Collection<Map<String, Object>>> listSkills() {
        Collection<Map<String, Object>> skills = skillRegistry.listAll()
                .stream()
                .map(this::toSkillView)
                .toList();

        return ApiResponse.success(skills);
    }

    @GetMapping("/stats")
    public ApiResponse<List<SkillStatsResponse>> getSkillStats() {
        return ApiResponse.success(skillStatsService.getSkillStats());
    }

    @GetMapping("/{name}")
    public ApiResponse<Map<String, Object>> getSkill(@PathVariable String name) {
        return skillRegistry.getSkill(name)
                .map(skill -> ApiResponse.success(toSkillView(skill)))
                .orElseGet(() -> ApiResponse.fail("Skill not found: " + name));
    }

    @PostMapping("/{name}/enable")
    public ApiResponse<Void> enableSkill(@PathVariable String name) {
        boolean success = skillRegistry.enable(name);

        if (!success) {
            return ApiResponse.fail("Skill not found: " + name);
        }

        return ApiResponse.success("Skill enabled: " + name, null);
    }

    @PostMapping("/{name}/disable")
    public ApiResponse<Void> disableSkill(@PathVariable String name) {
        boolean success = skillRegistry.disable(name);

        if (!success) {
            return ApiResponse.fail("Skill not found: " + name);
        }

        return ApiResponse.success("Skill disabled: " + name, null);
    }

    @PostMapping("/{name}/call")
    public ApiResponse<SkillResult> callSkill(
            @PathVariable String name,
            @RequestBody SkillCallRequest request
    ) {
        SkillContext context = new SkillContext(request.getParams());
        SkillResult result = skillRegistry.call(name, context);

        if (!result.isSuccess()) {
            return ApiResponse.fail(result.getErrorMessage());
        }

        return ApiResponse.success(result);
    }

    private Map<String, Object> toSkillView(Skill skill) {
        SkillMetadata metadata = skill.metadata();

        return Map.of(
                "name", metadata.getName(),
                "displayName", metadata.getDisplayName(),
                "description", metadata.getDescription(),
                "version", metadata.getVersion(),
                "parameterSchema", metadata.getParameterSchema(),
                "dependencies", metadata.getDependencies(),
                "enabled", skillRegistry.isEnabled(metadata.getName())
        );
    }
}
