package com.sharon.agentplatform.system.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.system.dto.SystemHealthResponse;
import com.sharon.agentplatform.system.service.SystemHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemHealthController {

    private final SystemHealthService systemHealthService;

    public SystemHealthController(SystemHealthService systemHealthService) {
        this.systemHealthService = systemHealthService;
    }

    @GetMapping("/health-check")
    public ApiResponse<SystemHealthResponse> healthCheck() {
        return ApiResponse.success(systemHealthService.check());
    }
}
