package com.sharon.agentplatform.plugin.manifest;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PluginManifestCompletenessChecker {

    public List<String> check(PluginManifest manifest) {
        List<String> warnings = new ArrayList<>();
        if (manifest == null) {
            warnings.add("Plugin manifest META-INF/agent-skill.json is recommended for Skill Market discovery");
            return warnings;
        }

        addIfBlank(warnings, manifest.getSchemaVersion(), "Plugin manifest schemaVersion is recommended");

        PluginManifestInfo plugin = manifest.getPlugin();
        if (plugin == null) {
            warnings.add("Plugin manifest plugin section is recommended");
        } else {
            addIfBlank(warnings, plugin.getName(), "Plugin manifest plugin.name is recommended");
            addIfBlank(warnings, plugin.getDisplayName(), "Plugin manifest plugin.displayName is recommended");
            addIfBlank(warnings, plugin.getAuthor(), "Plugin manifest plugin.author is recommended");
            addIfBlank(warnings, plugin.getVersion(), "Plugin manifest plugin.version is recommended");
            addIfBlank(warnings, plugin.getDescription(), "Plugin manifest plugin.description is recommended");
            addIfBlank(warnings, plugin.getLicense(), "Plugin manifest plugin.license is recommended");
            addIfBlank(warnings, plugin.getRepository(), "Plugin manifest plugin.repository is recommended");
            addIfBlank(warnings, plugin.getMinPlatformVersion(), "Plugin manifest plugin.minPlatformVersion is recommended");
        }

        if (manifest.getSkills() == null || manifest.getSkills().isEmpty()) {
            warnings.add("Plugin manifest skills section is recommended");
            return warnings;
        }

        for (PluginManifestSkill skill : manifest.getSkills()) {
            if (skill == null) {
                warnings.add("Plugin manifest skill entry should not be null");
                continue;
            }
            String skillName = isBlank(skill.getName()) ? "<unknown>" : skill.getName();
            addIfBlank(warnings, skill.getCategory(), "Plugin manifest skills[" + skillName + "].category is recommended");
            if (skill.getTags() == null || skill.getTags().isEmpty()) {
                warnings.add("Plugin manifest skills[" + skillName + "].tags is recommended");
            }
            if (skill.getExamples() == null || skill.getExamples().isEmpty()) {
                warnings.add("Plugin manifest skills[" + skillName + "].examples is recommended");
            }
            if (skill.getPermissions() == null || skill.getPermissions().isEmpty()) {
                warnings.add("Plugin manifest skills[" + skillName + "].permissions is recommended");
            }
        }

        return warnings;
    }

    private void addIfBlank(List<String> warnings, String value, String warning) {
        if (isBlank(value)) {
            warnings.add(warning);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
