package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.workflow.dto.AgentActionObservationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentActionResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionViewResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunReflectionResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowStageResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentRunReflectionService {

    private static final String STATUS_SUCCESS = "SUCCESS";

    private final AgentWorkflowService agentWorkflowService;
    private final AgentDecisionService agentDecisionService;
    private final AgentActionObservationService agentActionObservationService;

    public AgentRunReflectionService(AgentWorkflowService agentWorkflowService,
                                     AgentDecisionService agentDecisionService,
                                     AgentActionObservationService agentActionObservationService) {
        this.agentWorkflowService = agentWorkflowService;
        this.agentDecisionService = agentDecisionService;
        this.agentActionObservationService = agentActionObservationService;
    }

    public AgentRunReflectionResponse reflect(String runId) {
        AgentWorkflowResponse workflow = agentWorkflowService.getWorkflow(runId);
        AgentDecisionViewResponse decisions = agentDecisionService.getDecisions(runId);
        AgentActionObservationResponse actions = agentActionObservationService.getActions(runId);

        AgentRunReflectionResponse response = new AgentRunReflectionResponse();
        response.setRunId(workflow.getRunId());
        response.setConversationId(workflow.getConversationId());
        response.setModelId(workflow.getModelId());
        response.setStatus(workflow.getStatus());
        response.setCreatedAt(workflow.getCreatedAt());
        response.setWhatWentWell(buildWhatWentWell(workflow, decisions, actions));
        response.setWhatNeedsAttention(buildWhatNeedsAttention(workflow, decisions, actions));
        response.setSuggestedNextSteps(buildSuggestedNextSteps(workflow, decisions, actions));
        return response;
    }

    private List<String> buildWhatWentWell(AgentWorkflowResponse workflow,
                                           AgentDecisionViewResponse decisions,
                                           AgentActionObservationResponse actions) {
        List<String> items = new ArrayList<>();
        for (AgentWorkflowStageResponse stage : safeStages(workflow)) {
            if (STATUS_SUCCESS.equals(stage.getStatus())) {
                items.add(stage.getStage() + " 阶段执行成功");
            }
        }
        if (decisions.getDecisionCount() != null && decisions.getDecisionCount() > 0) {
            items.add("保留了 " + decisions.getDecisionCount() + " 个决策事件，便于解释 Agent 为什么这样做");
        }
        for (AgentActionResponse action : safeActions(actions)) {
            if (STATUS_SUCCESS.equals(action.getStatus())) {
                items.add(action.getName() + " Skill 调用成功，并记录了输入和观察结果");
            }
        }
        if (items.isEmpty()) {
            items.add("本次运行没有可确认的成功阶段，需要查看原始 trace");
        }
        return items;
    }

    private List<String> buildWhatNeedsAttention(AgentWorkflowResponse workflow,
                                                 AgentDecisionViewResponse decisions,
                                                 AgentActionObservationResponse actions) {
        List<String> items = new ArrayList<>();
        if (!STATUS_SUCCESS.equals(workflow.getStatus())) {
            items.add("Agent Run 整体状态不是 SUCCESS：" + workflow.getStatus());
        }
        for (AgentWorkflowStageResponse stage : safeStages(workflow)) {
            if (!STATUS_SUCCESS.equals(stage.getStatus())) {
                items.add(stage.getStage() + " 阶段存在失败或跳过步骤");
            }
        }
        for (AgentDecisionResponse decision : safeDecisions(decisions)) {
            if (!STATUS_SUCCESS.equals(decision.getStatus())) {
                items.add("决策事件未成功：" + nullToEmpty(decision.getSummary()));
            }
            if (decision.getMissingParams() != null) {
                items.add("存在缺失参数或追问：" + decision.getMissingParams());
            }
        }
        for (AgentActionResponse action : safeActions(actions)) {
            if (!STATUS_SUCCESS.equals(action.getStatus())) {
                items.add(action.getName() + " Skill 调用失败：" + nullToEmpty(action.getErrorMessage()));
            }
        }
        if (items.isEmpty()) {
            items.add("本次没有发现失败步骤或明显风险");
        }
        return items;
    }

    private List<String> buildSuggestedNextSteps(AgentWorkflowResponse workflow,
                                                 AgentDecisionViewResponse decisions,
                                                 AgentActionObservationResponse actions) {
        List<String> items = new ArrayList<>();
        if (!STATUS_SUCCESS.equals(workflow.getStatus())) {
            items.add("优先查看 Run Detail 和 Workflow View，定位失败阶段");
        }
        if (hasMissingParams(decisions)) {
            items.add("如果存在缺参追问，下一轮应按参数名补充缺失值");
        }
        if (hasFailedAction(actions)) {
            items.add("如果 Skill 调用失败，优先查看 Action / Observation View 中的 input、observation 和 errorMessage");
        }
        if (actions.getActionCount() != null && actions.getActionCount() > 0) {
            items.add("可以继续观察后续 Run 的 Skill 调用耗时和失败率");
        }
        if (items.isEmpty()) {
            items.add("可以继续使用 Explain / Workflow / Decisions / Actions 视图复盘类似请求");
        }
        return items;
    }

    private boolean hasMissingParams(AgentDecisionViewResponse decisions) {
        for (AgentDecisionResponse decision : safeDecisions(decisions)) {
            if (decision.getMissingParams() != null) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFailedAction(AgentActionObservationResponse actions) {
        for (AgentActionResponse action : safeActions(actions)) {
            if (!STATUS_SUCCESS.equals(action.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private List<AgentWorkflowStageResponse> safeStages(AgentWorkflowResponse workflow) {
        return workflow.getStages() == null ? List.of() : workflow.getStages();
    }

    private List<AgentDecisionResponse> safeDecisions(AgentDecisionViewResponse decisions) {
        return decisions.getDecisions() == null ? List.of() : decisions.getDecisions();
    }

    private List<AgentActionResponse> safeActions(AgentActionObservationResponse actions) {
        return actions.getActions() == null ? List.of() : actions.getActions();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
