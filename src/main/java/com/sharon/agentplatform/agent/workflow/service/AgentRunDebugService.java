package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.workflow.dto.AgentRunDebugResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunDebugSummaryResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunPlanResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunTimelineResponse;
import com.sharon.agentplatform.agent.workflow.model.AgentAction;
import com.sharon.agentplatform.agent.workflow.model.AgentDecision;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowRun;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowStage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentRunDebugService {

    private static final String STATUS_SUCCESS = "SUCCESS";

    private final AgentWorkflowModelService agentWorkflowModelService;
    private final AgentRunPlanService agentRunPlanService;
    private final AgentRunTimelineService agentRunTimelineService;

    public AgentRunDebugService(AgentWorkflowModelService agentWorkflowModelService,
                                AgentRunPlanService agentRunPlanService,
                                AgentRunTimelineService agentRunTimelineService) {
        this.agentWorkflowModelService = agentWorkflowModelService;
        this.agentRunPlanService = agentRunPlanService;
        this.agentRunTimelineService = agentRunTimelineService;
    }

    public AgentRunDebugResponse debug(String runId) {
        AgentWorkflowRun workflowRun = agentWorkflowModelService.getWorkflowRun(runId);
        AgentRunPlanResponse plan = agentRunPlanService.getPlan(runId);
        AgentRunTimelineResponse timeline = agentRunTimelineService.getTimeline(runId);

        AgentRunDebugSummaryResponse summary = buildSummary(workflowRun);

        AgentRunDebugResponse response = new AgentRunDebugResponse();
        response.setRunId(workflowRun.getRunId());
        response.setConversationId(workflowRun.getConversationId());
        response.setModelId(workflowRun.getModelId());
        response.setStatus(workflowRun.getStatus());
        response.setUserMessage(workflowRun.getUserMessage());
        response.setAnswer(workflowRun.getAnswer());
        response.setErrorMessage(workflowRun.getErrorMessage());
        response.setDurationMs(workflowRun.getDurationMs());
        response.setCreatedAt(workflowRun.getCreatedAt());
        response.setSummary(summary);
        response.setDebugHints(buildDebugHints(workflowRun, summary));
        response.setReplayContext(buildReplayContext(workflowRun, plan, timeline));
        return response;
    }

    private AgentRunDebugSummaryResponse buildSummary(AgentWorkflowRun workflowRun) {
        AgentRunDebugSummaryResponse summary = new AgentRunDebugSummaryResponse();
        summary.setMemoryLoaded(hasStage(workflowRun, "MEMORY"));
        summary.setDecisionCount(safeDecisions(workflowRun).size());
        summary.setActionCount(safeActions(workflowRun).size());
        summary.setHasMissingParams(hasMissingParams(workflowRun));
        summary.setHasFailedAction(hasFailedAction(workflowRun));
        summary.setSelectedSkill(firstSelectedSkill(workflowRun));
        summary.setResolvedParams(firstResolvedParams(workflowRun));
        summary.setMissingParams(firstMissingParams(workflowRun));
        summary.setActionObservation(firstActionObservation(workflowRun));
        return summary;
    }

    private List<String> buildDebugHints(AgentWorkflowRun workflowRun, AgentRunDebugSummaryResponse summary) {
        List<String> hints = new ArrayList<>();
        if (!STATUS_SUCCESS.equals(workflowRun.getStatus())) {
            hints.add("Run 状态不是 SUCCESS，优先查看 errorMessage 和失败阶段");
        }
        if (Boolean.TRUE.equals(summary.getHasMissingParams())) {
            hints.add("存在 missingParams，本次可能进入 pending 追问，下一轮应补充缺失参数");
        }
        if (Boolean.TRUE.equals(summary.getHasFailedAction())) {
            hints.add("存在失败的 Skill action，优先查看 replayContext.timeline 中 ACTION / OBSERVATION 的 data");
        }
        if (summary.getDecisionCount() != null && summary.getDecisionCount() == 0) {
            hints.add("没有记录 decision 事件，检查 INTENT_DETECTION 或 SELECT_SKILL trace 是否生成");
        }
        if (summary.getActionCount() != null && summary.getActionCount() == 0
                && !Boolean.TRUE.equals(summary.getHasMissingParams())) {
            hints.add("没有 Skill action，说明本次可能是普通聊天或未命中 Skill");
        }
        if (!Boolean.TRUE.equals(summary.getMemoryLoaded())) {
            hints.add("没有 MEMORY 阶段，检查 MemoryService 或 LOAD_MEMORY trace");
        }
        if (hints.isEmpty()) {
            hints.add("本次 run 没有明显异常，可结合 plan、timeline 和 reflection 继续复盘");
        }
        return hints;
    }

    private Map<String, Object> buildReplayContext(AgentWorkflowRun workflowRun,
                                                   AgentRunPlanResponse plan,
                                                   AgentRunTimelineResponse timeline) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("note", "This is a read-only replay context. It does not re-run model or Skill calls.");
        context.put("input", Map.of(
                "conversationId", nullToEmpty(workflowRun.getConversationId()),
                "modelId", nullToEmpty(workflowRun.getModelId()),
                "userMessage", nullToEmpty(workflowRun.getUserMessage())
        ));
        context.put("plan", plan);
        context.put("timeline", timeline);
        context.put("finalAnswer", workflowRun.getAnswer());
        context.put("reflection", workflowRun.getReflection());
        return context;
    }

    private boolean hasStage(AgentWorkflowRun workflowRun, String stageName) {
        if (workflowRun.getStages() == null) {
            return false;
        }
        for (AgentWorkflowStage stage : workflowRun.getStages()) {
            if (stageName.equals(stage.getStage())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMissingParams(AgentWorkflowRun workflowRun) {
        return firstMissingParams(workflowRun) != null;
    }

    private boolean hasFailedAction(AgentWorkflowRun workflowRun) {
        for (AgentAction action : safeActions(workflowRun)) {
            if (!STATUS_SUCCESS.equals(action.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private String firstSelectedSkill(AgentWorkflowRun workflowRun) {
        for (AgentAction action : safeActions(workflowRun)) {
            if (action.getName() != null && !action.getName().isBlank()) {
                return action.getName();
            }
        }
        for (AgentDecision decision : safeDecisions(workflowRun)) {
            if (decision.getSkillName() != null && !decision.getSkillName().isBlank()) {
                return decision.getSkillName();
            }
        }
        return null;
    }

    private Object firstResolvedParams(AgentWorkflowRun workflowRun) {
        for (AgentAction action : safeActions(workflowRun)) {
            if (action.getInput() != null) {
                return action.getInput();
            }
        }
        for (AgentDecision decision : safeDecisions(workflowRun)) {
            if (decision.getParams() != null) {
                return decision.getParams();
            }
        }
        return null;
    }

    private Object firstMissingParams(AgentWorkflowRun workflowRun) {
        for (AgentDecision decision : safeDecisions(workflowRun)) {
            if (decision.getMissingParams() != null) {
                return decision.getMissingParams();
            }
        }
        return null;
    }

    private Object firstActionObservation(AgentWorkflowRun workflowRun) {
        for (AgentAction action : safeActions(workflowRun)) {
            if (action.getObservation() != null) {
                return action.getObservation().getData();
            }
        }
        return null;
    }

    private List<AgentDecision> safeDecisions(AgentWorkflowRun workflowRun) {
        return workflowRun.getDecisions() == null ? List.of() : workflowRun.getDecisions();
    }

    private List<AgentAction> safeActions(AgentWorkflowRun workflowRun) {
        return workflowRun.getActions() == null ? List.of() : workflowRun.getActions();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
