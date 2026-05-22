package com.sharon.agentplatform.mcp.filesystem;

import com.sharon.agentplatform.mcp.core.McpClient;
import com.sharon.agentplatform.mcp.core.McpTool;
import com.sharon.agentplatform.mcp.core.McpToolResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
public class FileSystemMcpClient implements McpClient {

    private static final Path ROOT_DIR = Path.of("workspace")
            .toAbsolutePath()
            .normalize();

    private static final long MAX_READ_FILE_SIZE_BYTES = 1024 * 100;

    @Override
    public List<McpTool> listTools() {
        return List.of(
                new McpTool(
                        "listFiles",
                        "列出文件",
                        "列出 workspace 目录下指定路径的文件",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "path", Map.of(
                                                "type", "string",
                                                "description", "相对于 workspace 的目录路径，可为空"
                                        )
                                )
                        )
                ),
                new McpTool(
                        "readFile",
                        "读取文件",
                        "读取 workspace 目录下指定文件的文本内容",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "path", Map.of(
                                                "type", "string",
                                                "description", "相对于 workspace 的文件路径"
                                        )
                                ),
                                "required", List.of("path")
                        )
                ),
                new McpTool(
                        "searchFiles",
                        "搜索文件",
                        "根据文件名关键字搜索 workspace 目录下的文件",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "keyword", Map.of(
                                                "type", "string",
                                                "description", "文件名关键字"
                                        )
                                ),
                                "required", List.of("keyword")
                        )
                )
        );
    }

    @Override
    public McpToolResult callTool(String toolName, Map<String, Object> arguments) {
        try {
            ensureWorkspaceExists();

            if (toolName == null || toolName.isBlank()) {
                return McpToolResult.fail("Missing required argument: toolName");
            }

            return switch (toolName) {
                case "listFiles" -> listFiles(arguments);
                case "readFile" -> readFile(arguments);
                case "searchFiles" -> searchFiles(arguments);
                default -> McpToolResult.fail("Unknown MCP tool: " + toolName);
            };
        } catch (Exception e) {
            return McpToolResult.fail("MCP tool execution failed: " + e.getMessage());
        }
    }

    private McpToolResult listFiles(Map<String, Object> arguments) throws IOException {
        String pathText = getString(arguments, "path");
        Path dir = resolveSafePath(pathText == null ? "" : pathText);

        if (!Files.exists(dir)) {
            return McpToolResult.fail("Directory does not exist: " + pathText);
        }

        if (!Files.isDirectory(dir)) {
            return McpToolResult.fail("Path is not a directory: " + pathText);
        }

        List<Map<String, Object>> files;
        try (var stream = Files.list(dir)) {
            files = stream
                    .map(path -> Map.<String, Object>of(
                            "name", path.getFileName().toString(),
                            "path", ROOT_DIR.relativize(path).toString(),
                            "directory", Files.isDirectory(path)
                    ))
                    .toList();
        }

        return McpToolResult.success(Map.of(
                "rootDir", ROOT_DIR.toString(),
                "path", pathText == null ? "" : pathText,
                "files", files
        ));
    }

    private McpToolResult readFile(Map<String, Object> arguments) throws IOException {
        String pathText = getString(arguments, "path");

        if (pathText == null || pathText.isBlank()) {
            return McpToolResult.fail("Missing required argument: path");
        }

        Path file = resolveSafePath(pathText);

        if (!Files.exists(file)) {
            return McpToolResult.fail("File does not exist: " + pathText);
        }

        if (!Files.isRegularFile(file)) {
            return McpToolResult.fail("Path is not a regular file: " + pathText);
        }

        long size = Files.size(file);
        if (size > MAX_READ_FILE_SIZE_BYTES) {
            return McpToolResult.fail("File is too large to read. size=" + size);
        }

        String content = Files.readString(file, StandardCharsets.UTF_8);

        return McpToolResult.success(Map.of(
                "path", ROOT_DIR.relativize(file).toString(),
                "size", size,
                "content", content
        ));
    }

    private McpToolResult searchFiles(Map<String, Object> arguments) throws IOException {
        String keyword = getString(arguments, "keyword");

        if (keyword == null || keyword.isBlank()) {
            return McpToolResult.fail("Missing required argument: keyword");
        }

        List<String> matchedFiles;
        try (var stream = Files.walk(ROOT_DIR, 5)) {
            matchedFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains(keyword))
                    .map(path -> ROOT_DIR.relativize(path).toString())
                    .toList();
        }

        return McpToolResult.success(Map.of(
                "rootDir", ROOT_DIR.toString(),
                "keyword", keyword,
                "files", matchedFiles
        ));
    }

    private String getString(Map<String, Object> arguments, String key) {
        if (arguments == null) {
            return null;
        }

        Object value = arguments.get(key);
        return value == null ? null : value.toString();
    }

    private void ensureWorkspaceExists() throws IOException {
        if (!Files.exists(ROOT_DIR)) {
            Files.createDirectories(ROOT_DIR);
        }
    }

    private Path resolveSafePath(String relativePath) {
        Path resolved = ROOT_DIR.resolve(relativePath == null ? "" : relativePath)
                .normalize()
                .toAbsolutePath();

        if (!resolved.startsWith(ROOT_DIR)) {
            throw new IllegalArgumentException("Path access denied: " + relativePath);
        }

        return resolved;
    }
}
