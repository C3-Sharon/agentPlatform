package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.workflow.dto.AgentPlanStepResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunPlanResponse;
import com.sharon.agentplatform.agent.workflow.model.AgentAction;
import com.sharon.agentplatform.agent.workflow.model.AgentDecision;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowRun;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowStage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgentRunPlanService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String PLAN_COMPLETED = "COMPLETED";
    private static final String PLAN_FAILED = "FAILED";
    private static final String PLAN_WAITING = "WAITING_FOR_USER";
    private static final String PLAN_SKIPPED = "SKIPPED";

    private final AgentWorkflowModelService agentWorkflowModelService;

    public AgentRunPlanService(AgentWorkflowModelService agentWorkflowModelService) {
        this.agentWorkflowModelService = agentWorkflowModelService;
    }

    public AgentRunPlanResponse getPlan(String runId) {
        AgentWorkflowRun workflowRun = agentWorkflowModelService.getWorkflowRun(runId);
        List<AgentPlanStepResponse> steps = buildPlanSteps(workflowRun);

        AgentRunPlanResponse response = new AgentRunPlanResponse();
        response.setRunId(workflowRun.getRunId());
        response.setConversationId(workflowRun.getConversationId());
        response.setModelId(workflowRun.getModelId());
        response.setStatus(workflowRun.getStatus());
        response.setCreatedAt(workflowRun.getCreatedAt());
        response.setSummary(buildSummary(workflowRun, steps));
        response.setSteps(steps);
        return response;
    }

    private List<AgentPlanStepResponse> buildPlanSteps(AgentWorkflowRun workflowRun) {
        List<AgentPlanStepResponse> steps = new ArrayList<>();
        int order = 1;

        steps.add(step(order++, "MEMORY", "加载记忆",
                "加载当前 conversationId 的短期记忆和长期记忆",
                stageStatus(workflowRun, "MEMORY"), stageEvidence(workflowRun, "MEMORY"), stageTime(workflowRun, "MEMORY")));

        steps.add(step(order++, "INTENT", "识别意图",
                "判断用户是普通聊天、显式 Skill 调用，还是需要由模型辅助决策",
                stageStatus(workflowRun, "INTENT"), stageEvidence(workflowRun, "INTENT"), stageTime(workflowRun, "INTENT")));

        steps.add(step(order++, "DECISION", "选择能力并解析参数",
                "根据意图选择 Skill 或直接回答，并解析 required 参数",
                stageStatus(workflowRun, "DECISION"), decisionEvidence(workflowRun), stageTime(workflowRun, "DECISION")));

        if (hasMissingParams(workflowRun)) {
            steps.add(step(order++, "PENDING", "等待用户补充参数",
                    "发现 required 参数缺失，保存 pending skill call 并向用户追问",
                    PLAN_WAITING, missingParamEvidence(workflowRun), firstDecisionTime(workflowRun)));
            steps.add(step(order++, "ACTION", "执行 Skill",
                    "参数补齐前不会实际调用 Skill",
                    PLAN_SKIPPED, "等待下一轮用户补参", null));
        } else if (hasActions(workflowRun)) {
            steps.add(step(order++, "ACTION", "执行 Skill",
                    "调用已选择的 Skill，并记录输入参数和返回观察结果",
                    stageStatus(workflowRun, "ACTION"), actionEvidence(workflowRun), stageTime(workflowRun, "ACTION")));
        } else {
            steps.add(step(order++, "ACTION", "执行 Skill",
                    "本次运行未选择 Skill，跳过外部动作执行",
                    PLAN_SKIPPED, "没有 CALL_SKILL trace", null));
        }

        steps.add(step(order++, "OBSERVATION", "观察执行结果",
                "整理 Skill 返回结果、错误信息或直接回答所需上下文",
                observationStatus(workflowRun), observationEvidence(workflowRun), stageTime(workflowRun, "ACTION")));

        steps.add(step(order++, "ANSWER", "生成最终回答",
                "根据模型输出或 Skill direct return 生成用户可读回答",
                stageStatus(workflowRun, "ANSWER"), stageEvidence(workflowRun, "ANSWER"), stageTime(workflowRun, "ANSWER")));

        steps.add(step(order, "REFLECTION", "复盘本次运行",
                "基于 Workflow、Decision、Action / Observation 生成规则式复盘建议",
                reflectionStatus(workflowRun), reflectionEvidence(workflowRun), workflowRun.getCreatedAt()));

        return steps;
    }

    private AgentPlanStepResponse step(Integer order,
                                       String phase,
                                       String title,
                                       String description,
                                       String status,
                                       String evidence,
                                       LocalDateTime observedAt) {
        AgentPlanStepResponse response = new AgentPlanStepResponse();
        response.setOrder(order);
        response.setPhase(phase);
        response.setTitle(title);
        response.setDescription(description);
        response.setStatus(status);
        response.setEvidence(evidence);
        response.setObservedAt(observedAt);
        return response;
    }

    private String buildSummary(AgentWorkflowRun workflowRun, List<AgentPlanStepResponse> steps) {
        if (hasMissingParams(workflowRun)) {
            return "本次计划在参数解析阶段发现缺失 required 参数，因此进入 pending 追问，等待用户补充后继续执行";
        }
        if (hasActions(workflowRun)) {
            return "本次计划完成了记忆加载、意图识别、能力选择、Skill 执行、结果观察、回答生成和复盘";
        }
        return "本次计划完成了记忆加载、意图识别、直接回答和复盘，没有执行 Skill 动作";
    }

    private String stageStatus(AgentWorkflowRun workflowRun, String stageName) {
        AgentWorkflowStage stage = findStage(workflowRun, stageName);
        if (stage == null) {
            return PLAN_SKIPPED;
        }
        return STATUS_SUCCESS.equals(stage.getStatus()) ? PLAN_COMPLETED : PLAN_FAILED;
    }

    private String stageEvidence(AgentWorkflowRun workflowRun, String stageName) {
        AgentWorkflowStage stage = findStage(workflowRun, stageName);
        if (stage == null) {
            return "没有 " + stageName + " 阶段 trace";
        }
        int count = stage.getSteps() == null ? 0 : stage.getSteps().size();
        return stage.getSummary() + "，trace step 数：" + count;
    }

    private LocalDateTime stageTime(AgentWorkflowRun workflowRun, String stageName) {
        AgentWorkflowStage stage = findStage(workflowRun, stageName);
        return stage == null ? null : stage.getStartedAt();
    }

    private AgentWorkflowStage findStage(AgentWorkflowRun workflowRun, String stageName) {
        if (workflowRun.getStages() == null) {
            return null;
        }
        for (AgentWorkflowStage stage : workflowRun.getStages()) {
            if (stageName.equals(stage.getStage())) {
                return stage;
            }
        }
        return null;
    }

    private String decisionEvidence(AgentWorkflowRun workflowRun) {
        if (workflowRun.getDecisions() == null || workflowRun.getDecisions().isEmpty()) {
            return "没有 decision trace";
        }
        for (AgentDecision decision : workflowRun.getDecisions()) {
            if (decision.getSkillName() != null && !decision.getSkillName().isBlank()) {
                return "选择或涉及 Skill：" + decision.getSkillName();
            }
        }
        return "决策事件数量：" + workflowRun.getDecisions().size();
    }

    private boolean hasMissingParams(AgentWorkflowRun workflowRun) {
        if (workflowRun.getDecisions() == null) {
            return false;
        }
        for (AgentDecision decision : workflowRun.getDecisions()) {
            if (decision.getMissingParams() != null) {
                return true;
            }
        }
        return false;
    }

    private String missingParamEvidence(AgentWorkflowRun workflowRun) {
        if (workflowRun.getDecisions() == null) {
            return "缺少参数信息未记录";
        }
        for (AgentDecision decision : workflowRun.getDecisions()) {
            if (decision.getMissingParams() != null) {
                return "missingParams=" + decision.getMissingParams();
            }
        }
        return "缺少参数信息未记录";
    }

    private LocalDateTime firstDecisionTime(AgentWorkflowRun workflowRun) {
        if (workflowRun.getDecisions() == null || workflowRun.getDecisions().isEmpty()) {
            return null;
        }
        return workflowRun.getDecisions().get(0).getTraceTimestamp() == null
                ? workflowRun.getDecisions().get(0).getCreatedAt()
                : workflowRun.getDecisions().get(0).getTraceTimestamp();
    }

    private boolean hasActions(AgentWorkflowRun workflowRun) {
        return workflowRun.getActions() != null && !workflowRun.getActions().isEmpty();
    }

    private String actionEvidence(AgentWorkflowRun workflowRun) {
        if (!hasActions(workflowRun)) {
            return "没有实际 Skill 调用";
        }
        List<String> names = new ArrayList<>();
        for (AgentAction action : workflowRun.getActions()) {
            if (action.getName() != null && !action.getName().isBlank()) {
                names.add(action.getName());
            }
        }
        return names.isEmpty() ? "Skill 调用数量：" + workflowRun.getActions().size() : "调用 Skill：" + String.join(", ", names);
    }

    private String observationStatus(AgentWorkflowRun workflowRun) {
        if (!hasActions(workflowRun)) {
            return PLAN_SKIPPED;
        }
        for (AgentAction action : workflowRun.getActions()) {
            if (!STATUS_SUCCESS.equals(action.getStatus())) {
                return PLAN_FAILED;
            }
        }
        return PLAN_COMPLETED;
    }

    private String observationEvidence(AgentWorkflowRun workflowRun) {
        if (!hasActions(workflowRun)) {
            return "没有 Skill observation";
        }
        for (AgentAction action : workflowRun.getActions()) {
            if (action.getObservation() != null && action.getObservation().getErrorMessage() != null) {
                return action.getName() + " errorMessage=" + action.getObservation().getErrorMessage();
            }
        }
        return "已记录 Skill result observation";
    }

    private String reflectionStatus(AgentWorkflowRun workflowRun) {
        if (workflowRun.getReflection() == null) {
            return PLAN_SKIPPED;
        }
        return PLAN_COMPLETED;
    }

    private String reflectionEvidence(AgentWorkflowRun workflowRun) {
        if (workflowRun.getReflection() == null) {
            return "没有 reflection";
        }
        int well = workflowRun.getReflection().getWhatWentWell() == null ? 0 : workflowRun.getReflection().getWhatWentWell().size();
        int attention = workflowRun.getReflection().getWhatNeedsAttention() == null ? 0 : workflowRun.getReflection().getWhatNeedsAttention().size();
        return "whatWentWell=" + well + ", whatNeedsAttention=" + attention;
    }
}
