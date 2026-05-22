package com.sharon.agentplatform.plugin.core;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.skill.core.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class PluginSkillLoader {

    private static final Logger log = LoggerFactory.getLogger(PluginSkillLoader.class);

    public List<Skill> loadSkillsFromJar(Path jarPath) {
        if (jarPath == null || !Files.exists(jarPath)) {
            throw new BusinessException("Plugin jar does not exist: " + jarPath);
        }

        List<Skill> skills = new ArrayList<>();

        try {
            URL jarUrl = jarPath.toUri().toURL();
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{jarUrl},
                    Thread.currentThread().getContextClassLoader()
            );

            try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                jarFile.stream()
                        .filter(this::isClassEntry)
                        .map(this::toClassName)
                        .forEach(className -> loadSkillClass(classLoader, className, skills));
            }
        } catch (IOException e) {
            throw new BusinessException("Failed to read plugin jar: " + jarPath, e);
        }

        if (skills.isEmpty()) {
            throw new BusinessException("No Skill implementation found in jar: " + jarPath);
        }

        return skills;
    }

    private boolean isClassEntry(JarEntry entry) {
        return !entry.isDirectory() && entry.getName().endsWith(".class");
    }

    private String toClassName(JarEntry entry) {
        String name = entry.getName();
        return name.substring(0, name.length() - ".class".length()).replace('/', '.');
    }

    private void loadSkillClass(URLClassLoader classLoader, String className, List<Skill> skills) {
        try {
            Class<?> candidateClass = classLoader.loadClass(className);

            if (!Skill.class.isAssignableFrom(candidateClass)) {
                return;
            }

            int modifiers = candidateClass.getModifiers();
            if (candidateClass.isInterface() || Modifier.isAbstract(modifiers)) {
                return;
            }

            Object instance = candidateClass.getConstructor().newInstance();
            Skill skill = (Skill) instance;
            String skillName = skill.metadata().getName();

            skills.add(skill);
            log.info("Loaded plugin skill: {}, {}", skillName, className);
        } catch (Throwable e) {
            log.warn("Failed to load plugin class: {}", className, e);
        }
    }
}
