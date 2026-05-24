package com.sharon.agentplatform.agent.history.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.agent.core.AgentTrace;
import com.sharon.agentplatform.agent.history.dto.AgentRunDetailResponse;
import com.sharon.agentplatform.agent.history.dto.AgentRunSummaryResponse;
import com.sharon.agentplatform.agent.history.dto.AgentRunTraceResponse;
import com.sharon.agentplatform.agent.history.entity.AgentRunEntity;
import com.sharon.agentplatform.agent.history.entity.AgentRunTraceEntity;
import com.sharon.agentplatform.agent.history.repository.AgentRunRepository;
import com.sharon.agentplatform.agent.history.repository.AgentRunTraceRepository;
import com.sharon.agentplatform.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgentRunHistoryService {

    private static final Logger log = LoggerFactory.getLogger(AgentRunHistoryService.class);

    private final AgentRunRepository agentRunRepository;
    private final AgentRunTraceRepository agentRunTraceRepository;
    private final ObjectMapper objectMapper;

    public AgentRunHistoryService(AgentRunRepository agentRunRepository,
                                  AgentRunTraceRepository agentRunTraceRepository,
                                  ObjectMapper objectMapper) {
        this.agentRunRepository = agentRunRepository;
        this.agentRunTraceRepository = agentRunTraceRepository;
        this.objectMapper = objectMapper;
    }

    public void saveRun(String conversationId,
                        String modelId,
                        String userMessage,
                        String answer,
                        String usedModel,
                        List<String> usedSkills,
                        List<AgentTrace> trace,
                        String status,
                        String errorMessage,
                        LocalDateTime startedAt,
                        LocalDateTime finishedAt,
                        Long durationMs) {
        try {
            String runId = UUID.randomUUID().toString().replace("-", "");
            LocalDateTime now = LocalDateTime.now();

            AgentRunEntity run = new AgentRunEntity();
            run.setRunId(runId);
            run.setConversationId(conversationId);
            run.setModelId(modelId);
            run.setUserMessage(userMessage);
            run.setAnswer(answer);
            run.setUsedModel(usedModel);
            run.setUsedSkillsJson(toJson(usedSkills == null ? List.of() : usedSkills));
            run.setStatus(status);
            run.setErrorMessage(errorMessage);
            run.setStartedAt(startedAt);
            run.setFinishedAt(finishedAt);
            run.setDurationMs(durationMs);
            run.setCreatedAt(now);
            run.setUpdatedAt(now);
            agentRunRepository.save(run);

            List<AgentRunTraceEntity> traceEntities = new ArrayList<>();
            if (trace != null) {
                int stepOrder = 1;
                for (AgentTrace item : trace) {
                    AgentRunTraceEntity traceEntity = new AgentRunTraceEntity();
                    traceEntity.setRunId(runId);
                    traceEntity.setStepOrder(stepOrder++);
                    traceEntity.setStep(item.getStep() == null ? "" : item.getStep().name());
                    traceEntity.setStatus(item.getStatus() == null ? "" : item.getStatus().name());
                    traceEntity.setDetail(trim(item.getDetail(), 1000));
                    traceEntity.setDataJson(toJsonSafely(item.getData()));
                    traceEntity.setDurationMs(item.getDurationMs());
                    traceEntity.setTraceTimestamp(item.getTimestamp());
                    traceEntity.setCreatedAt(now);
                    traceEntities.add(traceEntity);
                }
            }
            agentRunTraceRepository.saveAll(traceEntities);
        } catch (Exception exception) {
            log.warn("Failed to save agent run history", exception);
        }
    }

    public List<AgentRunSummaryResponse> listRecentRuns() {
        return agentRunRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public AgentRunDetailResponse getRunDetail(String runId) {
        AgentRunEntity run = agentRunRepository.findByRunId(runId)
                .orElseThrow(() -> new BusinessException("Agent run not found: " + runId));
        List<AgentRunTraceResponse> trace = agentRunTraceRepository.findByRunIdOrderByStepOrderAsc(runId)
                .stream()
                .map(this::toTraceResponse)
                .toList();

        AgentRunDetailResponse response = new AgentRunDetailResponse();
        response.setRunId(run.getRunId());
        response.setConversationId(run.getConversationId());
        response.setModelId(run.getModelId());
        response.setUserMessage(run.getUserMessage());
        response.setAnswer(run.getAnswer());
        response.setUsedModel(run.getUsedModel());
        response.setUsedSkills(parseUsedSkills(run.getUsedSkillsJson()));
        response.setStatus(run.getStatus());
        response.setErrorMessage(run.getErrorMessage());
        response.setDurationMs(run.getDurationMs());
        response.setStartedAt(run.getStartedAt());
        response.setFinishedAt(run.getFinishedAt());
        response.setCreatedAt(run.getCreatedAt());
        response.setTrace(trace);
        return response;
    }

    private AgentRunSummaryResponse toSummaryResponse(AgentRunEntity run) {
        AgentRunSummaryResponse response = new AgentRunSummaryResponse();
        response.setRunId(run.getRunId());
        response.setConversationId(run.getConversationId());
        response.setModelId(run.getModelId());
        response.setUserMessage(run.getUserMessage());
        response.setUsedModel(run.getUsedModel());
        response.setUsedSkills(parseUsedSkills(run.getUsedSkillsJson()));
        response.setStatus(run.getStatus());
        response.setErrorMessage(run.getErrorMessage());
        response.setDurationMs(run.getDurationMs());
        response.setStartedAt(run.getStartedAt());
        response.setFinishedAt(run.getFinishedAt());
        response.setCreatedAt(run.getCreatedAt());
        return response;
    }

    private AgentRunTraceResponse toTraceResponse(AgentRunTraceEntity trace) {
        AgentRunTraceResponse response = new AgentRunTraceResponse();
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

    private String toJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private String toJsonSafely(Object value) {
        try {
            return toJson(value == null ? "" : value);
        } catch (Exception exception) {
            return "{\"serializationError\":\"" + escapeJson(exception.getMessage()) + "\",\"fallback\":\"" + escapeJson(String.valueOf(value)) + "\"}";
        }
    }

    private List<String> parseUsedSkills(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception exception) {
            return List.of(json);
        }
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

    private String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
