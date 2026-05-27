package com.sharon.agentplatform.plugin.runtime;

import java.io.IOException;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginRuntime {

    private String pluginId;
    private String jarPath;
    private URLClassLoader classLoader;
    private List<String> skillNames = new ArrayList<>();
    private LocalDateTime loadedAt;
    private boolean closed;

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<String> getSkillNames() {
        return Collections.unmodifiableList(skillNames);
    }

    public void setSkillNames(List<String> skillNames) {
        this.skillNames = skillNames == null ? new ArrayList<>() : new ArrayList<>(skillNames);
    }

    public LocalDateTime getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(LocalDateTime loadedAt) {
        this.loadedAt = loadedAt;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }
        if (classLoader != null) {
            classLoader.close();
        }
        closed = true;
    }
}
