package com.sharon.agentplatform.plugin.spi;

import java.util.List;

public interface PluginContext {

    String getPluginId();

    String getJarPath();

    String getPlatformVersion();

    PluginLogger getLogger();

    PluginMcpClient getMcpClient();

    List<String> getPermissions();

    boolean hasPermission(String permission);

    void requirePermission(String permission);
}
