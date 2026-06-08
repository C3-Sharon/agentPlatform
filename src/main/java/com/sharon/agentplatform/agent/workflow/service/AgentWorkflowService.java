package com.sharon.agentplatform.agent.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.agent.history.entity.AgentRunEntity;
import com.sharon.agentplatform.agent.history.entity.AgentRunTraceEntity;
import com.sharon.agentplatform.agent.history.repository.AgentRunRepository;
import com.sharon.agentplatform.agent.history.repository.AgentRunTraceRepository;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowStageResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowStepResponse;
import com.sharon.agentplatform.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentWorkflowService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final AgentRunRepository agentRunRepository;
    private final AgentRunTraceRepository agentRunTraceRepository;
    private final ObjectMapper objectMapper;

    public AgentWorkflowService(AgentRunRepository agentRunRepository,
                                AgentRunTraceRepository agentRunTraceRepository,
                                ObjectMapper objectMapper) {
        this.agentRunRepository = agentRunRepository;
        this.agentRunTraceRepository = agentRunTraceRepository;
        this.objectMapper = objectMapper;
    }

    public AgentWorkflowResponse getWorkflow(String runId) {
        AgentRunEntity run = agentRunRepository.findByRunId(runId)
                .orElseThrow(() -> new BusinessException("Agent run not found: " + runId));
        List<AgentRunTraceEntity> traces = agentRunTraceRepository.findByRunIdOrderByStepOrderAsc(runId);

        AgentWorkflowResponse response = new AgentWorkflowResponse();
        response.setRunId(run.getRunId());
        response.setConversationId(run.getConversationId());
        response.setModelId(run.getModelId());
        response.setUserMessage(run.getUserMessage());
        response.setAnswer(run.getAnswer());
        response.setStatus(run.getStatus());
        response.setErrorMessage(run.getErrorMessage());
        response.setDurationMs(run.getDurationMs());
        response.setCreatedAt(run.getCreatedAt());
        response.setStages(buildStages(traces));
        return response;
    }

    private List<AgentWorkflowStageResponse> buildStages(List<AgentRunTraceEntity> traces) {
        Map<String, List<AgentWorkflowStepResponse>> grouped = new LinkedHashMap<>();
        for (String stage : List.of("MEMORY", "INTENT", "DECISION", "ACTION", "ANSWER", "OTHER")) {
            grouped.put(stage, new ArrayList<>());
        }

        for (AgentRunTraceEntity trace : traces) {
            grouped.get(classifyStage(trace.getStep())).add(toStepResponse(trace));
        }

        List<AgentWorkflowStageResponse> stages = new ArrayList<>();
        for (Map.Entry<String, List<AgentWorkflowStepResponse>> entry : grouped.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                stages.add(toStageResponse(entry.getKey(), entry.getValue()));
            }
        }
        return stages;
    }

    private AgentWorkflowStageResponse toStageResponse(String stage, List<AgentWorkflowStepResponse> steps) {
        AgentWorkflowStageResponse response = new AgentWorkflowStageResponse();
        response.setStage(stage);
        response.setStatus(stageStatus(steps));
        response.setSummary(stageSummary(stage, steps));
        response.setStartedAt(firstTime(steps));
        response.setFinishedAt(lastTime(steps));
        response.setDurationMs(totalDuration(steps));
        response.setSteps(steps);
        return response;
    }

    private AgentWorkflowStepResponse toStepResponse(AgentRunTraceEntity trace) {
        AgentWorkflowStepResponse response = new AgentWorkflowStepResponse();
        response.setStepOrder(trace.getStepOrder());
        response.setStep(trace.getStep());
        response.setStatus(trace.getStatus());
        response.setDetail(trace.getDetail());
        response.setData(parseData(trace.getDataJson()));
        response.setDurationMs(trace.getDurationMs());
        response.setTraceTimestamp(trace.getTraceTimestamp());
        response.setCreatedAt(trace.getCreatedAt());
        return response;
    }

    private String classifyStage(String step) {
        if ("LOAD_MEMORY".equals(step) || "SAVE_MEMORY".equals(step)) {
            return "MEMORY";
        }
        if ("INTENT_DETECTION".equals(step)) {
            return "INTENT";
        }
        if ("SELECT_SKILL".equals(step) || "PARAM_RESOLUTION".equals(step)) {
            return "DECISION";
        }
        if ("CALL_SKILL".equals(step)) {
            return "ACTION";
        }
        if ("GENERATE_ANSWER".equals(step)) {
            return "ANSWER";
        }
        return "OTHER";
    }

    private String stageStatus(List<AgentWorkflowStepResponse> steps) {
        for (AgentWorkflowStepResponse step : steps) {
            if (!STATUS_SUCCESS.equals(step.getStatus())) {
                return STATUS_FAILED;
            }
        }
        return STATUS_SUCCESS;
    }

    private String stageSummary(String stage, List<AgentWorkflowStepResponse> steps) {
        return switch (stage) {
            case "MEMORY" -> "Loaded or saved short-term and long-term memory";
            case "INTENT" -> "Detected user intent and potential Skill usage";
            case "DECISION" -> "Selected Skill and resolved required parameters";
            case "ACTION" -> "Executed selected Skill or platform action";
            case "ANSWER" -> "Generated final response for the user";
            default -> "Captured additional Agent runtime trace steps";
        };
    }

    private LocalDateTime firstTime(List<AgentWorkflowStepResponse> steps) {
        return steps.stream()
                .map(this::stepTime)
                .filter(time -> time != null)
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime lastTime(List<AgentWorkflowStepResponse> steps) {
        LocalDateTime last = null;
        for (AgentWorkflowStepResponse step : steps) {
            LocalDateTime time = stepTime(step);
            if (time != null) {
                last = time;
            }
        }
        return last;
    }

    private LocalDateTime stepTime(AgentWorkflowStepResponse step) {
        return step.getTraceTimestamp() == null ? step.getCreatedAt() : step.getTraceTimestamp();
    }

    private Long totalDuration(List<AgentWorkflowStepResponse> steps) {
        long total = 0L;
        boolean hasDuration = false;
        for (AgentWorkflowStepResponse step : steps) {
            if (step.getDurationMs() != null) {
                total += step.getDurationMs();
                hasDuration = true;
            }
        }
        return hasDuration ? total : null;
    }

    private Object parseData(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception exception) {
            return json;
        }
    }
}
