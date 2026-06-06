package com.sharon.agentplatform.plugin.spi;

public interface PluginLogger {

    void info(String message);

    void warn(String message);

    void error(String message);
}
