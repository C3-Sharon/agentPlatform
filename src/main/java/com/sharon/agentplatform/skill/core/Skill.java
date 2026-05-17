package com.sharon.agentplatform.skill.core;

public interface Skill {

    SkillMetadata metadata();

    SkillResult execute(SkillContext context);
}