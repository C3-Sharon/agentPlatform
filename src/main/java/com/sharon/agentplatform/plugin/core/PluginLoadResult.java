package com.sharon.agentplatform.plugin.core;

import com.sharon.agentplatform.skill.core.Skill;

import java.net.URLClassLoader;
import java.util.List;

public class PluginLoadResult {

    private URLClassLoader classLoader;
    private List<Skill> loadedSkills;

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<Skill> getLoadedSkills() {
        return loadedSkills;
    }

    public void setLoadedSkills(List<Skill> loadedSkills) {
        this.loadedSkills = loadedSkills;
    }
}
