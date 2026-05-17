package com.sharon.agentplatform.agent.core;

public enum AgentStep {

    RECEIVE_MESSAGE,
    LOAD_MEMORY,
    SAVE_MEMORY,

    INTENT_DETECTION,
    LLM_SKILL_DECISION,
    SELECT_SKILL,
    CALL_SKILL,

    GENERATE_ANSWER,
    FINISH,
    ERROR
}