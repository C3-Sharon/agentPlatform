package com.sharon.agentplatform.skill.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.plugin.entity.PluginPackageEntity;
import com.sharon.agentplatform.plugin.entity.PluginSkillEntity;
import com.sharon.agentplatform.plugin.repository.PluginPackageRepository;
import com.sharon.agentplatform.plugin.repository.PluginSkillRepository;
import com.sharon.agentplatform.plugin.runtime.PluginRuntimeRegistry;
import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillRegistry;
import com.sharon.agentplatform.skill.dto.SkillMarketResponse;
import com.sharon.agentplatform.skill.dto.SkillStatsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SkillMarketService {

    private static final String SOURCE_BUILTIN = "BUILTIN";
    private static final String SOURCE_PLUGIN = "PLUGIN";

    private final SkillRegistry skillRegistry;
    private final SkillStatsService skillStatsService;
    private final PluginSkillRepository pluginSkillRepository;
    private final PluginPackageRepository pluginPackageRepository;
    private final PluginRuntimeRegistry pluginRuntimeRegistry;
    private final ObjectMapper objectMapper;

    public SkillMarketService(SkillRegistry skillRegistry,
                              SkillStatsService skillStatsService,
                              PluginSkillRepository pluginSkillRepository,
                              PluginPackageRepository pluginPackageRepository,
                              PluginRuntimeRegistry pluginRuntimeRegistry,
                              ObjectMapper objectMapper) {
        this.skillRegistry = skillRegistry;
        this.skillStatsService = skillStatsService;
        this.pluginSkillRepository = pluginSkillRepository;
        this.pluginPackageRepository = pluginPackageRepository;
        this.pluginRuntimeRegistry = pluginRuntimeRegistry;
        this.objectMapper = objectMapper;
    }

    public List<SkillMarketResponse> listMarketSkills() {
        Map<String, SkillStatsResponse> statsBySkillName = skillStatsService.getSkillStats()
                .stream()
                .collect(Collectors.toMap(SkillStatsResponse::getSkillName, item -> item, (left, right) -> left));

        List<PluginSkillEntity> pluginSkills = pluginSkillRepository.findAll();
        Map<String, List<PluginSkillEntity>> pluginSkillsByName = pluginSkills.stream()
                .collect(Collectors.groupingBy(PluginSkillEntity::getSkillName));
        Map<String, PluginPackageEntity> packagesByPluginId = pluginPackageRepository.findAll()
                .stream()
                .collect(Collectors.toMap(PluginPackageEntity::getPluginId, item -> item, (left, right) -> left));

        Map<String, SkillMarketResponse> market = new LinkedHashMap<>();

        for (Skill skill : skillRegistry.listAll()) {
            SkillMetadata metadata = skill.metadata();
            if (metadata == null || isBlank(metadata.getName())) {
                continue;
            }

            PluginSkillEntity pluginSkill = firstPluginSkill(pluginSkillsByName.get(metadata.getName()));
            SkillMarketResponse response = fromRegisteredSkill(skill, metadata, pluginSkill, packagesByPluginId);
            response.setStats(statsBySkillName.get(metadata.getName()));
            market.put(metadata.getName(), response);
        }

        for (PluginSkillEntity pluginSkill : pluginSkills) {
            if (isBlank(pluginSkill.getSkillName()) || market.containsKey(pluginSkill.getSkillName())) {
                continue;
            }

            SkillMarketResponse response = fromPluginRecord(pluginSkill, packagesByPluginId.get(pluginSkill.getPluginId()));
            response.setStats(statsBySkillName.get(pluginSkill.getSkillName()));
            market.put(pluginSkill.getSkillName(), response);
        }

        return new ArrayList<>(market.values());
    }

    private SkillMarketResponse fromRegisteredSkill(Skill skill,
                                                    SkillMetadata metadata,
                                                    PluginSkillEntity pluginSkill,
                                                    Map<String, PluginPackageEntity> packagesByPluginId) {
        SkillMarketResponse response = new SkillMarketResponse();
        response.setSkillName(metadata.getName());
        response.setDisplayName(metadata.getDisplayName());
        response.setDescription(metadata.getDescription());
        response.setVersion(metadata.getVersion());
        response.setParameterSchema(metadata.getParameterSchema());
        response.setDependencies(metadata.getDependencies());
        response.setEnabled(skillRegistry.isEnabled(metadata.getName()));
        response.setRegistered(true);
        response.setClassName(skill.getClass().getName());

        if (pluginSkill == null) {
            response.setSourceType(SOURCE_BUILTIN);
            response.setRuntimeLoaded(null);
            return response;
        }

        PluginPackageEntity pluginPackage = packagesByPluginId.get(pluginSkill.getPluginId());
        response.setSourceType(SOURCE_PLUGIN);
        response.setPluginId(pluginSkill.getPluginId());
        response.setPluginStatus(pluginPackage == null ? null : pluginPackage.getStatus());
        response.setRuntimeLoaded(pluginRuntimeRegistry.isLoaded(pluginSkill.getPluginId()));
        response.setClassName(pluginSkill.getClassName());
        response.setCreatedAt(pluginSkill.getCreatedAt());
        response.setUpdatedAt(pluginSkill.getUpdatedAt());
        applyPluginManifest(response, pluginPackage);
        applyMarketMetadata(response, parseMetadata(pluginSkill.getMarketMetadataJson()));
        return response;
    }

    private SkillMarketResponse fromPluginRecord(PluginSkillEntity pluginSkill, PluginPackageEntity pluginPackage) {
        Map<String, Object> metadata = parseMetadata(pluginSkill.getMetadataJson());
        Map<String, Object> marketMetadata = parseMetadata(pluginSkill.getMarketMetadataJson());

        SkillMarketResponse response = new SkillMarketResponse();
        response.setSkillName(pluginSkill.getSkillName());
        response.setDisplayName(firstNonBlank(pluginSkill.getDisplayName(), stringValue(metadata.get("displayName"))));
        response.setDescription(firstNonBlank(pluginSkill.getDescription(), stringValue(metadata.get("description"))));
        response.setVersion(firstNonBlank(pluginSkill.getVersion(), stringValue(metadata.get("version"))));
        response.setSourceType(SOURCE_PLUGIN);
        response.setEnabled(false);
        response.setRegistered(false);
        response.setPluginId(pluginSkill.getPluginId());
        response.setPluginStatus(pluginPackage == null ? null : pluginPackage.getStatus());
        response.setRuntimeLoaded(pluginRuntimeRegistry.isLoaded(pluginSkill.getPluginId()));
        response.setClassName(pluginSkill.getClassName());
        response.setParameterSchema(metadata.get("parameterSchema"));
        response.setDependencies(toStringList(metadata.get("dependencies")));
        response.setCreatedAt(pluginSkill.getCreatedAt());
        response.setUpdatedAt(pluginSkill.getUpdatedAt());
        applyPluginManifest(response, pluginPackage);
        applyMarketMetadata(response, marketMetadata);
        return response;
    }

    private PluginSkillEntity firstPluginSkill(List<PluginSkillEntity> pluginSkills) {
        if (pluginSkills == null || pluginSkills.isEmpty()) {
            return null;
        }
        return pluginSkills.get(0);
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        if (isBlank(metadataJson)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<>() {});
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private void applyPluginManifest(SkillMarketResponse response, PluginPackageEntity pluginPackage) {
        if (pluginPackage == null) {
            return;
        }
        Map<String, Object> manifest = parseMetadata(pluginPackage.getManifestJson());
        Object pluginValue = manifest.get("plugin");
        if (!(pluginValue instanceof Map<?, ?> plugin)) {
            return;
        }

        response.setPluginName(stringValue(plugin.get("name")));
        response.setPluginDisplayName(stringValue(plugin.get("displayName")));
        response.setPluginAuthor(stringValue(plugin.get("author")));
        response.setPluginHomepage(stringValue(plugin.get("homepage")));
    }

    private void applyMarketMetadata(SkillMarketResponse response, Map<String, Object> marketMetadata) {
        if (marketMetadata == null || marketMetadata.isEmpty()) {
            return;
        }

        response.setCategory(stringValue(marketMetadata.get("category")));
        response.setTags(toStringList(marketMetadata.get("tags")));
        response.setExamples(marketMetadata.get("examples"));
        response.setPermissions(toStringList(marketMetadata.get("permissions")));
    }

    private List<String> toStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
