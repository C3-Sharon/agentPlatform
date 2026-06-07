package com.sharon.agentplatform.mcp.external.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.mcp.external.dto.McpExternalServerCreateRequest;
import com.sharon.agentplatform.mcp.external.dto.McpExternalServerHealthResponse;
import com.sharon.agentplatform.mcp.external.dto.McpExternalServerResponse;
import com.sharon.agentplatform.mcp.external.dto.McpExternalToolCallRequest;
import com.sharon.agentplatform.mcp.external.dto.McpExternalToolCallResponse;
import com.sharon.agentplatform.mcp.external.service.McpExternalServerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mcp/external")
public class McpExternalServerController {

    private final McpExternalServerService mcpExternalServerService;

    public McpExternalServerController(McpExternalServerService mcpExternalServerService) {
        this.mcpExternalServerService = mcpExternalServerService;
    }

    @PostMapping("/servers")
    public ApiResponse<McpExternalServerResponse> registerServer(@RequestBody McpExternalServerCreateRequest request) {
        return ApiResponse.success(mcpExternalServerService.registerServer(request));
    }

    @GetMapping("/servers")
    public ApiResponse<List<McpExternalServerResponse>> listServers() {
        return ApiResponse.success(mcpExternalServerService.listServers());
    }

    @GetMapping("/servers/{serverId}")
    public ApiResponse<McpExternalServerResponse> getServer(@PathVariable String serverId) {
        return ApiResponse.success(mcpExternalServerService.getServer(serverId));
    }

    @PostMapping("/servers/{serverId}/sync-tools")
    public ApiResponse<McpExternalServerResponse> syncTools(@PathVariable String serverId) {
        return ApiResponse.success(mcpExternalServerService.syncTools(serverId));
    }

    @PostMapping("/servers/{serverId}/health-check")
    public ApiResponse<McpExternalServerHealthResponse> healthCheck(@PathVariable String serverId) {
        return ApiResponse.success(mcpExternalServerService.healthCheck(serverId));
    }

    @PostMapping("/tools/{toolId}/call")
    public ApiResponse<McpExternalToolCallResponse> callTool(@PathVariable String toolId,
                                                             @RequestBody McpExternalToolCallRequest request) {
        return ApiResponse.success(mcpExternalServerService.callTool(toolId, request));
    }
}
