package com.sharon.agentplatform.resume.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeRequest;
import com.sharon.agentplatform.resume.dto.ResumeOptimizeResponse;
import com.sharon.agentplatform.resume.service.ResumeOptimizeService;
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
}
