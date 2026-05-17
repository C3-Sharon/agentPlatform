package com.sharon.agentplatform.skill.builtin;

import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillContext;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
public class FileSearchSkill implements Skill {

    private static final Path ROOT_DIR = Path.of("workspace").toAbsolutePath().normalize();

    @Override
    public SkillMetadata metadata() {
        return new SkillMetadata(
                "file_search",
                "文件搜索",
                "在指定目录中搜索文件名。当前版本限制在 workspace 目录下。",
                "1.0.0",
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
                List.of()
        );
    }

    @Override
    public SkillResult execute(SkillContext context) {
        String keyword = context.getStringParam("keyword");

        if (keyword == null || keyword.isBlank()) {
            return SkillResult.fail("Missing required parameter: keyword");
        }

        try {
            ensureWorkspaceExists();

            List<String> matchedFiles = Files.walk(ROOT_DIR)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains(keyword))
                    .map(path -> ROOT_DIR.relativize(path).toString())
                    .toList();

            return SkillResult.success(Map.of(
                    "rootDir", ROOT_DIR.toString(),
                    "keyword", keyword,
                    "files", matchedFiles
            ));
        } catch (IOException e) {
            return SkillResult.fail("File search failed: " + e.getMessage());
        }
    }

    private void ensureWorkspaceExists() throws IOException {
        if (!Files.exists(ROOT_DIR)) {
            Files.createDirectories(ROOT_DIR);
        }
    }
}