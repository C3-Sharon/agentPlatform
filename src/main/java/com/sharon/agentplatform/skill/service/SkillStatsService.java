package com.sharon.agentplatform.skill.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.agent.history.entity.AgentRunTraceEntity;
import com.sharon.agentplatform.agent.history.repository.AgentRunTraceRepository;
import com.sharon.agentplatform.skill.dto.SkillStatsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkillStatsService {

    private static final String CALL_SKILL_STEP = "CALL_SKILL";
    private static final String SUCCESS_STATUS = "SUCCESS";

    private final AgentRunTraceRepository agentRunTraceRepository;
    private final ObjectMapper objectMapper;

    public SkillStatsService(AgentRunTraceRepository agentRunTraceRepository, ObjectMapper objectMapper) {
        this.agentRunTraceRepository = agentRunTraceRepository;
        this.objectMapper = objectMapper;
    }

    public List<SkillStatsResponse> getSkillStats() {
        Map<String, SkillStatsAccumulator> statsBySkill = new LinkedHashMap<>();

        for (AgentRunTraceEntity trace : agentRunTraceRepository.findTop500ByStepOrderByCreatedAtDesc(CALL_SKILL_STEP)) {
            String skillName = extractSkillName(trace.getDataJson());
            if (skillName == null || skillName.isBlank()) {
                continue;
            }

            SkillStatsAccumulator stats = statsBySkill.computeIfAbsent(skillName, SkillStatsAccumulator::new);
            stats.accept(trace);
        }

        return statsBySkill.values()
                .stream()
                .map(SkillStatsAccumulator::toResponse)
                .sorted(Comparator
                        .comparing(SkillStatsResponse::getCallCount, Comparator.reverseOrder())
                        .thenComparing(SkillStatsResponse::getLastCalledAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private String extractSkillName(String dataJson) {
        if (dataJson == null || dataJson.isBlank()) {
            return null;
        }

        try {
            Object parsed = objectMapper.readValue(dataJson, Object.class);
            if (parsed instanceof Map<?, ?> data) {
                Object skillName = data.get("skillName");
                return skillName == null ? null : skillName.toString();
            }
        } catch (Exception ignored) {
            return null;
        }

        return null;
    }

    private static class SkillStatsAccumulator {
        private final String skillName;
        private long callCount;
        private long successCount;
        private long failCount;
        private long durationCount;
        private long durationTotal;
        private LocalDateTime lastCalledAt;

        private SkillStatsAccumulator(String skillName) {
            this.skillName = skillName;
        }

        private void accept(AgentRunTraceEntity trace) {
            callCount++;
            if (SUCCESS_STATUS.equals(trace.getStatus())) {
                successCount++;
            } else {
                failCount++;
            }

            if (trace.getDurationMs() != null) {
                durationCount++;
                durationTotal += trace.getDurationMs();
            }

            LocalDateTime calledAt = trace.getTraceTimestamp() == null ? trace.getCreatedAt() : trace.getTraceTimestamp();
            if (calledAt != null && (lastCalledAt == null || calledAt.isAfter(lastCalledAt))) {
                lastCalledAt = calledAt;
            }
        }

        private SkillStatsResponse toResponse() {
            SkillStatsResponse response = new SkillStatsResponse();
            response.setSkillName(skillName);
            response.setCallCount(callCount);
            response.setSuccessCount(successCount);
            response.setFailCount(failCount);
            response.setAvgDurationMs(durationCount == 0 ? null : (double) durationTotal / durationCount);
            response.setLastCalledAt(lastCalledAt);
            return response;
        }
    }
}
