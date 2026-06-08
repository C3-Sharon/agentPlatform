package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.history.entity.AgentRunEntity;
import com.sharon.agentplatform.agent.history.entity.AgentRunTraceEntity;
import com.sharon.agentplatform.agent.history.repository.AgentRunRepository;
import com.sharon.agentplatform.agent.history.repository.AgentRunTraceRepository;
import com.sharon.agentplatform.agent.workflow.model.AgentAction;
import com.sharon.agentplatform.agent.workflow.model.AgentDecision;
import com.sharon.agentplatform.agent.workflow.model.AgentReflection;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowRun;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowStage;
import com.sharon.agentplatform.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentWorkflowModelService {

    private final AgentRunRepository agentRunRepository;
    private final AgentRunTraceRepository agentRunTraceRepository;
    private final AgentWorkflowTraceMapper agentWorkflowTraceMapper;

    public AgentWorkflowModelService(AgentRunRepository agentRunRepository,
                                     AgentRunTraceRepository agentRunTraceRepository,
                                     AgentWorkflowTraceMapper agentWorkflowTraceMapper) {
        this.agentRunRepository = agentRunRepository;
        this.agentRunTraceRepository = agentRunTraceRepository;
        this.agentWorkflowTraceMapper = agentWorkflowTraceMapper;
    }

    public AgentWorkflowRun getWorkflowRun(String runId) {
        AgentRunEntity run = agentRunRepository.findByRunId(runId)
                .orElseThrow(() -> new BusinessException("Agent run not found: " + runId));
        List<AgentRunTraceEntity> traces = agentRunTraceRepository.findByRunIdOrderByStepOrderAsc(runId);

        List<AgentWorkflowStage> stages = agentWorkflowTraceMapper.buildStages(traces);
        List<AgentDecision> decisions = agentWorkflowTraceMapper.buildDecisions(traces);
        List<AgentAction> actions = agentWorkflowTraceMapper.buildActions(traces);
        AgentReflection reflection = agentWorkflowTraceMapper.buildReflection(stages, decisions, actions, run.getStatus());

        AgentWorkflowRun workflowRun = new AgentWorkflowRun();
        workflowRun.setRunId(run.getRunId());
        workflowRun.setConversationId(run.getConversationId());
        workflowRun.setModelId(run.getModelId());
        workflowRun.setUserMessage(run.getUserMessage());
        workflowRun.setAnswer(run.getAnswer());
        workflowRun.setStatus(run.getStatus());
        workflowRun.setErrorMessage(run.getErrorMessage());
        workflowRun.setDurationMs(run.getDurationMs());
        workflowRun.setCreatedAt(run.getCreatedAt());
        workflowRun.setStages(stages);
        workflowRun.setDecisions(decisions);
        workflowRun.setActions(actions);
        workflowRun.setReflection(reflection);
        return workflowRun;
    }
}
