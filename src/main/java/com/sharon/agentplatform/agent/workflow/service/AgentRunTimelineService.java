package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.workflow.dto.AgentRunPlanResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunTimelineResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentTimelineItemResponse;
import com.sharon.agentplatform.agent.workflow.model.AgentAction;
import com.sharon.agentplatform.agent.workflow.model.AgentDecision;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowRun;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowStage;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowStep;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentRunTimelineService {

    private final AgentWorkflowModelService agentWorkflowModelService;
    private final AgentRunPlanService agentRunPlanService;

    public AgentRunTimelineService(AgentWorkflowModelService agentWorkflowModelService,
                                   AgentRunPlanService agentRunPlanService) {
        this.agentWorkflowModelService = agentWorkflowModelService;
        this.agentRunPlanService = agentRunPlanService;
    }

    public AgentRunTimelineResponse getTimeline(String runId) {
        AgentWorkflowRun workflowRun = agentWorkflowModelService.getWorkflowRun(runId);
        AgentRunPlanResponse plan = agentRunPlanService.getPlan(runId);
        List<AgentTimelineItemResponse> items = buildItems(workflowRun, plan);

        AgentRunTimelineResponse response = new AgentRunTimelineResponse();
        response.setRunId(workflowRun.getRunId());
        response.setConversationId(workflowRun.getConversationId());
        response.setModelId(workflowRun.getModelId());
        response.setStatus(workflowRun.getStatus());
        response.setCreatedAt(workflowRun.getCreatedAt());
        response.setItemCount(items.size());
        response.setItems(items);
        return response;
    }

    private List<AgentTimelineItemResponse> buildItems(AgentWorkflowRun workflowRun, AgentRunPlanResponse plan) {
        List<AgentTimelineItemResponse> items = new ArrayList<>();
        int order = 1;

        items.add(item(order++, "PLAN", "PLAN_SUMMARY", "运行计划", plan.getSummary(),
                workflowRun.getStatus(), workflowRun.getCreatedAt(), null, Map.of("steps", plan.getSteps())));

        for (AgentWorkflowStage stage : safeStages(workflowRun.getStages())) {
            items.add(item(order++, stage.getStage(), "STAGE", stage.getStage() + " 阶段",
                    stage.getSummary(), stage.getStatus(), stage.getStartedAt(), stage.getDurationMs(), null));

            for (AgentWorkflowStep step : safeSteps(stage.getSteps())) {
                items.add(item(order++, stage.getStage(), "TRACE_STEP", step.getStep(),
                        step.getDetail(), step.getStatus(), stepTime(step), step.getDurationMs(), step.getData()));
            }
        }

        for (AgentDecision decision : safeDecisions(workflowRun.getDecisions())) {
            items.add(item(order++, "DECISION", decision.getType(), decisionTitle(decision),
                    decisionSummary(decision), decision.getStatus(), decisionTime(decision), decision.getDurationMs(), decisionData(decision)));
        }

        for (AgentAction action : safeActions(workflowRun.getActions())) {
            items.add(item(order++, "ACTION", action.getType(), actionTitle(action),
                    "执行 Agent 动作：" + nullToEmpty(action.getName()), action.getStatus(),
                    actionTime(action), action.getDurationMs(), actionData(action)));

            items.add(item(order++, "OBSERVATION", "OBSERVATION", observationTitle(action),
                    observationSummary(action), action.getStatus(), actionTime(action), null,
                    action.getObservation() == null ? null : action.getObservation().getData()));
        }

        if (workflowRun.getReflection() != null) {
            items.add(item(order, "REFLECTION", "REFLECTION", "运行复盘",
                    "基于本次 run 生成规则式复盘建议", "SUCCESS", workflowRun.getCreatedAt(), null,
                    reflectionData(workflowRun)));
        }

        return items;
    }

    private AgentTimelineItemResponse item(Integer order,
                                           String stage,
                                           String type,
                                           String title,
                                           String summary,
                                           String status,
                                           LocalDateTime timestamp,
                                           Long durationMs,
                                           Object data) {
        AgentTimelineItemResponse response = new AgentTimelineItemResponse();
        response.setOrder(order);
        response.setStage(stage);
        response.setType(type);
        response.setTitle(title);
        response.setSummary(summary);
        response.setStatus(status);
        response.setTimestamp(timestamp);
        response.setDurationMs(durationMs);
        response.setData(data);
        return response;
    }

    private String decisionTitle(AgentDecision decision) {
        if (decision.getSkillName() != null && !decision.getSkillName().isBlank()) {
            return decision.getType() + "：" + decision.getSkillName();
        }
        return decision.getType();
    }

    private String decisionSummary(AgentDecision decision) {
        if (decision.getMissingParams() != null) {
            return "缺少 required 参数：" + decision.getMissingParams();
        }
        if (decision.getIntent() != null && !decision.getIntent().isBlank()) {
            return "识别意图：" + decision.getIntent();
        }
        if (decision.getSummary() != null && !decision.getSummary().isBlank()) {
            return decision.getSummary();
        }
        return "记录 Agent 决策事件";
    }

    private Map<String, Object> decisionData(AgentDecision decision) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("source", decision.getSource());
        data.put("intent", decision.getIntent());
        data.put("needSkill", decision.getNeedSkill());
        data.put("skillName", decision.getSkillName());
        data.put("params", decision.getParams());
        data.put("missingParams", decision.getMissingParams());
        data.put("reason", decision.getReason());
        data.put("pendingStore", decision.getPendingStore());
        data.put("rawData", decision.getRawData());
        return data;
    }

    private String actionTitle(AgentAction action) {
        return action.getName() == null || action.getName().isBlank()
                ? "Skill 调用"
                : "调用 Skill：" + action.getName();
    }

    private Map<String, Object> actionData(AgentAction action) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("input", action.getInput());
        data.put("observation", action.getObservation() == null ? null : action.getObservation().getData());
        data.put("errorMessage", action.getObservation() == null ? null : action.getObservation().getErrorMessage());
        return data;
    }

    private String observationTitle(AgentAction action) {
        return action.getName() == null || action.getName().isBlank()
                ? "观察 Skill 结果"
                : "观察 " + action.getName() + " 结果";
    }

    private String observationSummary(AgentAction action) {
        if (action.getObservation() != null && action.getObservation().getErrorMessage() != null) {
            return "Skill 返回错误：" + action.getObservation().getErrorMessage();
        }
        return "记录 Skill 返回结果";
    }

    private Map<String, Object> reflectionData(AgentWorkflowRun workflowRun) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("whatWentWell", workflowRun.getReflection().getWhatWentWell());
        data.put("whatNeedsAttention", workflowRun.getReflection().getWhatNeedsAttention());
        data.put("suggestedNextSteps", workflowRun.getReflection().getSuggestedNextSteps());
        return data;
    }

    private LocalDateTime stepTime(AgentWorkflowStep step) {
        return step.getTraceTimestamp() == null ? step.getCreatedAt() : step.getTraceTimestamp();
    }

    private LocalDateTime decisionTime(AgentDecision decision) {
        return decision.getTraceTimestamp() == null ? decision.getCreatedAt() : decision.getTraceTimestamp();
    }

    private LocalDateTime actionTime(AgentAction action) {
        return action.getTraceTimestamp() == null ? action.getCreatedAt() : action.getTraceTimestamp();
    }

    private List<AgentWorkflowStage> safeStages(List<AgentWorkflowStage> stages) {
        return stages == null ? List.of() : stages;
    }

    private List<AgentWorkflowStep> safeSteps(List<AgentWorkflowStep> steps) {
        return steps == null ? List.of() : steps;
    }

    private List<AgentDecision> safeDecisions(List<AgentDecision> decisions) {
        return decisions == null ? List.of() : decisions;
    }

    private List<AgentAction> safeActions(List<AgentAction> actions) {
        return actions == null ? List.of() : actions;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
