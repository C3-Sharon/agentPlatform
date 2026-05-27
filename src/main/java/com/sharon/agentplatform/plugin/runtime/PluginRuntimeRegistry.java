package com.sharon.agentplatform.plugin.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class PluginRuntimeRegistry {

    private static final Logger log = LoggerFactory.getLogger(PluginRuntimeRegistry.class);

    private final ConcurrentMap<String, PluginRuntime> runtimes = new ConcurrentHashMap<>();

    public void register(PluginRuntime runtime) {
        if (runtime == null || runtime.getPluginId() == null || runtime.getPluginId().isBlank()) {
            return;
        }

        PluginRuntime oldRuntime = runtimes.put(runtime.getPluginId(), runtime);
        closeQuietly(oldRuntime);
    }

    public Optional<PluginRuntime> get(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(runtimes.get(pluginId));
    }

    public List<PluginRuntime> list() {
        return new ArrayList<>(runtimes.values());
    }

    public void unload(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            return;
        }
        PluginRuntime runtime = runtimes.remove(pluginId);
        closeQuietly(runtime);
    }

    public void unloadBySkillNames(Collection<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return;
        }

        for (PluginRuntime runtime : list()) {
            if (runtime.getSkillNames().stream().anyMatch(skillNames::contains)) {
                unload(runtime.getPluginId());
            }
        }
    }

    public boolean isLoaded(String pluginId) {
        return get(pluginId).isPresent();
    }

    private void closeQuietly(PluginRuntime runtime) {
        if (runtime == null) {
            return;
        }
        try {
            runtime.close();
        } catch (IOException exception) {
            log.warn("Failed to close plugin classLoader: {}", runtime.getPluginId(), exception);
        }
    }
}
