package com.sharon.agentplatform.agent.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.agent.history.entity.AgentRunTraceEntity;
import com.sharon.agentplatform.agent.workflow.model.AgentAction;
import com.sharon.agentplatform.agent.workflow.model.AgentDecision;
import com.sharon.agentplatform.agent.workflow.model.AgentObservation;
import com.sharon.agentplatform.agent.workflow.model.AgentReflection;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowStage;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowStep;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentWorkflowTraceMapper {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STEP_INTENT_DETECTION = "INTENT_DETECTION";
    private static final String STEP_LLM_SKILL_DECISION = "LLM_SKILL_DECISION";
    private static final String STEP_SELECT_SKILL = "SELECT_SKILL";
    private static final String STEP_PARAM_RESOLUTION = "PARAM_RESOLUTION";
    private static final String STEP_CALL_SKILL = "CALL_SKILL";
    private static final String ACTION_TYPE_SKILL_CALL = "SKILL_CALL";

    private final ObjectMapper objectMapper;

    public AgentWorkflowTraceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<AgentWorkflowStage> buildStages(List<AgentRunTraceEntity> traces) {
        Map<String, List<AgentWorkflowStep>> grouped = new LinkedHashMap<>();
        for (String stage : List.of("MEMORY", "INTENT", "DECISION", "ACTION", "ANSWER", "OTHER")) {
            grouped.put(stage, new ArrayList<>());
        }

        for (AgentRunTraceEntity trace : traces) {
            String stage = classifyStage(trace.getStep());
            grouped.get(stage).add(toWorkflowStep(trace, stage));
        }

        List<AgentWorkflowStage> stages = new ArrayList<>();
        for (Map.Entry<String, List<AgentWorkflowStep>> entry : grouped.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                stages.add(toWorkflowStage(entry.getKey(), entry.getValue()));
            }
        }
        return stages;
    }

    public List<AgentDecision> buildDecisions(List<AgentRunTraceEntity> traces) {
        List<AgentDecision> decisions = new ArrayList<>();
        int decisionOrder = 1;
        for (AgentRunTraceEntity trace : traces) {
            if (isDecisionStep(trace.getStep())) {
                decisions.add(toDecision(decisionOrder++, trace));
            }
        }
        return decisions;
    }

    public List<AgentAction> buildActions(List<AgentRunTraceEntity> traces) {
        List<AgentAction> actions = new ArrayList<>();
        int actionOrder = 1;
        for (AgentRunTraceEntity trace : traces) {
            if (STEP_CALL_SKILL.equals(trace.getStep())) {
                actions.add(toAction(actionOrder++, trace));
            }
        }
        return actions;
    }

    public AgentReflection buildReflection(List<AgentWorkflowStage> stages,
                                           List<AgentDecision> decisions,
                                           List<AgentAction> actions,
                                           String runStatus) {
        AgentReflection reflection = new AgentReflection();
        reflection.setWhatWentWell(buildWhatWentWell(stages, decisions, actions));
        reflection.setWhatNeedsAttention(buildWhatNeedsAttention(stages, decisions, actions, runStatus));
        reflection.setSuggestedNextSteps(buildSuggestedNextSteps(decisions, actions, runStatus));
        return reflection;
    }

    private AgentWorkflowStep toWorkflowStep(AgentRunTraceEntity trace, String stage) {
        AgentWorkflowStep step = new AgentWorkflowStep();
        step.setStepOrder(trace.getStepOrder());
        step.setStep(trace.getStep());
        step.setStage(stage);
        step.setStatus(trace.getStatus());
        step.setDetail(trace.getDetail());
        step.setData(parseData(trace.getDataJson()));
        step.setDurationMs(trace.getDurationMs());
        step.setTraceTimestamp(trace.getTraceTimestamp());
        step.setCreatedAt(trace.getCreatedAt());
        return step;
    }

    private AgentWorkflowStage toWorkflowStage(String stage, List<AgentWorkflowStep> steps) {
        AgentWorkflowStage response = new AgentWorkflowStage();
        response.setStage(stage);
        response.setStatus(stageStatus(steps));
        response.setSummary(stageSummary(stage));
        response.setStartedAt(firstTime(steps));
        response.setFinishedAt(lastTime(steps));
        response.setDurationMs(totalDuration(steps));
        response.setSteps(steps);
        return response;
    }

    private AgentDecision toDecision(int decisionOrder, AgentRunTraceEntity trace) {
        Map<String, Object> data = parseDataMap(trace.getDataJson());
        AgentDecision decision = new AgentDecision();
        decision.setDecisionOrder(decisionOrder);
        decision.setTraceStepOrder(trace.getStepOrder());
        decision.setType(firstNonBlank(data, "decisionType", decisionType(trace.getStep())));
        decision.setSource(firstNonBlank(data, "decisionSource", decisionSource(trace.getStep(), trace.getDetail())));
        decision.setStatus(trace.getStatus());
        decision.setSummary(trace.getDetail());
        decision.setIntent(stringValue(data.get("intent")));
        decision.setNeedSkill(booleanValue(data.get("needSkill")));
        decision.setSkillName(firstNonBlank(data, "selectedSkill", stringValue(data.get("skillName"))));
        decision.setParams(firstPresent(data, "resolvedParams", "params", "knownParams"));
        decision.setMissingParams(data.get("missingParams"));
        decision.setReason(stringValue(data.get("reason")));
        decision.setPendingStore(stringValue(data.get("pendingStore")));
        decision.setRawData(data);
        decision.setDurationMs(trace.getDurationMs());
        decision.setTraceTimestamp(trace.getTraceTimestamp());
        decision.setCreatedAt(trace.getCreatedAt());
        return decision;
    }

    private AgentAction toAction(int actionOrder, AgentRunTraceEntity trace) {
        Map<String, Object> data = parseDataMap(trace.getDataJson());
        AgentObservation observation = new AgentObservation();
        observation.setType(firstNonBlank(data, "observationType", "SKILL_RESULT"));
        observation.setData(firstPresent(data, "observation", "result"));
        observation.setErrorMessage(blankToNull(stringValue(data.get("errorMessage"))));

        AgentAction action = new AgentAction();
        action.setActionOrder(actionOrder);
        action.setTraceStepOrder(trace.getStepOrder());
        action.setType(firstNonBlank(data, "actionType", ACTION_TYPE_SKILL_CALL));
        action.setName(firstNonBlank(data, "selectedSkill", stringValue(data.get("skillName"))));
        action.setStatus(trace.getStatus());
        action.setInput(firstPresent(data, "resolvedParams", "params"));
        action.setObservation(observation);
        action.setDurationMs(trace.getDurationMs());
        action.setTraceTimestamp(trace.getTraceTimestamp());
        action.setCreatedAt(trace.getCreatedAt());
        return action;
    }

    private String classifyStage(String step) {
        if ("LOAD_MEMORY".equals(step) || "SAVE_MEMORY".equals(step)) {
            return "MEMORY";
        }
        if (STEP_INTENT_DETECTION.equals(step)) {
            return "INTENT";
        }
        if (STEP_SELECT_SKILL.equals(step) || STEP_PARAM_RESOLUTION.equals(step)
                || STEP_LLM_SKILL_DECISION.equals(step)) {
            return "DECISION";
        }
        if (STEP_CALL_SKILL.equals(step)) {
            return "ACTION";
        }
        if ("GENERATE_ANSWER".equals(step)) {
            return "ANSWER";
        }
        return "OTHER";
    }

    private boolean isDecisionStep(String step) {
        return STEP_INTENT_DETECTION.equals(step)
                || STEP_LLM_SKILL_DECISION.equals(step)
                || STEP_SELECT_SKILL.equals(step)
                || STEP_PARAM_RESOLUTION.equals(step);
    }

    private String stageStatus(List<AgentWorkflowStep> steps) {
        for (AgentWorkflowStep step : steps) {
            if (!STATUS_SUCCESS.equals(step.getStatus())) {
                return STATUS_FAILED;
            }
        }
        return STATUS_SUCCESS;
    }

    private String stageSummary(String stage) {
        return switch (stage) {
            case "MEMORY" -> "加载或保存短期记忆和长期记忆";
            case "INTENT" -> "识别用户意图和潜在 Skill 调用";
            case "DECISION" -> "选择 Skill 并解析必要参数";
            case "ACTION" -> "执行 Skill 或平台动作";
            case "ANSWER" -> "生成面向用户的最终回答";
            default -> "记录其他 AgentRuntime trace 步骤";
        };
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

    private LocalDateTime firstTime(List<AgentWorkflowStep> steps) {
        return steps.stream()
                .map(this::stepTime)
                .filter(time -> time != null)
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime lastTime(List<AgentWorkflowStep> steps) {
        LocalDateTime last = null;
        for (AgentWorkflowStep step : steps) {
            LocalDateTime time = stepTime(step);
            if (time != null) {
                last = time;
            }
        }
        return last;
    }

    private LocalDateTime stepTime(AgentWorkflowStep step) {
        return step.getTraceTimestamp() == null ? step.getCreatedAt() : step.getTraceTimestamp();
    }

    private Long totalDuration(List<AgentWorkflowStep> steps) {
        long total = 0L;
        boolean hasDuration = false;
        for (AgentWorkflowStep step : steps) {
            if (step.getDurationMs() != null) {
                total += step.getDurationMs();
                hasDuration = true;
            }
        }
        return hasDuration ? total : null;
    }

    private List<String> buildWhatWentWell(List<AgentWorkflowStage> stages,
                                           List<AgentDecision> decisions,
                                           List<AgentAction> actions) {
        List<String> items = new ArrayList<>();
        for (AgentWorkflowStage stage : safeStages(stages)) {
            if (STATUS_SUCCESS.equals(stage.getStatus())) {
                items.add(stage.getStage() + " 阶段执行成功");
            }
        }
        if (!safeDecisions(decisions).isEmpty()) {
            items.add("保留了 " + safeDecisions(decisions).size() + " 个决策事件，便于解释 Agent 为什么这样做");
        }
        for (AgentAction action : safeActions(actions)) {
            if (STATUS_SUCCESS.equals(action.getStatus())) {
                items.add(action.getName() + " Skill 调用成功，并记录了输入和观察结果");
            }
        }
        if (items.isEmpty()) {
            items.add("本次运行没有可确认的成功阶段，需要查看原始 trace");
        }
        return items;
    }

    private List<String> buildWhatNeedsAttention(List<AgentWorkflowStage> stages,
                                                 List<AgentDecision> decisions,
                                                 List<AgentAction> actions,
                                                 String runStatus) {
        List<String> items = new ArrayList<>();
        if (!STATUS_SUCCESS.equals(runStatus)) {
            items.add("Agent Run 整体状态不是 SUCCESS：" + runStatus);
        }
        for (AgentWorkflowStage stage : safeStages(stages)) {
            if (!STATUS_SUCCESS.equals(stage.getStatus())) {
                items.add(stage.getStage() + " 阶段存在失败或跳过步骤");
            }
        }
        for (AgentDecision decision : safeDecisions(decisions)) {
            if (!STATUS_SUCCESS.equals(decision.getStatus())) {
                items.add("决策事件未成功：" + nullToEmpty(decision.getSummary()));
            }
            if (decision.getMissingParams() != null) {
                items.add("存在缺失参数或追问：" + decision.getMissingParams());
            }
        }
        for (AgentAction action : safeActions(actions)) {
            if (!STATUS_SUCCESS.equals(action.getStatus())) {
                String errorMessage = action.getObservation() == null ? null : action.getObservation().getErrorMessage();
                items.add(action.getName() + " Skill 调用失败：" + nullToEmpty(errorMessage));
            }
        }
        if (items.isEmpty()) {
            items.add("本次没有发现失败步骤或明显风险");
        }
        return items;
    }

    private List<String> buildSuggestedNextSteps(List<AgentDecision> decisions,
                                                 List<AgentAction> actions,
                                                 String runStatus) {
        List<String> items = new ArrayList<>();
        if (!STATUS_SUCCESS.equals(runStatus)) {
            items.add("优先查看 Run Detail 和 Workflow View，定位失败阶段");
        }
        if (hasMissingParams(decisions)) {
            items.add("如果存在缺参追问，下一轮应按参数名补充缺失值");
        }
        if (hasFailedAction(actions)) {
            items.add("如果 Skill 调用失败，优先查看 Action / Observation View 中的 input、observation 和 errorMessage");
        }
        if (!safeActions(actions).isEmpty()) {
            items.add("可以继续观察后续 Run 的 Skill 调用耗时和失败率");
        }
        if (items.isEmpty()) {
            items.add("可以继续使用 Explain / Workflow / Decisions / Actions 视图复盘类似请求");
        }
        return items;
    }

    private boolean hasMissingParams(List<AgentDecision> decisions) {
        for (AgentDecision decision : safeDecisions(decisions)) {
            if (decision.getMissingParams() != null) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFailedAction(List<AgentAction> actions) {
        for (AgentAction action : safeActions(actions)) {
            if (!STATUS_SUCCESS.equals(action.getStatus())) {
                return true;
            }
        }
        return false;
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

    private List<AgentWorkflowStage> safeStages(List<AgentWorkflowStage> stages) {
        return stages == null ? List.of() : stages;
    }

    private List<AgentDecision> safeDecisions(List<AgentDecision> decisions) {
        return decisions == null ? List.of() : decisions;
    }

    private List<AgentAction> safeActions(List<AgentAction> actions) {
        return actions == null ? List.of() : actions;
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String firstNonBlank(Map<String, Object> data, String key, String fallback) {
        Object value = data.get(key);
        if (value == null || value.toString().isBlank()) {
            return fallback;
        }
        return value.toString();
    }

    private Object firstPresent(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
