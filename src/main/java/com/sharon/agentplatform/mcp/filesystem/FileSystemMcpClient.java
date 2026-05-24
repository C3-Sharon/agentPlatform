package com.sharon.agentplatform.mcp.filesystem;

import com.sharon.agentplatform.mcp.core.McpClient;
import com.sharon.agentplatform.mcp.core.McpToolMetadata;
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
    public List<McpToolMetadata> listTools() {
        return List.of(
                metadata(
                        "listFiles",
                        "\u5217\u51fa\u6587\u4ef6",
                        "\u5217\u51fa workspace \u76ee\u5f55\u4e0b\u6307\u5b9a\u8def\u5f84\u7684\u6587\u4ef6",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "path", Map.of(
                                                "type", "string",
                                                "description", "\u76f8\u5bf9\u4e8e workspace \u7684\u76ee\u5f55\u8def\u5f84\uff0c\u53ef\u4e3a\u7a7a"
                                        )
                                )
                        )
                ),
                metadata(
                        "readFile",
                        "\u8bfb\u53d6\u6587\u4ef6",
                        "\u8bfb\u53d6 workspace \u76ee\u5f55\u4e0b\u6307\u5b9a\u6587\u4ef6\u7684\u6587\u672c\u5185\u5bb9",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "path", Map.of(
                                                "type", "string",
                                                "description", "\u76f8\u5bf9\u4e8e workspace \u7684\u6587\u4ef6\u8def\u5f84"
                                        )
                                ),
                                "required", List.of("path")
                        )
                ),
                metadata(
                        "searchFiles",
                        "\u641c\u7d22\u6587\u4ef6",
                        "\u6839\u636e\u6587\u4ef6\u540d\u5173\u952e\u5b57\u641c\u7d22 workspace \u76ee\u5f55\u4e0b\u7684\u6587\u4ef6",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "keyword", Map.of(
                                                "type", "string",
                                                "description", "\u6587\u4ef6\u540d\u5173\u952e\u5b57"
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

    private McpToolMetadata metadata(String name,
                                     String displayName,
                                     String description,
                                     Map<String, Object> inputSchema) {
        McpToolMetadata metadata = new McpToolMetadata();
        metadata.setName(name);
        metadata.setDisplayName(displayName);
        metadata.setDescription(description);
        metadata.setVersion("1.0.0");
        metadata.setInputSchema(inputSchema);
        return metadata;
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
