package com.sharon.agentplatform.plugin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.plugin.core.LoadedPluginSkill;
import com.sharon.agentplatform.plugin.core.PluginLoadResult;
import com.sharon.agentplatform.plugin.core.PluginSkillLoader;
import com.sharon.agentplatform.plugin.dto.PluginPackageResponse;
import com.sharon.agentplatform.plugin.dto.PluginPreviewResponse;
import com.sharon.agentplatform.plugin.dto.PluginPreviewSkillResponse;
import com.sharon.agentplatform.plugin.dto.PluginRuntimeResponse;
import com.sharon.agentplatform.plugin.dto.PluginSkillResponse;
import com.sharon.agentplatform.plugin.entity.PluginPackageEntity;
import com.sharon.agentplatform.plugin.entity.PluginSkillEntity;
import com.sharon.agentplatform.plugin.manifest.PluginManifest;
import com.sharon.agentplatform.plugin.manifest.PluginManifestLoader;
import com.sharon.agentplatform.plugin.manifest.PluginManifestSkill;
import com.sharon.agentplatform.plugin.permission.PluginPermissionPolicy;
import com.sharon.agentplatform.plugin.repository.PluginPackageRepository;
import com.sharon.agentplatform.plugin.repository.PluginSkillRepository;
import com.sharon.agentplatform.plugin.runtime.PluginRuntime;
import com.sharon.agentplatform.plugin.runtime.PluginRuntimeRegistry;
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

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";
    public static final String STATUS_FAILED = "FAILED";

    private static final Logger log = LoggerFactory.getLogger(PluginSkillService.class);

    private final PluginSkillLoader pluginSkillLoader;
    private final SkillRegistry skillRegistry;
    private final PluginPackageRepository pluginPackageRepository;
    private final PluginSkillRepository pluginSkillRepository;
    private final PluginSkillValidator pluginSkillValidator;
    private final PluginRuntimeRegistry pluginRuntimeRegistry;
    private final PluginManifestLoader pluginManifestLoader;
    private final PluginPermissionPolicy pluginPermissionPolicy;
    private final ObjectMapper objectMapper;
    private final Path pluginDir = Path.of("data", "plugins").toAbsolutePath().normalize();

    public PluginSkillService(PluginSkillLoader pluginSkillLoader,
                              SkillRegistry skillRegistry,
                              PluginPackageRepository pluginPackageRepository,
                              PluginSkillRepository pluginSkillRepository,
                              PluginSkillValidator pluginSkillValidator,
                              PluginRuntimeRegistry pluginRuntimeRegistry,
                              PluginManifestLoader pluginManifestLoader,
                              PluginPermissionPolicy pluginPermissionPolicy,
                              ObjectMapper objectMapper) {
        this.pluginSkillLoader = pluginSkillLoader;
        this.skillRegistry = skillRegistry;
        this.pluginPackageRepository = pluginPackageRepository;
        this.pluginSkillRepository = pluginSkillRepository;
        this.pluginSkillValidator = pluginSkillValidator;
        this.pluginRuntimeRegistry = pluginRuntimeRegistry;
        this.pluginManifestLoader = pluginManifestLoader;
        this.pluginPermissionPolicy = pluginPermissionPolicy;
        this.objectMapper = objectMapper;
    }

    public List<LoadedPluginSkill> uploadAndLoad(MultipartFile file) {
        return uploadAndLoadWithPluginId(file).loadedSkills();
    }

    public PluginPreviewResponse previewPlugin(MultipartFile file) {
        validateFile(file);

        PluginPreviewResponse response = new PluginPreviewResponse();
        response.setFileName(file.getOriginalFilename());

        Path tempJar = null;
        PluginLoadResult loadResult = null;
        try (InputStream inputStream = file.getInputStream()) {
            tempJar = Files.createTempFile("plugin-preview-", ".jar").toAbsolutePath().normalize();
            Files.copy(inputStream, tempJar, StandardCopyOption.REPLACE_EXISTING);

            PluginManifest manifest = pluginManifestLoader.load(tempJar).orElse(null);
            response.setManifest(manifest);
            loadResult = pluginSkillLoader.loadWithClassLoader(tempJar);
            List<Skill> skills = loadResult.getLoadedSkills();

            for (Skill skill : skills) {
                SkillMetadata metadata = skill.metadata();
                PluginManifestSkill manifestSkill = findManifestSkill(manifest, metadata == null ? null : metadata.getName());
                response.getSkills().add(toPreviewSkillResponse(
                        skill,
                        manifestSkill
                ));
                if (manifestSkill != null) {
                    response.getWarnings().addAll(pluginPermissionPolicy.warnings(manifestSkill.getPermissions()));
                }
            }

            try {
                pluginSkillValidator.validateForBootstrap(skills, manifest);
            } catch (BusinessException exception) {
                response.getErrors().add(exception.getMessage());
            }

            for (PluginPreviewSkillResponse skill : response.getSkills()) {
                if (Boolean.TRUE.equals(skill.getConflict())) {
                    response.getErrors().add(skill.getConflictMessage());
                }
            }

            response.setInstallable(response.getErrors().isEmpty());
            return response;
        } catch (BusinessException exception) {
            response.getErrors().add(exception.getMessage());
            response.setInstallable(false);
            return response;
        } catch (IOException | RuntimeException exception) {
            response.getErrors().add("Failed to preview plugin jar: " + exception.getMessage());
            response.setInstallable(false);
            return response;
        } finally {
            closeLoadResult(loadResult);
            deleteTempJar(tempJar);
        }
    }

    public LoadedPluginUpload uploadAndLoadWithPluginId(MultipartFile file) {
        validateFile(file);
        createPluginDir();

        String pluginId = UUID.randomUUID().toString().replace("-", "");
        String originalFilename = file.getOriginalFilename();
        String savedFilename = pluginId + ".jar";
        Path jarPath = pluginDir.resolve(savedFilename).normalize();
        LocalDateTime now = LocalDateTime.now();

        PluginPackageEntity pluginPackage = new PluginPackageEntity();
        pluginPackage.setPluginId(pluginId);
        pluginPackage.setFileName(originalFilename);
        pluginPackage.setJarPath(jarPath.toString());
        pluginPackage.setStatus(STATUS_FAILED);
        pluginPackage.setUploadedAt(now);
        pluginPackage.setCreatedAt(now);
        pluginPackage.setUpdatedAt(now);

        PluginLoadResult loadResult = null;
        List<String> registeredSkillNames = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, jarPath, StandardCopyOption.REPLACE_EXISTING);

            PluginManifest manifest = pluginManifestLoader.load(jarPath).orElse(null);
            pluginPackage.setManifestJson(toNullableJson(manifest));
            loadResult = pluginSkillLoader.loadWithClassLoader(jarPath);
            List<Skill> skills = loadResult.getLoadedSkills();
            pluginSkillValidator.validateForUpload(skills, manifest);
            List<LoadedPluginSkill> loadedPluginSkills = registerAndPersistSkills(pluginId, jarPath, skills, manifest, true);
            registeredSkillNames = loadedPluginSkills.stream()
                    .map(LoadedPluginSkill::getSkillName)
                    .toList();

            pluginPackage.setStatus(STATUS_ENABLED);
            pluginPackage.setErrorMessage(null);
            LocalDateTime loadedAt = LocalDateTime.now();
            pluginPackage.setLoadedAt(loadedAt);
            pluginPackage.setUpdatedAt(loadedAt);
            pluginPackageRepository.save(pluginPackage);
            registerRuntime(pluginId, jarPath, loadResult, registeredSkillNames, loadedAt);

            return new LoadedPluginUpload(pluginId, loadedPluginSkills);
        } catch (BusinessException e) {
            unregisterSkills(registeredSkillNames);
            disablePluginSkillRecords(pluginId, registeredSkillNames);
            closeLoadResult(loadResult);
            saveFailedPackage(pluginPackage, e);
            throw e;
        } catch (IOException | RuntimeException e) {
            unregisterSkills(registeredSkillNames);
            disablePluginSkillRecords(pluginId, registeredSkillNames);
            closeLoadResult(loadResult);
            saveFailedPackage(pluginPackage, e);
            throw new BusinessException("Failed to load plugin jar", e);
        }
    }

    public List<PluginPackageResponse> listPlugins() {
        return pluginPackageRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toPackageResponse)
                .toList();
    }

    public List<PluginRuntimeResponse> listPluginRuntimes() {
        return pluginRuntimeRegistry.list()
                .stream()
                .map(this::toRuntimeResponse)
                .toList();
    }

    public PluginPackageResponse disablePlugin(String pluginId) {
        PluginPackageEntity pluginPackage = findPluginPackage(pluginId);
        List<PluginSkillEntity> skills = pluginSkillRepository.findByPluginId(pluginId);
        LocalDateTime now = LocalDateTime.now();

        for (PluginSkillEntity skill : skills) {
            skillRegistry.unregister(skill.getSkillName());
            skill.setEnabled(false);
            skill.setUpdatedAt(now);
        }
        pluginSkillRepository.saveAll(skills);
        pluginRuntimeRegistry.unload(pluginId);

        pluginPackage.setStatus(STATUS_DISABLED);
        pluginPackage.setUpdatedAt(now);
        pluginPackageRepository.save(pluginPackage);
        return toPackageResponse(pluginPackage);
    }

    public PluginPackageResponse enablePlugin(String pluginId) {
        PluginPackageEntity pluginPackage = findPluginPackage(pluginId);
        try {
            loadAndEnablePackage(pluginPackage);
            return toPackageResponse(pluginPackageRepository.findByPluginId(pluginId).orElse(pluginPackage));
        } catch (RuntimeException exception) {
            pluginPackage.setStatus(STATUS_FAILED);
            pluginPackage.setErrorMessage(exception.getMessage());
            pluginPackage.setUpdatedAt(LocalDateTime.now());
            pluginPackageRepository.save(pluginPackage);
            throw exception;
        }
    }

    public void loadEnabledPluginsOnStartup() {
        for (PluginPackageEntity pluginPackage : pluginPackageRepository.findByStatus(STATUS_ENABLED)) {
            try {
                loadAndEnablePackage(pluginPackage);
                log.info("Restored enabled plugin: {}", pluginPackage.getPluginId());
            } catch (Exception exception) {
                log.warn("Failed to restore plugin: {}", pluginPackage.getPluginId(), exception);
                pluginPackage.setStatus(STATUS_FAILED);
                pluginPackage.setErrorMessage(exception.getMessage());
                pluginPackage.setUpdatedAt(LocalDateTime.now());
                pluginPackageRepository.save(pluginPackage);
            }
        }
    }

    private void loadAndEnablePackage(PluginPackageEntity pluginPackage) {
        Path jarPath = Path.of(pluginPackage.getJarPath()).toAbsolutePath().normalize();
        PluginLoadResult loadResult = null;
        List<String> registeredSkillNames = new ArrayList<>();
        try {
            loadResult = pluginSkillLoader.loadWithClassLoader(jarPath);
            PluginManifest manifest = pluginManifestLoader.load(jarPath).orElse(null);
            List<Skill> skills = loadResult.getLoadedSkills();
            pluginSkillValidator.validateForBootstrap(skills, manifest);
            List<LoadedPluginSkill> loadedPluginSkills = registerAndPersistSkills(pluginPackage.getPluginId(), jarPath, skills, manifest, true);
            registeredSkillNames = loadedPluginSkills.stream()
                    .map(LoadedPluginSkill::getSkillName)
                    .toList();

            LocalDateTime loadedAt = LocalDateTime.now();
            pluginPackage.setStatus(STATUS_ENABLED);
            pluginPackage.setErrorMessage(null);
            pluginPackage.setManifestJson(toNullableJson(manifest));
            pluginPackage.setLoadedAt(loadedAt);
            pluginPackage.setUpdatedAt(loadedAt);
            pluginPackageRepository.save(pluginPackage);
            registerRuntime(pluginPackage.getPluginId(), jarPath, loadResult, registeredSkillNames, loadedAt);
        } catch (RuntimeException exception) {
            unregisterSkills(registeredSkillNames);
            disablePluginSkillRecords(pluginPackage.getPluginId(), registeredSkillNames);
            closeLoadResult(loadResult);
            throw exception;
        }
    }

    private List<LoadedPluginSkill> registerAndPersistSkills(String pluginId,
                                                             Path jarPath,
                                                             List<Skill> skills,
                                                             PluginManifest manifest,
                                                             boolean enabled) {
        List<LoadedPluginSkill> loadedPluginSkills = new ArrayList<>();
        List<String> registeredSkillNames = new ArrayList<>();
        try {
            for (Skill skill : skills) {
                skillRegistry.register(skill);
                String skillName = skill.metadata().getName();
                registeredSkillNames.add(skillName);
                LoadedPluginSkill loadedPluginSkill = toLoadedPluginSkill(skill, jarPath);
                loadedPluginSkills.add(loadedPluginSkill);
                upsertPluginSkill(pluginId, skill, findManifestSkill(manifest, skillName), enabled);
                log.info("Registered plugin skill: {}", skillName);
            }
        } catch (RuntimeException exception) {
            unregisterSkills(registeredSkillNames);
            disablePluginSkillRecords(pluginId, registeredSkillNames);
            throw exception;
        }
        return loadedPluginSkills;
    }

    private void registerRuntime(String pluginId,
                                 Path jarPath,
                                 PluginLoadResult loadResult,
                                 List<String> skillNames,
                                 LocalDateTime loadedAt) {
        if (loadResult == null || loadResult.getClassLoader() == null) {
            return;
        }

        PluginRuntime runtime = new PluginRuntime();
        runtime.setPluginId(pluginId);
        runtime.setJarPath(jarPath.toString());
        runtime.setClassLoader(loadResult.getClassLoader());
        runtime.setSkillNames(skillNames);
        runtime.setLoadedAt(loadedAt);
        pluginRuntimeRegistry.register(runtime);
    }

    private void closeLoadResult(PluginLoadResult loadResult) {
        if (loadResult == null || loadResult.getClassLoader() == null) {
            return;
        }
        try {
            loadResult.getClassLoader().close();
        } catch (IOException exception) {
            log.warn("Failed to close plugin classLoader after failed load", exception);
        }
    }

    private void deleteTempJar(Path tempJar) {
        if (tempJar == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempJar);
        } catch (IOException exception) {
            log.warn("Failed to delete plugin preview temp jar: {}", tempJar, exception);
        }
    }

    private void unregisterSkills(List<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return;
        }
        for (String skillName : skillNames) {
            skillRegistry.unregister(skillName);
        }
    }

    private void disablePluginSkillRecords(String pluginId, List<String> skillNames) {
        if (pluginId == null || pluginId.isBlank() || skillNames == null || skillNames.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (String skillName : skillNames) {
            pluginSkillRepository.findByPluginIdAndSkillName(pluginId, skillName)
                    .ifPresent(entity -> {
                        entity.setEnabled(false);
                        entity.setUpdatedAt(now);
                        pluginSkillRepository.save(entity);
                    });
        }
    }

    private void upsertPluginSkill(String pluginId, Skill skill, PluginManifestSkill manifestSkill, boolean enabled) {
        SkillMetadata metadata = skill.metadata();
        LocalDateTime now = LocalDateTime.now();
        PluginSkillEntity entity = pluginSkillRepository.findByPluginIdAndSkillName(pluginId, metadata.getName())
                .orElseGet(() -> {
                    PluginSkillEntity created = new PluginSkillEntity();
                    created.setPluginId(pluginId);
                    created.setSkillName(metadata.getName());
                    created.setCreatedAt(now);
                    return created;
                });

        entity.setDisplayName(metadata.getDisplayName());
        entity.setDescription(metadata.getDescription());
        entity.setVersion(metadata.getVersion());
        entity.setClassName(skill.getClass().getName());
        entity.setEnabled(enabled);
        entity.setMetadataJson(toJson(metadata));
        entity.setMarketMetadataJson(toNullableJson(manifestSkill));
        entity.setUpdatedAt(now);
        pluginSkillRepository.save(entity);
    }

    private void saveFailedPackage(PluginPackageEntity pluginPackage, Exception exception) {
        pluginPackage.setStatus(STATUS_FAILED);
        pluginPackage.setErrorMessage(exception.getMessage());
        pluginPackage.setUpdatedAt(LocalDateTime.now());
        pluginPackageRepository.save(pluginPackage);
    }

    private PluginPackageEntity findPluginPackage(String pluginId) {
        return pluginPackageRepository.findByPluginId(pluginId)
                .orElseThrow(() -> new BusinessException("Plugin package not found: " + pluginId));
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
        loadedPluginSkill.setDescription(metadata.getDescription());
        loadedPluginSkill.setVersion(metadata.getVersion());
        loadedPluginSkill.setJarPath(jarPath.toString());
        loadedPluginSkill.setClassName(skill.getClass().getName());
        loadedPluginSkill.setMetadata(metadata);
        loadedPluginSkill.setLoadedAt(LocalDateTime.now());
        return loadedPluginSkill;
    }

    private PluginPackageResponse toPackageResponse(PluginPackageEntity entity) {
        PluginPackageResponse response = new PluginPackageResponse();
        response.setPluginId(entity.getPluginId());
        response.setFileName(entity.getFileName());
        response.setJarPath(entity.getJarPath());
        response.setStatus(entity.getStatus());
        response.setErrorMessage(entity.getErrorMessage());
        response.setManifest(parseJson(entity.getManifestJson()));
        response.setUploadedAt(entity.getUploadedAt());
        response.setLoadedAt(entity.getLoadedAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setSkills(pluginSkillRepository.findByPluginId(entity.getPluginId()).stream()
                .map(this::toSkillResponse)
                .toList());
        return response;
    }

    private PluginSkillResponse toSkillResponse(PluginSkillEntity entity) {
        PluginSkillResponse response = new PluginSkillResponse();
        response.setPluginId(entity.getPluginId());
        response.setSkillName(entity.getSkillName());
        response.setDisplayName(entity.getDisplayName());
        response.setDescription(entity.getDescription());
        response.setVersion(entity.getVersion());
        response.setClassName(entity.getClassName());
        response.setEnabled(entity.getEnabled());
        response.setMetadata(parseJson(entity.getMetadataJson()));
        response.setMarketMetadata(parseJson(entity.getMarketMetadataJson()));
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private PluginPreviewSkillResponse toPreviewSkillResponse(Skill skill, PluginManifestSkill manifestSkill) {
        SkillMetadata metadata = skill.metadata();
        PluginPreviewSkillResponse response = new PluginPreviewSkillResponse();
        response.setClassName(skill.getClass().getName());
        response.setMarketMetadata(manifestSkill);
        if (metadata == null) {
            response.setConflict(false);
            return response;
        }

        response.setSkillName(metadata.getName());
        response.setDisplayName(metadata.getDisplayName());
        response.setDescription(metadata.getDescription());
        response.setVersion(metadata.getVersion());
        response.setParameterSchema(metadata.getParameterSchema());
        response.setDependencies(metadata.getDependencies());

        if (manifestSkill != null) {
            response.setPermissionDetails(pluginPermissionPolicy.describe(manifestSkill.getPermissions()));
            response.setPermissionRiskLevel(pluginPermissionPolicy.highestRiskLevel(manifestSkill.getPermissions()));
        }

        boolean conflict = metadata.getName() != null && skillRegistry.getSkill(metadata.getName()).isPresent();
        response.setConflict(conflict);
        response.setConflictMessage(conflict ? "Skill name already exists: " + metadata.getName() : null);
        return response;
    }

    private PluginRuntimeResponse toRuntimeResponse(PluginRuntime runtime) {
        PluginRuntimeResponse response = new PluginRuntimeResponse();
        response.setPluginId(runtime.getPluginId());
        response.setJarPath(runtime.getJarPath());
        response.setSkillNames(runtime.getSkillNames());
        response.setClassLoaderType(runtime.getClassLoader() == null ? null : runtime.getClassLoader().getClass().getName());
        response.setLoadedAt(runtime.getLoadedAt());
        response.setClosed(runtime.isClosed());
        return response;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            return String.valueOf(value);
        }
    }

    private String toNullableJson(Object value) {
        return value == null ? null : toJson(value);
    }

    private PluginManifestSkill findManifestSkill(PluginManifest manifest, String skillName) {
        if (manifest == null || manifest.getSkills() == null || skillName == null) {
            return null;
        }
        return manifest.getSkills()
                .stream()
                .filter(skill -> skillName.equals(skill.getName()))
                .findFirst()
                .orElse(null);
    }

    private Object parseJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception exception) {
            return json;
        }
    }

    public record LoadedPluginUpload(String pluginId, List<LoadedPluginSkill> loadedSkills) {
    }
}
