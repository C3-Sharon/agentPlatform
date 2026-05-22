package com.sharon.agentplatform.plugin.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.plugin.core.LoadedPluginSkill;
import com.sharon.agentplatform.plugin.core.PluginSkillLoader;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PluginSkillService {

    private static final Logger log = LoggerFactory.getLogger(PluginSkillService.class);

    private final PluginSkillLoader pluginSkillLoader;
    private final SkillRegistry skillRegistry;
    private final Path pluginDir = Path.of("data", "plugins").toAbsolutePath().normalize();

    public PluginSkillService(PluginSkillLoader pluginSkillLoader, SkillRegistry skillRegistry) {
        this.pluginSkillLoader = pluginSkillLoader;
        this.skillRegistry = skillRegistry;
    }

    public List<LoadedPluginSkill> uploadAndLoad(MultipartFile file) {
        validateFile(file);
        createPluginDir();

        String savedFilename = UUID.randomUUID() + ".jar";
        Path jarPath = pluginDir.resolve(savedFilename).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, jarPath, StandardCopyOption.REPLACE_EXISTING);

            List<Skill> skills = pluginSkillLoader.loadSkillsFromJar(jarPath);
            List<LoadedPluginSkill> loadedPluginSkills = new ArrayList<>();

            for (Skill skill : skills) {
                skillRegistry.register(skill);
                loadedPluginSkills.add(toLoadedPluginSkill(skill, jarPath));
                log.info("Registered plugin skill: {}", skill.metadata().getName());
            }

            return loadedPluginSkills;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new BusinessException("Failed to load plugin jar", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Plugin jar is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException("Plugin jar file name is empty");
        }

        if (!originalFilename.toLowerCase().endsWith(".jar")) {
            throw new BusinessException("Only jar plugin files are supported");
        }
    }

    private void createPluginDir() {
        try {
            Files.createDirectories(pluginDir);
        } catch (IOException e) {
            throw new BusinessException("Failed to load plugin jar", e);
        }
    }

    private LoadedPluginSkill toLoadedPluginSkill(Skill skill, Path jarPath) {
        SkillMetadata metadata = skill.metadata();

        LoadedPluginSkill loadedPluginSkill = new LoadedPluginSkill();
        loadedPluginSkill.setSkillName(metadata.getName());
        loadedPluginSkill.setDisplayName(metadata.getDisplayName());
        loadedPluginSkill.setVersion(metadata.getVersion());
        loadedPluginSkill.setJarPath(jarPath.toString());
        loadedPluginSkill.setClassName(skill.getClass().getName());
        loadedPluginSkill.setLoadedAt(LocalDateTime.now());
        return loadedPluginSkill;
    }
}
