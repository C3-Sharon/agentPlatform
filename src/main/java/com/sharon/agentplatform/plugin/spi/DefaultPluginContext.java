package com.sharon.agentplatform.plugin.spi;

public class DefaultPluginContext implements PluginContext {

    private String pluginId;
    private String jarPath;
    private String platformVersion;
    private PluginLogger logger;

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
}
