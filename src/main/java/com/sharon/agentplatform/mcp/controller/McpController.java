package com.sharon.agentplatform.mcp.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.mcp.core.McpClient;
import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolResult;
import com.sharon.agentplatform.mcp.dto.McpToolCallRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private final McpClient mcpClient;

    public McpController(McpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    @GetMapping("/tools")
    public ApiResponse<List<McpTool>> listTools() {
        return ApiResponse.success(mcpClient.listTools());
    }

    @PostMapping("/call")
    public ApiResponse<McpToolResult> callTool(
            @RequestBody McpToolCallRequest request
    ) {
        if (request == null || request.getToolName() == null || request.getToolName().isBlank()) {
            return ApiResponse.fail("Missing required argument: toolName");
        }

        McpToolResult result = mcpClient.callTool(
                request.getToolName(),
                request.getArguments()
        );

        if (!result.isSuccess()) {
            return ApiResponse.fail(result.getErrorMessage());
        }

        return ApiResponse.success(result);
    }
}
