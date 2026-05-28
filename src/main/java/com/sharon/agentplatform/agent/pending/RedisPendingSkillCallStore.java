package com.sharon.agentplatform.agent.pending;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.agent.core.PendingSkillCall;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = "agentplatform.pending-skill", name = "store-type", havingValue = "redis")
public class RedisPendingSkillCallStore implements PendingSkillCallStore {

    public static final String KEY_PATTERN = "agent:pending-skill:{conversationId}";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final PendingSkillProperties properties;

    public RedisPendingSkillCallStore(StringRedisTemplate stringRedisTemplate,
                                      ObjectMapper objectMapper,
                                      PendingSkillProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void save(String conversationId, PendingSkillCall pendingSkillCall) {
        if (conversationId == null || conversationId.isBlank() || pendingSkillCall == null) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(pendingSkillCall);
            stringRedisTemplate.opsForValue().set(redisKey(conversationId), json, ttl());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize pending skill call: " + exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to save pending skill call to Redis: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Optional<PendingSkillCall> get(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Optional.empty();
        }

        try {
            String json = stringRedisTemplate.opsForValue().get(redisKey(conversationId));
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, PendingSkillCall.class));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize pending skill call from Redis: " + exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read pending skill call from Redis: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void remove(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }

        try {
            stringRedisTemplate.delete(redisKey(conversationId));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to remove pending skill call from Redis: " + exception.getMessage(), exception);
        }
    }

    @Override
    public String storeType() {
        return "redis";
    }

    private String redisKey(String conversationId) {
        return "agent:pending-skill:" + conversationId;
    }

    private Duration ttl() {
        long ttlMinutes = properties.getTtlMinutes() == null || properties.getTtlMinutes() <= 0
                ? 30L
                : properties.getTtlMinutes();
        return Duration.ofMinutes(ttlMinutes);
    }
}
