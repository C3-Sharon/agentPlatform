package com.sharon.agentplatform.mcp.rpc.controller;

import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcRequest;
import com.sharon.agentplatform.mcp.rpc.dto.McpJsonRpcResponse;
import com.sharon.agentplatform.mcp.rpc.service.McpJsonRpcService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mcp/rpc")
public class McpJsonRpcController {

    private final McpJsonRpcService mcpJsonRpcService;

    public McpJsonRpcController(McpJsonRpcService mcpJsonRpcService) {
        this.mcpJsonRpcService = mcpJsonRpcService;
    }

    @PostMapping
    public McpJsonRpcResponse handle(@RequestBody McpJsonRpcRequest request) {
        return mcpJsonRpcService.handle(request);
    }
}
