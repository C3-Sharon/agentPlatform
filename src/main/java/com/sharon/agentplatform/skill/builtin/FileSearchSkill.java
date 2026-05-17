package com.sharon.agentplatform.skill.builtin;

import com.sharon.agentplatform.mcp.core.McpClient;
import com.sharon.agentplatform.mcp.core.McpToolResult;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillContext;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FileSearchSkill implements Skill {

    private final McpClient mcpClient;

    public FileSearchSkill(McpClient mcpClient) {
        this.mcpClient = mcpClient;
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

        McpToolResult mcpResult = mcpClient.callTool(
                "searchFiles",
                Map.of("keyword", keyword)
        );

        if (!mcpResult.isSuccess()) {
            return SkillResult.fail(mcpResult.getErrorMessage());
        }

        return SkillResult.success(Map.of(
                "mcpTool", "searchFiles",
                "keyword", keyword,
                "result", mcpResult.getContent()
        ));
    }
}