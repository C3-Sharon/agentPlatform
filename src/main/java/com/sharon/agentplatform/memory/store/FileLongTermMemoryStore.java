package com.sharon.agentplatform.memory.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.memory.core.LongTermMemory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
@Component
public class FileLongTermMemoryStore implements LongTermMemoryStore {

    private static final Path MEMORY_FILE = Path.of("data", "memory", "long-term-memory.json")
            .toAbsolutePath()
            .normalize();

    private final ObjectMapper objectMapper;

    public FileLongTermMemoryStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public synchronized void add(LongTermMemory memory) {
        List<LongTermMemory> memories = readAll();
        memories.add(memory);
        writeAll(memories);
    }

    @Override
    public synchronized List<LongTermMemory> listByConversationId(String conversationId) {
        return readAll()
                .stream()
                .filter(memory -> conversationId.equals(memory.getConversationId()))
                .toList();
    }

    @Override
    public synchronized void clearByConversationId(String conversationId) {
        List<LongTermMemory> remained = readAll()
                .stream()
                .filter(memory -> !conversationId.equals(memory.getConversationId()))
                .toList();

        writeAll(remained);
    }

    private List<LongTermMemory> readAll() {
        try {
            ensureFileExists();

            if (Files.size(MEMORY_FILE) == 0) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(
                    MEMORY_FILE.toFile(),
                    new TypeReference<List<LongTermMemory>>() {
                    }
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read long-term memory file", e);
        }
    }

    private void writeAll(List<LongTermMemory> memories) {
        try {
            ensureFileExists();

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(MEMORY_FILE.toFile(), memories);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write long-term memory file", e);
        }
    }

    private void ensureFileExists() throws IOException {
        Path parent = MEMORY_FILE.getParent();

        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        if (!Files.exists(MEMORY_FILE)) {
            Files.createFile(MEMORY_FILE);
            Files.writeString(MEMORY_FILE, "[]");
        }
    }
}