package com.sharon.agentplatform.skill.core;

public class SkillResult {

    private boolean success;
    private Object result;
    private String errorMessage;

    private SkillResult(boolean success, Object result, String errorMessage) {
        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public static SkillResult success(Object result) {
        return new SkillResult(true, result, null);
    }

    public static SkillResult fail(String errorMessage) {
        return new SkillResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}