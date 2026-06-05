package com.sharon.agentplatform.plugin.manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class PluginManifestLoader {

    public static final String MANIFEST_PATH = "META-INF/agent-skill.json";

    private final ObjectMapper objectMapper;

    public PluginManifestLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<PluginManifest> load(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry(MANIFEST_PATH);
            if (entry == null) {
                return Optional.empty();
            }

            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                return Optional.of(objectMapper.readValue(inputStream, PluginManifest.class));
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("Failed to read plugin manifest: " + MANIFEST_PATH, exception);
        }
    }
}
