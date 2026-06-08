package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.workflow.dto.AgentActionObservationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentActionResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionViewResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunExplanationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowStageResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentRunExplanationService {

    private static final String STATUS_SUCCESS = "SUCCESS";

    private final AgentWorkflowService agentWorkflowService;
    private final AgentDecisionService agentDecisionService;
    private final AgentActionObservationService agentActionObservationService;

    public AgentRunExplanationService(AgentWorkflowService agentWorkflowService,
                                      AgentDecisionService agentDecisionService,
                                      AgentActionObservationService agentActionObservationService) {
        this.agentWorkflowService = agentWorkflowService;
        this.agentDecisionService = agentDecisionService;
        this.agentActionObservationService = agentActionObservationService;
    }

    public AgentRunExplanationResponse explain(String runId) {
        AgentWorkflowResponse workflow = agentWorkflowService.getWorkflow(runId);
        AgentDecisionViewResponse decisions = agentDecisionService.getDecisions(runId);
        AgentActionObservationResponse actions = agentActionObservationService.getActions(runId);

        AgentRunExplanationResponse response = new AgentRunExplanationResponse();
        response.setRunId(workflow.getRunId());
        response.setConversationId(workflow.getConversationId());
        response.setModelId(workflow.getModelId());
        response.setStatus(workflow.getStatus());
        response.setCreatedAt(workflow.getCreatedAt());
        response.setHighlights(buildHighlights(workflow, decisions, actions));
        response.setRiskNotes(buildRiskNotes(workflow, decisions, actions));
        response.setSummary(buildSummary(workflow, decisions, actions));
        return response;
    }

    private String buildSummary(AgentWorkflowResponse workflow,
                                AgentDecisionViewResponse decisions,
                                AgentActionObservationResponse actions) {
        String intent = firstIntent(decisions);
        String skill = firstActionName(actions);
        if (skill != null) {
            return "本次 Agent 先加载记忆，然后识别用户意图"
                    + (intent == null ? "" : "（" + intent + "）")
                    + "，选择并调用 " + skill + " Skill，最后生成回答并保存记忆";
        }
        return "本次 Agent 先加载记忆，然后识别用户意图"
                + (intent == null ? "" : "（" + intent + "）")
                + "，未调用 Skill，直接生成回答并保存记忆";
    }

    private List<String> buildHighlights(AgentWorkflowResponse workflow,
                                         AgentDecisionViewResponse decisions,
                                         AgentActionObservationResponse actions) {
        List<String> highlights = new ArrayList<>();
        if (hasStage(workflow, "MEMORY")) {
            highlights.add("读取或保存会话记忆");
        }
        if (decisions.getDecisionCount() != null && decisions.getDecisionCount() > 0) {
            highlights.add("记录了 " + decisions.getDecisionCount() + " 个决策事件");
        }
        for (AgentDecisionResponse decision : safeDecisions(decisions)) {
            if (decision.getSkillName() != null && !decision.getSkillName().isBlank()) {
                highlights.add("决策阶段涉及 Skill：" + decision.getSkillName());
                break;
            }
        }
        if (actions.getActionCount() != null && actions.getActionCount() > 0) {
            highlights.add("执行了 " + actions.getActionCount() + " 个 Agent 动作");
        }
        for (AgentActionResponse action : safeActions(actions)) {
            if (STATUS_SUCCESS.equals(action.getStatus())) {
                highlights.add(action.getName() + " Skill 调用成功");
            } else {
                highlights.add(action.getName() + " Skill 调用未成功");
            }
        }
        if (hasStage(workflow, "ANSWER")) {
            highlights.add("生成最终回答");
        }
        return highlights;
    }

    private List<String> buildRiskNotes(AgentWorkflowResponse workflow,
                                        AgentDecisionViewResponse decisions,
                                        AgentActionObservationResponse actions) {
        List<String> risks = new ArrayList<>();
        if (!STATUS_SUCCESS.equals(workflow.getStatus())) {
            risks.add("本次 Agent Run 状态不是 SUCCESS：" + workflow.getStatus());
        }
        for (AgentWorkflowStageResponse stage : safeStages(workflow)) {
            if (!STATUS_SUCCESS.equals(stage.getStatus())) {
                risks.add(stage.getStage() + " 阶段存在失败或跳过步骤");
            }
        }
        for (AgentDecisionResponse decision : safeDecisions(decisions)) {
            if (!STATUS_SUCCESS.equals(decision.getStatus())) {
                risks.add("决策事件未成功：" + nullToEmpty(decision.getSummary()));
            }
            if (decision.getMissingParams() != null) {
                risks.add("存在缺失参数或追问：" + decision.getMissingParams());
            }
        }
        for (AgentActionResponse action : safeActions(actions)) {
            if (!STATUS_SUCCESS.equals(action.getStatus())) {
                risks.add(action.getName() + " Skill 调用失败：" + nullToEmpty(action.getErrorMessage()));
            }
        }
        if (risks.isEmpty()) {
            risks.add("本次没有发现失败步骤");
        }
        return risks;
    }

    private boolean hasStage(AgentWorkflowResponse workflow, String stageName) {
        for (AgentWorkflowStageResponse stage : safeStages(workflow)) {
            if (stageName.equals(stage.getStage())) {
                return true;
            }
        }
        return false;
    }

    private String firstIntent(AgentDecisionViewResponse decisions) {
        for (AgentDecisionResponse decision : safeDecisions(decisions)) {
            if (decision.getIntent() != null && !decision.getIntent().isBlank()) {
                return decision.getIntent();
            }
        }
        return null;
    }

    private String firstActionName(AgentActionObservationResponse actions) {
        for (AgentActionResponse action : safeActions(actions)) {
            if (action.getName() != null && !action.getName().isBlank()) {
                return action.getName();
            }
        }
        return null;
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
