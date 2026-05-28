package com.sharon.agentplatform.agent.pending;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "agentplatform.pending-skill")
public class PendingSkillProperties {

    private String storeType = "memory";
    private Long ttlMinutes = 30L;

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public Long getTtlMinutes() {
        return ttlMinutes;
    }

    public void setTtlMinutes(Long ttlMinutes) {
        this.ttlMinutes = ttlMinutes;
    }
}
