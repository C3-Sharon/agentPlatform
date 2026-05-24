package com.sharon.agentplatform.mcp.tool;

import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolMetadata;
import com.sharon.agentplatform.mcp.core.McpToolRequest;
import com.sharon.agentplatform.mcp.core.McpToolResponse;
import com.sharon.agentplatform.mcp.core.McpToolResult;
import com.sharon.agentplatform.mcp.filesystem.FileSystemMcpClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FileSystemSearchMcpTool implements McpTool {

    private final FileSystemMcpClient fileSystemMcpClient;

    public FileSystemSearchMcpTool(FileSystemMcpClient fileSystemMcpClient) {
        this.fileSystemMcpClient = fileSystemMcpClient;
    }

    @Override
    public McpToolMetadata metadata() {
        McpToolMetadata metadata = new McpToolMetadata();
        metadata.setName("filesystem.search");
        metadata.setDisplayName("\u6587\u4ef6\u7cfb\u7edf\u641c\u7d22");
        metadata.setDescription("\u5728\u672c\u5730\u5de5\u4f5c\u533a\u4e2d\u641c\u7d22\u6587\u4ef6\u6216\u6587\u672c\u5185\u5bb9\uff0c\u7528\u4e8e\u6f14\u793a MCP \u98ce\u683c\u5de5\u5177\u8c03\u7528\u3002");
        metadata.setVersion("1.0.0");
        metadata.setInputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                        "keyword", Map.of(
                                "type", "string",
                                "description", "\u641c\u7d22\u5173\u952e\u8bcd"
                        )
                ),
                "required", List.of("keyword")
        ));
        return metadata;
    }

    @Override
    public McpToolResponse call(McpToolRequest request) {
        Map<String, Object> params = request == null ? null : request.getParams();
        Object keywordValue = params == null ? null : params.get("keyword");
        String keyword = keywordValue == null ? null : keywordValue.toString();
        if (keyword == null || keyword.isBlank()) {
            return McpToolResponse.fail("keyword is required");
        }

        McpToolResult result = fileSystemMcpClient.callTool("searchFiles", Map.of("keyword", keyword));
        if (!result.isSuccess()) {
            return McpToolResponse.fail(result.getErrorMessage());
        }

        return McpToolResponse.success(result.getContent());
    }
}
