package com.sharon.agentplatform.plugin.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PluginMarketBootstrap implements ApplicationRunner {

    private final PluginSkillService pluginSkillService;

    public PluginMarketBootstrap(PluginSkillService pluginSkillService) {
        this.pluginSkillService = pluginSkillService;
    }

    @Override
    public void run(ApplicationArguments args) {
        pluginSkillService.loadEnabledPluginsOnStartup();
    }
}
