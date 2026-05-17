package com.sharon.agentplatform.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.agent.core.SkillDecision;
import com.sharon.agentplatform.model.service.ModelService;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class LlmSkillDecisionService {

    private final SkillRegistry skillRegistry;
    private final ModelService modelService;
    private final ObjectMapper objectMapper;

    public LlmSkillDecisionService(
            SkillRegistry skillRegistry,
            ModelService modelService,
            ObjectMapper objectMapper
    ) {
        this.skillRegistry = skillRegistry;
        this.modelService = modelService;
        this.objectMapper = objectMapper;
    }

    public SkillDecision decide(String modelId, String userMessage) {
        Collection<Skill> enabledSkills = skillRegistry.listEnabled();

        String systemPrompt = buildDecisionSystemPrompt(enabledSkills);
        String userPrompt = buildDecisionUserPrompt(userMessage);

        String rawResponse = modelService.chatWithContext(
                modelId,
                systemPrompt,
                userPrompt
        );

        String json = extractJson(rawResponse);

        try {
            SkillDecision decision = objectMapper.readValue(json, SkillDecision.class);
            return normalizeDecision(decision);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse LLM skill decision. rawResponse=" + rawResponse, e);
        }
    }

    private String buildDecisionSystemPrompt(Collection<Skill> enabledSkills) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                你是 AI Agent Platform 的工具选择器。
                你的任务是根据用户输入，判断是否需要调用一个 Skill。
                
                你只能从给定 Skill 列表中选择。
                如果不需要调用 Skill，请返回 needSkill=false。
                如果需要调用 Skill，请返回 needSkill=true，并给出 skillName 和 params。
                
                你必须只返回 JSON，不要返回 Markdown，不要返回解释文字。
                
                JSON 格式必须严格如下：
                {
                  "needSkill": true,
                  "skillName": "skill_name",
                  "params": {
                    "paramName": "paramValue"
                  },
                  "reason": "为什么选择这个 Skill"
                }
                
                如果不需要 Skill，返回：
                {
                  "needSkill": false,
                  "skillName": "none",
                  "params": {},
                  "reason": "不需要调用 Skill 的原因"
                }
                
                可用 Skill 列表：
                """);

        for (Skill skill : enabledSkills) {
            SkillMetadata metadata = skill.metadata();

            prompt.append("\n- name: ")
                    .append(metadata.getName())
                    .append("\n  displayName: ")
                    .append(metadata.getDisplayName())
                    .append("\n  description: ")
                    .append(metadata.getDescription())
                    .append("\n  parameterSchema: ")
                    .append(metadata.getParameterSchema())
                    .append("\n");
        }

        prompt.append("""
                
                重要规则：
                1. 数学计算类问题优先选择 calculator。
                2. 天气、气温、下雨、出门建议类问题优先选择 weather。
                3. 查找文件、搜索 README、搜索 txt/md 文件类问题优先选择 file_search。
                4. 普通知识解释、闲聊、写作类问题不需要 Skill。
                5. params 必须符合对应 Skill 的 parameterSchema。
                6. 不要选择不存在或已禁用的 Skill。
                7. 最终只输出 JSON。
                """);

        return prompt.toString();
    }

    private String buildDecisionUserPrompt(String userMessage) {
        return """
                用户输入：
                %s
                
                请判断是否需要调用 Skill，并按指定 JSON 格式返回。
                """.formatted(userMessage);
    }

    private String extractJson(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalArgumentException("LLM decision response is empty");
        }

        String text = rawResponse.trim();

        if (text.startsWith("```")) {
            text = text.replace("```json", "")
                    .replace("```", "")
                    .trim();
        }

        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");

        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalArgumentException("No JSON object found in LLM response: " + rawResponse);
        }

        return text.substring(start, end + 1);
    }

    private SkillDecision normalizeDecision(SkillDecision decision) {
        if (decision == null) {
            return SkillDecision.noSkill("LLM decision is null");
        }

        if (!decision.isNeedSkill()) {
            return SkillDecision.noSkill(
                    decision.getReason() == null ? "LLM decided no skill is needed" : decision.getReason()
            );
        }

        if (decision.getSkillName() == null || decision.getSkillName().isBlank()) {
            return SkillDecision.noSkill("LLM did not provide skillName");
        }

        if (skillRegistry.getSkill(decision.getSkillName()).isEmpty()) {
            return SkillDecision.noSkill("LLM selected unknown skill: " + decision.getSkillName());
        }

        if (!skillRegistry.isEnabled(decision.getSkillName())) {
            return SkillDecision.noSkill("LLM selected disabled skill: " + decision.getSkillName());
        }

        if (decision.getParams() == null) {
            decision.setParams(Map.of());
        }

        return decision;
    }
}