package com.sharon.agentplatform.agent.history.controller;

import com.sharon.agentplatform.agent.history.dto.AgentRunDetailResponse;
import com.sharon.agentplatform.agent.history.dto.AgentRunSummaryResponse;
import com.sharon.agentplatform.agent.history.service.AgentRunHistoryService;
import com.sharon.agentplatform.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agent/runs")
public class AgentRunHistoryController {

    private final AgentRunHistoryService agentRunHistoryService;

    public AgentRunHistoryController(AgentRunHistoryService agentRunHistoryService) {
        this.agentRunHistoryService = agentRunHistoryService;
    }

    @GetMapping
    public ApiResponse<List<AgentRunSummaryResponse>> listRecentRuns() {
        return ApiResponse.success(agentRunHistoryService.listRecentRuns());
    }

    @GetMapping("/{runId}")
    public ApiResponse<AgentRunDetailResponse> getRunDetail(@PathVariable String runId) {
        return ApiResponse.success(agentRunHistoryService.getRunDetail(runId));
    }
}
