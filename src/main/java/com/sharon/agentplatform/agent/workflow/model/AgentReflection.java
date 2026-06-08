package com.sharon.agentplatform.agent.workflow.model;

import java.util.List;

public class AgentReflection {

    private List<String> whatWentWell;
    private List<String> whatNeedsAttention;
    private List<String> suggestedNextSteps;

    public List<String> getWhatWentWell() {
        return whatWentWell;
    }

    public void setWhatWentWell(List<String> whatWentWell) {
        this.whatWentWell = whatWentWell;
    }

    public List<String> getWhatNeedsAttention() {
        return whatNeedsAttention;
    }

    public void setWhatNeedsAttention(List<String> whatNeedsAttention) {
        this.whatNeedsAttention = whatNeedsAttention;
    }

    public List<String> getSuggestedNextSteps() {
        return suggestedNextSteps;
    }

    public void setSuggestedNextSteps(List<String> suggestedNextSteps) {
        this.suggestedNextSteps = suggestedNextSteps;
    }
}
