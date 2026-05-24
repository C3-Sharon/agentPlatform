package com.sharon.agentplatform.mcp.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolMetadata;
import com.sharon.agentplatform.mcp.core.McpToolRegistry;
import com.sharon.agentplatform.mcp.core.McpToolRequest;
import com.sharon.agentplatform.mcp.core.McpToolResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mcp/tools")
public class McpToolController {

    private final McpToolRegistry mcpToolRegistry;

    public McpToolController(McpToolRegistry mcpToolRegistry) {
        this.mcpToolRegistry = mcpToolRegistry;
    }

    @GetMapping
    public ApiResponse<List<McpToolMetadata>> listTools() {
        return ApiResponse.success(mcpToolRegistry.list());
    }

    @PostMapping("/{toolName}/call")
    public ApiResponse<McpToolResponse> callTool(@PathVariable String toolName,
                                                 @RequestBody McpToolRequest request) {
        McpTool tool = mcpToolRegistry.get(toolName)
                .orElseThrow(() -> new BusinessException("MCP tool not found: " + toolName));
        return ApiResponse.success(tool.call(request));
    }
}
