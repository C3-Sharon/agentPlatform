package com.sharon.agentplatform.agent.core;

import com.sharon.agentplatform.skill.core.SkillMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SkillParameterResolver {

    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile(
            "([A-Za-z0-9_-]+)\\s*(?:=|:|\\uFF1A)\\s*([^,\\uFF0C.\\u3002\\r\\n]+)"
    );

    public List<String> getRequiredParams(SkillMetadata metadata) {
        if (metadata == null || metadata.getParameterSchema() == null) {
            return List.of();
        }

        Object required = metadata.getParameterSchema().get("required");
        if (!(required instanceof List<?> requiredList)) {
            return List.of();
        }

        List<String> params = new ArrayList<>();
        for (Object item : requiredList) {
            if (item != null && !item.toString().isBlank()) {
                params.add(item.toString());
            }
        }
        return params;
    }

    public List<String> findMissingParams(SkillMetadata metadata, Map<String, Object> params) {
        List<String> missingParams = new ArrayList<>();
        for (String requiredParam : getRequiredParams(metadata)) {
            Object value = params == null ? null : params.get(requiredParam);
            if (value == null || value.toString().isBlank()) {
                missingParams.add(requiredParam);
            }
        }
        return missingParams;
    }

    public Map<String, Object> extractParams(String message) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (message == null || message.isBlank()) {
            return params;
        }

        Matcher matcher = KEY_VALUE_PATTERN.matcher(message);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            if (!key.isBlank() && !value.isBlank()) {
                params.put(key, parseValue(value));
            }
        }

        return params;
    }

    private Object parseValue(String value) {
        if (value.matches("\\d+")) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                return value;
            }
        }

        return value;
    }
}
