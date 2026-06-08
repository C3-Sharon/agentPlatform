package com.sharon.agentplatform.agent.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.agent.history.entity.AgentRunEntity;
import com.sharon.agentplatform.agent.history.entity.AgentRunTraceEntity;
import com.sharon.agentplatform.agent.history.repository.AgentRunRepository;
import com.sharon.agentplatform.agent.history.repository.AgentRunTraceRepository;
import com.sharon.agentplatform.agent.workflow.dto.AgentActionObservationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentActionResponse;
import com.sharon.agentplatform.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AgentActionObservationService {

    private static final String STEP_CALL_SKILL = "CALL_SKILL";
    private static final String ACTION_TYPE_SKILL_CALL = "SKILL_CALL";

    private final AgentRunRepository agentRunRepository;
    private final AgentRunTraceRepository agentRunTraceRepository;
    private final ObjectMapper objectMapper;

    public AgentActionObservationService(AgentRunRepository agentRunRepository,
                                         AgentRunTraceRepository agentRunTraceRepository,
                                         ObjectMapper objectMapper) {
        this.agentRunRepository = agentRunRepository;
        this.agentRunTraceRepository = agentRunTraceRepository;
        this.objectMapper = objectMapper;
    }

    public AgentActionObservationResponse getActions(String runId) {
        AgentRunEntity run = agentRunRepository.findByRunId(runId)
                .orElseThrow(() -> new BusinessException("Agent run not found: " + runId));
        List<AgentActionResponse> actions = buildActions(agentRunTraceRepository.findByRunIdOrderByStepOrderAsc(runId));

        AgentActionObservationResponse response = new AgentActionObservationResponse();
        response.setRunId(run.getRunId());
        response.setConversationId(run.getConversationId());
        response.setModelId(run.getModelId());
        response.setStatus(run.getStatus());
        response.setCreatedAt(run.getCreatedAt());
        response.setActionCount(actions.size());
        response.setActions(actions);
        return response;
    }

    private List<AgentActionResponse> buildActions(List<AgentRunTraceEntity> traces) {
        List<AgentActionResponse> actions = new ArrayList<>();
        int actionOrder = 1;
        for (AgentRunTraceEntity trace : traces) {
            if (STEP_CALL_SKILL.equals(trace.getStep())) {
                actions.add(toActionResponse(actionOrder++, trace));
            }
        }
        return actions;
    }

    private AgentActionResponse toActionResponse(int actionOrder, AgentRunTraceEntity trace) {
        Map<String, Object> data = parseDataMap(trace.getDataJson());
        AgentActionResponse response = new AgentActionResponse();
        response.setActionOrder(actionOrder);
        response.setTraceStepOrder(trace.getStepOrder());
        response.setType(ACTION_TYPE_SKILL_CALL);
        response.setName(stringValue(data.get("skillName")));
        response.setStatus(trace.getStatus());
        response.setInput(data.get("params"));
        response.setObservation(data.get("result"));
        response.setErrorMessage(blankToNull(stringValue(data.get("errorMessage"))));
        response.setDurationMs(trace.getDurationMs());
        response.setTraceTimestamp(trace.getTraceTimestamp());
        response.setCreatedAt(trace.getCreatedAt());
        return response;
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
