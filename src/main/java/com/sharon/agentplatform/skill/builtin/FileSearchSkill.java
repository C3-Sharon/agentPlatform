package com.sharon.agentplatform.skill.builtin;

import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolRegistry;
import com.sharon.agentplatform.mcp.core.McpToolRequest;
import com.sharon.agentplatform.mcp.core.McpToolResponse;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillContext;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FileSearchSkill implements Skill {

    private final McpToolRegistry mcpToolRegistry;

    public FileSearchSkill(McpToolRegistry mcpToolRegistry) {
        this.mcpToolRegistry = mcpToolRegistry;
    }

    @Override
    public SkillMetadata metadata() {
        return new SkillMetadata(
                "file_search",
                "文件搜索",
                "通过 MCP 文件系统工具在 workspace 目录中搜索文件名",
                "1.1.0",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "keyword", Map.of(
                                        "type", "string",
                                        "description", "文件名关键字"
                                )
                        ),
                        "required", List.of("keyword")
                ),
                List.of("mcp:file-system")
        );
    }

    @Override
    public SkillResult execute(SkillContext context) {
        String keyword = context.getStringParam("keyword");

        if (keyword == null || keyword.isBlank()) {
            return SkillResult.fail("Missing required parameter: keyword");
        }

        McpTool tool = mcpToolRegistry.get("filesystem.search")
                .orElseThrow(() -> new IllegalStateException("MCP tool not found: filesystem.search"));
        McpToolRequest request = new McpToolRequest();
        request.setParams(Map.of("keyword", keyword));
        McpToolResponse mcpResult = tool.call(request);

        if (!mcpResult.isSuccess()) {
            return SkillResult.fail(mcpResult.getErrorMessage());
        }

        return SkillResult.success(Map.of(
                "mcpTool", "filesystem.search",
                "keyword", keyword,
                "result", mcpResult.getResult()
        ));
    }
}
