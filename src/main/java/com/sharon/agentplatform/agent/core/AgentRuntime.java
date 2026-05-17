package com.sharon.agentplatform.agent.core;

import com.sharon.agentplatform.agent.service.LlmSkillDecisionService;
import com.sharon.agentplatform.agent.dto.ChatRequest;
import com.sharon.agentplatform.agent.dto.ChatResponse;
import com.sharon.agentplatform.common.exception.ModelCallException;
import com.sharon.agentplatform.memory.core.ChatMessage;
import com.sharon.agentplatform.memory.core.LongTermMemory;
import com.sharon.agentplatform.memory.service.MemoryService;
import com.sharon.agentplatform.skill.core.SkillContext;
import com.sharon.agentplatform.skill.core.SkillRegistry;
import com.sharon.agentplatform.skill.core.SkillResult;
import org.springframework.stereotype.Component;
import com.sharon.agentplatform.model.service.ModelService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AgentRuntime {

    private static final String MOCK_MODEL = "mock-model";

    private final SkillRegistry skillRegistry;
    private final MemoryService memoryService;
    private final ModelService modelService;
    private final LlmSkillDecisionService llmSkillDecisionService;

    public AgentRuntime(
            SkillRegistry skillRegistry,
            MemoryService memoryService,
            ModelService modelService,
            LlmSkillDecisionService llmSkillDecisionService
    ) {
        this.skillRegistry = skillRegistry;
        this.memoryService = memoryService;
        this.modelService = modelService;
        this.llmSkillDecisionService = llmSkillDecisionService;
    }

    public ChatResponse run(ChatRequest request) {
        List<AgentTrace> trace = new ArrayList<>();
        List<String> usedSkills = new ArrayList<>();

        String conversationId = normalizeConversationId(request.getConversationId());
        String modelId = normalizeModelId(request.getModelId());
        String message = request.getMessage();

        trace.add(AgentTrace.success(
                AgentStep.RECEIVE_MESSAGE,
                "收到用户输入",
                Map.of(
                        "conversationId", conversationId,
                        "message", message == null ? "" : message,
                        "modelId", modelId
                )
        ));

        if (message == null || message.isBlank()) {
            trace.add(AgentTrace.failed(
                    AgentStep.ERROR,
                    "用户输入为空"
            ));

            return new ChatResponse(
                    conversationId,
                    "请输入有效的问题。",
                    modelId,
                    usedSkills,
                    trace
            );
        }

        try {
            List<ChatMessage> shortTermMessages = memoryService.getShortTermMessages(conversationId);
            List<LongTermMemory> longTermMemories = memoryService.getLongTermMemories(conversationId);

            trace.add(AgentTrace.success(
                    AgentStep.LOAD_MEMORY,
                    "读取会话记忆",
                    Map.of(
                            "shortTermCount", shortTermMessages.size(),
                            "longTermCount", longTermMemories.size()
                    )
            ));

            memoryService.addUserMessage(conversationId, message);

            String answer;

            if (isAskNameIntent(message, longTermMemories, shortTermMessages)) {
                trace.add(AgentTrace.success(
                        AgentStep.INTENT_DETECTION,
                        "识别为询问用户姓名",
                        Map.of("intent", "ask_user_name")
                ));

                answer = answerUserName(longTermMemories, shortTermMessages);

                trace.add(AgentTrace.success(
                        AgentStep.GENERATE_ANSWER,
                        "根据记忆生成用户姓名回答"
                ));
            } else {
                try {
                    long decisionStart = System.currentTimeMillis();

                    SkillDecision decision = llmSkillDecisionService.decide(modelId, message);

                    long decisionDurationMs = System.currentTimeMillis() - decisionStart;

                    trace.add(AgentTrace.withDuration(
                            AgentStep.LLM_SKILL_DECISION,
                            AgentTraceStatus.SUCCESS,
                            "LLM 完成 Skill 调用决策",
                            Map.of(
                                    "needSkill", decision.isNeedSkill(),
                                    "skillName", decision.getSkillName() == null ? "" : decision.getSkillName(),
                                    "params", decision.getParams(),
                                    "reason", decision.getReason() == null ? "" : decision.getReason()
                            ),
                            decisionDurationMs
                    ));

                    if (decision.isNeedSkill()) {
                        answer = handleSkillDecision(
                                decision,
                                message,
                                modelId,
                                shortTermMessages,
                                longTermMemories,
                                trace,
                                usedSkills
                        );
                    } else {
                        answer = handleByRuleFallback(
                                message,
                                modelId,
                                shortTermMessages,
                                longTermMemories,
                                trace,
                                usedSkills
                        );
                    }
                } catch (Exception decisionException) {
                    trace.add(AgentTrace.failed(
                            AgentStep.LLM_SKILL_DECISION,
                            "LLM Skill 决策失败，使用规则兜底",
                            Map.of("errorMessage", decisionException.getMessage() == null ? "" : decisionException.getMessage())
                    ));

                    answer = handleByRuleFallback(
                            message,
                            modelId,
                            shortTermMessages,
                            longTermMemories,
                            trace,
                            usedSkills
                    );
                }
            }

            memoryService.addAssistantMessage(conversationId, answer);

            trace.add(AgentTrace.success(
                    AgentStep.SAVE_MEMORY,
                    "保存本轮对话到短期记忆",
                    Map.of(
                            "conversationId", conversationId,
                            "savedMessages", 2
                    )
            ));

            trace.add(AgentTrace.success(
                    AgentStep.FINISH,
                    "Agent 执行完成",
                    Map.of(
                            "usedSkills", usedSkills,
                            "usedModel", modelId
                    )
            ));

            return new ChatResponse(
                    conversationId,
                    answer,
                    modelId,
                    usedSkills,
                    trace
            );

        } catch (Exception e) {
            trace.add(AgentTrace.failed(
                    AgentStep.ERROR,
                    "Agent 执行异常",
                    Map.of("errorMessage", e.getMessage() == null ? "" : e.getMessage())
            ));

            return new ChatResponse(
                    conversationId,
                    "Agent 执行过程中出现错误：" + e.getMessage(),
                    modelId,
                    usedSkills,
                    trace
            );
        }
    }

    private String normalizeConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return "default";
        }
        return conversationId;
    }

    private String normalizeModelId(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return MOCK_MODEL;
        }
        return modelId;
    }

    private boolean isWeatherIntent(String message) {
        return message.contains("天气")
                || message.contains("气温")
                || message.contains("下雨")
                || message.toLowerCase().contains("weather");
    }

    private boolean isCalculatorIntent(String message) {
        return message.contains("计算")
                || message.contains("算一下")
                || message.matches(".*\\d+\\s*[+\\-*/]\\s*\\d+.*");
    }

    private boolean isFileSearchIntent(String message) {
        return message.contains("文件")
                || message.contains("README")
                || message.contains("搜索")
                || message.toLowerCase().contains("file");
    }

    private boolean isAskNameIntent(
            String message,
            List<LongTermMemory> longTermMemories,
            List<ChatMessage> shortTermMessages
    ) {
        return message.contains("我叫什么")
                || message.contains("我的名字")
                || message.toLowerCase().contains("my name");
    }

    private String answerUserName(
            List<LongTermMemory> longTermMemories,
            List<ChatMessage> shortTermMessages
    ) {
        for (LongTermMemory memory : longTermMemories) {
            if ("user_name".equalsIgnoreCase(memory.getKey())
                    || "name".equalsIgnoreCase(memory.getKey())) {
                return "根据长期记忆，你的名字是：" + memory.getValue();
            }
        }

        for (int i = shortTermMessages.size() - 1; i >= 0; i--) {
            ChatMessage chatMessage = shortTermMessages.get(i);
            String content = chatMessage.getContent();

            if (content != null && content.contains("我叫")) {
                String name = content.substring(content.indexOf("我叫") + 2).trim();

                if (!name.isBlank()) {
                    return "根据本轮会话记忆，你刚才说你叫：" + name;
                }
            }
        }

        return "我暂时还没有记住你的名字。你可以告诉我，例如：我叫 Sharon。";
    }

    private String handleWeather(
            String message,
            String modelId,
            List<ChatMessage> shortTermMessages,
            List<LongTermMemory> longTermMemories,
            List<AgentTrace> trace,
            List<String> usedSkills
    ) {
        String city = extractCity(message);
        String skillName = "weather";
        Map<String, Object> params = Map.of("city", city);

        trace.add(AgentTrace.success(
                AgentStep.SELECT_SKILL,
                "选择 weather Skill",
                Map.of(
                        "skillName", skillName,
                        "params", params
                )
        ));

        SkillResult result = callSkillWithTrace(skillName, params, trace);
        usedSkills.add(skillName);

        return summarizeSkillResultWithModel(
                modelId,
                message,
                skillName,
                params,
                result,
                shortTermMessages,
                longTermMemories,
                trace
        );
    }

    private String handleCalculator(
            String message,
            String modelId,
            List<ChatMessage> shortTermMessages,
            List<LongTermMemory> longTermMemories,
            List<AgentTrace> trace,
            List<String> usedSkills
    ) {
        String expression = extractExpression(message);
        String skillName = "calculator";
        Map<String, Object> params = Map.of("expression", expression);

        trace.add(AgentTrace.success(
                AgentStep.SELECT_SKILL,
                "选择 calculator Skill",
                Map.of(
                        "skillName", skillName,
                        "params", params
                )
        ));

        SkillResult result = callSkillWithTrace(skillName, params, trace);
        usedSkills.add(skillName);

        return summarizeSkillResultWithModel(
                modelId,
                message,
                skillName,
                params,
                result,
                shortTermMessages,
                longTermMemories,
                trace
        );
    }

    private String handleFileSearch(
            String message,
            String modelId,
            List<ChatMessage> shortTermMessages,
            List<LongTermMemory> longTermMemories,
            List<AgentTrace> trace,
            List<String> usedSkills
    ) {
        String keyword = extractFileKeyword(message);
        String skillName = "file_search";
        Map<String, Object> params = Map.of("keyword", keyword);

        trace.add(AgentTrace.success(
                AgentStep.SELECT_SKILL,
                "选择 file_search Skill",
                Map.of(
                        "skillName", skillName,
                        "params", params
                )
        ));

        SkillResult result = callSkillWithTrace(skillName, params, trace);
        usedSkills.add(skillName);

        return summarizeSkillResultWithModel(
                modelId,
                message,
                skillName,
                params,
                result,
                shortTermMessages,
                longTermMemories,
                trace
        );
    }

    private SkillResult callSkillWithTrace(
            String skillName,
            Map<String, Object> params,
            List<AgentTrace> trace
    ) {
        long start = System.currentTimeMillis();

        SkillResult result = skillRegistry.call(
                skillName,
                new SkillContext(params)
        );

        long durationMs = System.currentTimeMillis() - start;

        trace.add(AgentTrace.withDuration(
                AgentStep.CALL_SKILL,
                result.isSuccess() ? AgentTraceStatus.SUCCESS : AgentTraceStatus.FAILED,
                result.isSuccess() ? "调用 " + skillName + " Skill 成功" : "调用 " + skillName + " Skill 失败",
                Map.of(
                        "skillName", skillName,
                        "params", params,
                        "result", result.getResult() == null ? "" : result.getResult(),
                        "errorMessage", result.getErrorMessage() == null ? "" : result.getErrorMessage()
                ),
                durationMs
        ));

        return result;
    }

    private Object safeResult(SkillResult result) {
        if (result == null) {
            return "";
        }
        if (result.getResult() != null) {
            return result.getResult();
        }
        if (result.getErrorMessage() != null) {
            return result.getErrorMessage();
        }
        return "";
    }

    private String extractCity(String message) {
        if (message.contains("北京")) {
            return "北京";
        }
        if (message.contains("上海")) {
            return "上海";
        }
        if (message.contains("广州")) {
            return "广州";
        }
        if (message.toLowerCase().contains("beijing")) {
            return "北京";
        }
        if (message.toLowerCase().contains("shanghai")) {
            return "上海";
        }
        if (message.toLowerCase().contains("guangzhou")) {
            return "广州";
        }
        return "北京";
    }

    private String extractExpression(String message) {
        String cleaned = message.replace("帮我", "")
                .replace("计算", "")
                .replace("算一下", "")
                .replace("等于多少", "")
                .replace("是多少", "")
                .trim();

        return cleaned;
    }

    private String extractFileKeyword(String message) {
        if (message.contains("README")) {
            return "README";
        }
        if (message.contains("md")) {
            return ".md";
        }
        if (message.contains("txt")) {
            return ".txt";
        }
        if (message.toLowerCase().contains("readme")) {
            return "README";
        }
        return "README";
    }
    private String buildSystemPrompt(
            List<ChatMessage> shortTermMessages,
            List<LongTermMemory> longTermMemories
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
            你是 AI Agent Platform 中的智能助手。
            你的回答要清晰、准确、有帮助。
            如果上下文中包含短期记忆或长期记忆，请合理参考。
            不要编造不存在的记忆。
            如果你不确定，请明确说明不确定。
            
            """);

        prompt.append("【长期记忆】\n");
        if (longTermMemories == null || longTermMemories.isEmpty()) {
            prompt.append("无\n");
        } else {
            for (LongTermMemory memory : longTermMemories) {
                prompt.append("- ")
                        .append(memory.getKey())
                        .append(": ")
                        .append(memory.getValue())
                        .append("\n");
            }
        }

        prompt.append("\n【短期会话记忆】\n");
        if (shortTermMessages == null || shortTermMessages.isEmpty()) {
            prompt.append("无\n");
        } else {
            for (ChatMessage message : shortTermMessages) {
                prompt.append("- ")
                        .append(message.getRole())
                        .append(": ")
                        .append(message.getContent())
                        .append("\n");
            }
        }

        return prompt.toString();
    }
    private String summarizeSkillResultWithModel(
            String modelId,
            String originalUserMessage,
            String skillName,
            Map<String, Object> params,
            SkillResult skillResult,
            List<ChatMessage> shortTermMessages,
            List<LongTermMemory> longTermMemories,
            List<AgentTrace> trace
    ) {
        String systemPrompt = buildSkillSummarySystemPrompt(
                shortTermMessages,
                longTermMemories
        );

        String userPrompt = buildSkillSummaryUserPrompt(
                originalUserMessage,
                skillName,
                params,
                skillResult
        );

        long modelStart = System.currentTimeMillis();

        try {
            String answer = modelService.chatWithContext(modelId,systemPrompt, userPrompt);

            long modelDurationMs = System.currentTimeMillis() - modelStart;

            trace.add(AgentTrace.withDuration(
                    AgentStep.GENERATE_ANSWER,
                    AgentTraceStatus.SUCCESS,
                    "使用 Spring AI 根据 Skill 结果生成最终回答",
                    Map.of(
                            "modelId", modelId,
                            "skillName", skillName,
                            "params", params,
                            "skillSuccess", skillResult.isSuccess(),
                            "shortTermMemoryCount", shortTermMessages.size(),
                            "longTermMemoryCount", longTermMemories.size()
                    ),
                    modelDurationMs
            ));

            return answer;
        } catch (ModelCallException exception) {
            long modelDurationMs = System.currentTimeMillis() - modelStart;

            trace.add(AgentTrace.withDuration(
                    AgentStep.GENERATE_ANSWER,
                    AgentTraceStatus.FAILED,
                    "模型总结 SkillResult 失败",
                    Map.of(
                            "modelId", modelId,
                            "skillName", skillName,
                            "params", params,
                            "skillSuccess", skillResult.isSuccess(),
                            "errorMessage", exception.getMessage() == null ? "" : exception.getMessage()
                    ),
                    modelDurationMs
            ));

            if (skillResult.isSuccess()) {
                return "工具调用成功，原始结果：" + skillResult.getResult();
            }
            return "工具调用失败：" + skillResult.getErrorMessage();
        }
    }
    private String buildSkillSummarySystemPrompt(
            List<ChatMessage> shortTermMessages,
            List<LongTermMemory> longTermMemories
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
            你是 AI Agent Platform 中的智能助手。
            你会收到用户原始问题、工具名称、工具调用参数以及工具执行结果。
            你的任务是根据工具结果，生成自然、清晰、对用户有帮助的最终回答。
            
            要求：
            1. 不要生硬地复述 JSON 或 Map。
            2. 如果工具调用成功，请把结果解释成自然语言。
            3. 如果工具调用失败，请用友好的方式说明失败原因。
            4. 如果上下文中包含记忆信息，可以合理参考。
            5. 不要编造工具结果里没有的信息。
            6. 回答应当简洁，但必要时可以给出建议。
            
            """);

        prompt.append("【长期记忆】\n");
        if (longTermMemories == null || longTermMemories.isEmpty()) {
            prompt.append("无\n");
        } else {
            for (LongTermMemory memory : longTermMemories) {
                prompt.append("- ")
                        .append(memory.getKey())
                        .append(": ")
                        .append(memory.getValue())
                        .append("\n");
            }
        }

        prompt.append("\n【短期会话记忆】\n");
        if (shortTermMessages == null || shortTermMessages.isEmpty()) {
            prompt.append("无\n");
        } else {
            for (ChatMessage message : shortTermMessages) {
                prompt.append("- ")
                        .append(message.getRole())
                        .append(": ")
                        .append(message.getContent())
                        .append("\n");
            }
        }

        return prompt.toString();
    }
    private String buildSkillSummaryUserPrompt(
            String originalUserMessage,
            String skillName,
            Map<String, Object> params,
            SkillResult skillResult
    ) {
        return """
            用户原始问题：
            %s
            
            已调用的工具：
            %s
            
            工具调用参数：
            %s
            
            工具是否成功：
            %s
            
            工具返回结果：
            %s
            
            工具错误信息：
            %s
            
            请根据以上信息，给用户生成最终回答。
            """.formatted(
                originalUserMessage,
                skillName,
                params,
                skillResult.isSuccess(),
                skillResult.getResult() == null ? "无" : skillResult.getResult(),
                skillResult.getErrorMessage() == null ? "无" : skillResult.getErrorMessage()
        );
    }
    private String handleSkillDecision(
            SkillDecision decision,
            String message,
            String modelId,
            List<ChatMessage> shortTermMessages,
            List<LongTermMemory> longTermMemories,
            List<AgentTrace> trace,
            List<String> usedSkills
    ) {
        String skillName = decision.getSkillName();
        Map<String, Object> params = decision.getParams();

        trace.add(AgentTrace.success(
                AgentStep.SELECT_SKILL,
                "根据 LLM 决策选择 Skill",
                Map.of(
                        "skillName", skillName,
                        "params", params,
                        "reason", decision.getReason() == null ? "" : decision.getReason()
                )
        ));

        SkillResult result = callSkillWithTrace(skillName, params, trace);
        usedSkills.add(skillName);

        return summarizeSkillResultWithModel(
                modelId,
                message,
                skillName,
                params,
                result,
                shortTermMessages,
                longTermMemories,
                trace
        );
    }
    private String handleByRuleFallback(
            String message,
            String modelId,
            List<ChatMessage> shortTermMessages,
            List<LongTermMemory> longTermMemories,
            List<AgentTrace> trace,
            List<String> usedSkills
    ) {
        if (isWeatherIntent(message)) {
            trace.add(AgentTrace.success(
                    AgentStep.INTENT_DETECTION,
                    "规则兜底：识别为天气查询意图",
                    Map.of("intent", "weather")
            ));

            return handleWeather(
                    message,
                    modelId,
                    shortTermMessages,
                    longTermMemories,
                    trace,
                    usedSkills
            );
        }

        if (isCalculatorIntent(message)) {
            trace.add(AgentTrace.success(
                    AgentStep.INTENT_DETECTION,
                    "规则兜底：识别为计算意图",
                    Map.of("intent", "calculator")
            ));

            return handleCalculator(
                    message,
                    modelId,
                    shortTermMessages,
                    longTermMemories,
                    trace,
                    usedSkills
            );
        }

        if (isFileSearchIntent(message)) {
            trace.add(AgentTrace.success(
                    AgentStep.INTENT_DETECTION,
                    "规则兜底：识别为文件搜索意图",
                    Map.of("intent", "file_search")
            ));

            return handleFileSearch(
                    message,
                    modelId,
                    shortTermMessages,
                    longTermMemories,
                    trace,
                    usedSkills
            );
        }

        String systemPrompt = buildSystemPrompt(shortTermMessages, longTermMemories);

        long modelStart = System.currentTimeMillis();

        try {
            String answer = modelService.chatWithContext(modelId, systemPrompt, message);

            long modelDurationMs = System.currentTimeMillis() - modelStart;

            trace.add(AgentTrace.withDuration(
                    AgentStep.GENERATE_ANSWER,
                    AgentTraceStatus.SUCCESS,
                    "未调用 Skill，使用 Spring AI 生成普通回答",
                    Map.of(
                            "modelId", modelId,
                            "shortTermMemoryCount", shortTermMessages.size(),
                            "longTermMemoryCount", longTermMemories.size()
                    ),
                    modelDurationMs
            ));

            return answer;
        } catch (ModelCallException exception) {
            long modelDurationMs = System.currentTimeMillis() - modelStart;

            trace.add(AgentTrace.withDuration(
                    AgentStep.GENERATE_ANSWER,
                    AgentTraceStatus.FAILED,
                    "普通问题模型调用失败",
                    Map.of(
                            "modelId", modelId,
                            "shortTermMemoryCount", shortTermMessages.size(),
                            "longTermMemoryCount", longTermMemories.size(),
                            "errorMessage", exception.getMessage() == null ? "" : exception.getMessage()
                    ),
                    modelDurationMs
            ));

            return "模型调用失败，当前无法生成智能回答，请稍后重试。";
        }
    }
}
