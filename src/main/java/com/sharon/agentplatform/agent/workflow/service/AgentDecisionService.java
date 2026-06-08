package com.sharon.agentplatform.agent.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.agent.history.entity.AgentRunEntity;
import com.sharon.agentplatform.agent.history.entity.AgentRunTraceEntity;
import com.sharon.agentplatform.agent.history.repository.AgentRunRepository;
import com.sharon.agentplatform.agent.history.repository.AgentRunTraceRepository;
import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionViewResponse;
import com.sharon.agentplatform.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AgentDecisionService {

    private static final String STEP_INTENT_DETECTION = "INTENT_DETECTION";
    private static final String STEP_LLM_SKILL_DECISION = "LLM_SKILL_DECISION";
    private static final String STEP_SELECT_SKILL = "SELECT_SKILL";
    private static final String STEP_PARAM_RESOLUTION = "PARAM_RESOLUTION";

    private final AgentRunRepository agentRunRepository;
    private final AgentRunTraceRepository agentRunTraceRepository;
    private final ObjectMapper objectMapper;

    public AgentDecisionService(AgentRunRepository agentRunRepository,
                                AgentRunTraceRepository agentRunTraceRepository,
                                ObjectMapper objectMapper) {
        this.agentRunRepository = agentRunRepository;
        this.agentRunTraceRepository = agentRunTraceRepository;
        this.objectMapper = objectMapper;
    }

    public AgentDecisionViewResponse getDecisions(String runId) {
        AgentRunEntity run = agentRunRepository.findByRunId(runId)
                .orElseThrow(() -> new BusinessException("Agent run not found: " + runId));
        List<AgentDecisionResponse> decisions = buildDecisions(agentRunTraceRepository.findByRunIdOrderByStepOrderAsc(runId));

        AgentDecisionViewResponse response = new AgentDecisionViewResponse();
        response.setRunId(run.getRunId());
        response.setConversationId(run.getConversationId());
        response.setModelId(run.getModelId());
        response.setStatus(run.getStatus());
        response.setCreatedAt(run.getCreatedAt());
        response.setDecisionCount(decisions.size());
        response.setDecisions(decisions);
        return response;
    }

    private List<AgentDecisionResponse> buildDecisions(List<AgentRunTraceEntity> traces) {
        List<AgentDecisionResponse> decisions = new ArrayList<>();
        int decisionOrder = 1;
        for (AgentRunTraceEntity trace : traces) {
            if (isDecisionStep(trace.getStep())) {
                decisions.add(toDecisionResponse(decisionOrder++, trace));
            }
        }
        return decisions;
    }

    private AgentDecisionResponse toDecisionResponse(int decisionOrder, AgentRunTraceEntity trace) {
        Map<String, Object> data = parseDataMap(trace.getDataJson());
        AgentDecisionResponse response = new AgentDecisionResponse();
        response.setDecisionOrder(decisionOrder);
        response.setTraceStepOrder(trace.getStepOrder());
        response.setType(decisionType(trace.getStep()));
        response.setSource(decisionSource(trace.getStep(), trace.getDetail()));
        response.setStatus(trace.getStatus());
        response.setSummary(trace.getDetail());
        response.setIntent(stringValue(data.get("intent")));
        response.setNeedSkill(booleanValue(data.get("needSkill")));
        response.setSkillName(stringValue(data.get("skillName")));
        response.setParams(data.get("params"));
        response.setMissingParams(data.get("missingParams"));
        response.setReason(stringValue(data.get("reason")));
        response.setPendingStore(stringValue(data.get("pendingStore")));
        response.setRawData(data);
        response.setDurationMs(trace.getDurationMs());
        response.setTraceTimestamp(trace.getTraceTimestamp());
        response.setCreatedAt(trace.getCreatedAt());
        return response;
    }

    private boolean isDecisionStep(String step) {
        return STEP_INTENT_DETECTION.equals(step)
                || STEP_LLM_SKILL_DECISION.equals(step)
                || STEP_SELECT_SKILL.equals(step)
                || STEP_PARAM_RESOLUTION.equals(step);
    }

    private String decisionType(String step) {
        if (STEP_INTENT_DETECTION.equals(step)) {
            return "INTENT";
        }
        if (STEP_LLM_SKILL_DECISION.equals(step)) {
            return "LLM_SKILL_DECISION";
        }
        if (STEP_SELECT_SKILL.equals(step)) {
            return "SKILL_SELECTION";
        }
        if (STEP_PARAM_RESOLUTION.equals(step)) {
            return "PARAM_RESOLUTION";
        }
        return "OTHER_DECISION";
    }

    private String decisionSource(String step, String detail) {
        if (STEP_LLM_SKILL_DECISION.equals(step)) {
            return "LLM";
        }
        if (detail != null && detail.contains("规则")) {
            return "RULE";
        }
        if (detail != null && detail.contains("自然语言")) {
            return "RULE";
        }
        return "RUNTIME";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDataMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Object value = objectMapper.readValue(json, Object.class);
            if (value instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
        } catch (Exception ignored) {
            return Map.of();
        }
        return Map.of();
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
