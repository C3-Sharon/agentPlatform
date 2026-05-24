package com.sharon.agentplatform.resume.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.resume.dto.ResumeAnalysisTaskStatusResponse;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeAsyncResponse;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeRequest;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeResponse;
import com.sharon.agentplatform.resume.service.ResumeOptimizeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume")
public class ResumeOptimizeController {

    private final ResumeOptimizeService resumeOptimizeService;

    public ResumeOptimizeController(ResumeOptimizeService resumeOptimizeService) {
        this.resumeOptimizeService = resumeOptimizeService;
    }

    @PostMapping("/optimize")
    public ApiResponse<ResumeOptimizeResponse> optimize(@RequestBody ResumeOptimizeRequest request) {
        return ApiResponse.success(resumeOptimizeService.optimize(request));
    }

    @PostMapping("/optimize/async")
    public ApiResponse<ResumeOptimizeAsyncResponse> optimizeAsync(@RequestBody ResumeOptimizeRequest request) {
        return ApiResponse.success(resumeOptimizeService.submitAsync(request));
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<ResumeAnalysisTaskStatusResponse> getTaskStatus(@PathVariable Long taskId) {
        return ApiResponse.success(resumeOptimizeService.getTaskStatus(taskId));
    }
}
