package com.sharon.agentplatform.plugin.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultPluginContext implements PluginContext {

    private String pluginId;
    private String jarPath;
    private String platformVersion;
    private PluginLogger logger;
    private List<String> permissions = new ArrayList<>();

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

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public PluginLogger getLogger() {
        return logger;
    }

    public void setLogger(PluginLogger logger) {
        this.logger = logger;
    }

    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public void setPermissions(List<String> permissions) {
        if (permissions == null) {
            this.permissions = new ArrayList<>();
            return;
        }

        this.permissions = permissions.stream()
                .filter(permission -> permission != null && !permission.isBlank())
                .distinct()
                .toList();
    }

    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }
        return permissions.contains(permission);
    }

    public void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            throw new PluginPermissionException("Plugin permission required: " + permission);
        }
    }
}
