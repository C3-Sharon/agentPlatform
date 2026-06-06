package com.sharon.agentplatform.plugin.spi;

public interface PluginContext {

    String getPluginId();

    String getJarPath();

    String getPlatformVersion();

    PluginLogger getLogger();
}
