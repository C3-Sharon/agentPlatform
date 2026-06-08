package com.sharon.agentplatform.agent.workflow.dto;

public class AgentRunDebugSummaryResponse {

    private Boolean memoryLoaded;
    private Integer decisionCount;
    private Integer actionCount;
    private Boolean hasMissingParams;
    private Boolean hasFailedAction;
    private String selectedSkill;
    private Object resolvedParams;
    private Object missingParams;
    private Object actionObservation;

    public Boolean getMemoryLoaded() {
        return memoryLoaded;
    }

    public void setMemoryLoaded(Boolean memoryLoaded) {
        this.memoryLoaded = memoryLoaded;
    }

    public Integer getDecisionCount() {
        return decisionCount;
    }

    public void setDecisionCount(Integer decisionCount) {
        this.decisionCount = decisionCount;
    }

    public Integer getActionCount() {
        return actionCount;
    }

    public void setActionCount(Integer actionCount) {
        this.actionCount = actionCount;
    }

    public Boolean getHasMissingParams() {
        return hasMissingParams;
    }

    public void setHasMissingParams(Boolean hasMissingParams) {
        this.hasMissingParams = hasMissingParams;
    }

    public Boolean getHasFailedAction() {
        return hasFailedAction;
    }

    public void setHasFailedAction(Boolean hasFailedAction) {
        this.hasFailedAction = hasFailedAction;
    }

    public String getSelectedSkill() {
        return selectedSkill;
    }

    public void setSelectedSkill(String selectedSkill) {
        this.selectedSkill = selectedSkill;
    }

    public Object getResolvedParams() {
        return resolvedParams;
    }

    public void setResolvedParams(Object resolvedParams) {
        this.resolvedParams = resolvedParams;
    }

    public Object getMissingParams() {
        return missingParams;
    }

    public void setMissingParams(Object missingParams) {
        this.missingParams = missingParams;
    }

    public Object getActionObservation() {
        return actionObservation;
    }

    public void setActionObservation(Object actionObservation) {
        this.actionObservation = actionObservation;
    }
}
