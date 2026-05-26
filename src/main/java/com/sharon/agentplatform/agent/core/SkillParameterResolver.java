package com.sharon.agentplatform.agent.core;

import com.sharon.agentplatform.skill.core.SkillMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SkillParameterResolver {

    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile(
            "([A-Za-z0-9_-]+)\\s*(?:=|:|\\uFF1A)\\s*([^,\\uFF0C.\\u3002\\r\\n]+)"
    );
    private static final Pattern BA_ACTION_PATTERN = Pattern.compile(
            "\\u628a\\s*(.+?)\\s*(?:\\u8f6c\\u6210|\\u8f6c\\u4e3a|\\u8f6c\\u6362\\u6210|\\u8f6c\\u6362\\u4e3a|\\u53d8\\u6210|\\u53cd\\u8f6c|\\u5206\\u6790|\\u5904\\u7406)"
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

    public Optional<InferredParam> inferSingleRequiredStringParam(
            String skillName,
            SkillMetadata metadata,
            String userMessage,
            Map<String, Object> params
    ) {
        String paramName = singleMissingRequiredStringParam(metadata, params);
        if (paramName == null) {
            return Optional.empty();
        }

        String inferredValue = extractAfterLastColon(userMessage);
        if (isBlank(inferredValue)) {
            inferredValue = extractBaActionContent(userMessage);
        }
        if (isBlank(inferredValue)) {
            inferredValue = extractRemainingExplicitCallContent(skillName, userMessage);
        }

        return cleanParamValue(inferredValue)
                .map(value -> new InferredParam(paramName, value));
    }

    public Optional<InferredParam> inferPendingSingleRequiredStringParam(
            SkillMetadata metadata,
            String userMessage,
            Map<String, Object> params
    ) {
        String paramName = singleMissingRequiredStringParam(metadata, params);
        if (paramName == null) {
            return Optional.empty();
        }

        return cleanParamValue(userMessage)
                .map(value -> new InferredParam(paramName, value));
    }

    private String singleMissingRequiredStringParam(SkillMetadata metadata, Map<String, Object> params) {
        List<String> requiredParams = getRequiredParams(metadata);
        if (requiredParams.size() != 1) {
            return null;
        }

        String paramName = requiredParams.get(0);
        Object currentValue = params == null ? null : params.get(paramName);
        if (currentValue != null && !currentValue.toString().isBlank()) {
            return null;
        }

        if (!isStringParam(metadata, paramName)) {
            return null;
        }

        return paramName;
    }

    private boolean isStringParam(SkillMetadata metadata, String paramName) {
        if (metadata == null || metadata.getParameterSchema() == null) {
            return false;
        }

        Object properties = metadata.getParameterSchema().get("properties");
        if (!(properties instanceof Map<?, ?> propertiesMap)) {
            return false;
        }

        Object paramSchema = propertiesMap.get(paramName);
        if (!(paramSchema instanceof Map<?, ?> paramSchemaMap)) {
            return false;
        }

        Object type = paramSchemaMap.get("type");
        return type != null && "string".equalsIgnoreCase(type.toString());
    }

    private String extractAfterLastColon(String message) {
        if (message == null) {
            return null;
        }

        int englishColon = message.lastIndexOf(':');
        int chineseColon = message.lastIndexOf('\uFF1A');
        int colonIndex = Math.max(englishColon, chineseColon);
        if (colonIndex < 0 || colonIndex + 1 >= message.length()) {
            return null;
        }

        return message.substring(colonIndex + 1);
    }

    private String extractBaActionContent(String message) {
        if (message == null) {
            return null;
        }

        Matcher matcher = BA_ACTION_PATTERN.matcher(message);
        String value = null;
        while (matcher.find()) {
            value = matcher.group(1);
        }
        return value;
    }

    private String extractRemainingExplicitCallContent(String skillName, String message) {
        if (message == null) {
            return null;
        }

        String remaining = message;
        remaining = remaining.replaceAll("\\u8bf7\\s*\\u8c03\\u7528", " ");
        remaining = remaining.replaceAll("\\u8c03\\u7528", " ");
        remaining = remaining.replaceAll("\\u4f7f\\u7528", " ");
        remaining = remaining.replaceAll("(?i)\\buse\\b", " ");
        remaining = remaining.replaceAll("(?i)\\bskill\\b", " ");
        remaining = remaining.replaceAll("\\u6280\\u80fd", " ");

        if (skillName != null && !skillName.isBlank()) {
            remaining = remaining.replace(skillName, " ");
        }

        remaining = remaining.replaceAll("^\\s*(\\u5e2e\\u6211|\\u8bf7|\\u5206\\u6790|\\u5904\\u7406|\\u67e5\\u8be2|\\u4e00\\u4e0b)\\s*", "");
        return remaining;
    }

    private Optional<String> cleanParamValue(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String cleaned = value.trim();
        cleaned = cleaned.replaceAll("^[\\s,\\uFF0C.\\u3002:\\uFF1A]+", "");
        cleaned = cleaned.replaceAll("[\\s,\\uFF0C.\\u3002:\\uFF1A]+$", "");

        if (cleaned.length() > 5000) {
            cleaned = cleaned.substring(0, 5000);
        }

        if (cleaned.length() < 2 || isControlOnly(cleaned)) {
            return Optional.empty();
        }

        if (cleaned.matches(".*\\u6280\\u80fd$") && cleaned.length() <= 8) {
            return Optional.empty();
        }

        return Optional.of(cleaned);
    }

    private boolean isControlOnly(String value) {
        String compact = value.replaceAll("\\s+", "");
        return compact.isBlank()
                || compact.matches("\\u8bf7\\u8c03\\u7528|\\u8c03\\u7528|\\u4f7f\\u7528|\\u6280\\u80fd")
                || "use".equalsIgnoreCase(compact)
                || "skill".equalsIgnoreCase(compact);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
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

    public record InferredParam(String name, String value) {
    }
}
