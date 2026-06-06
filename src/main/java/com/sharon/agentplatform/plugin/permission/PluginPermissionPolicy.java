package com.sharon.agentplatform.plugin.permission;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PluginPermissionPolicy {

    private static final String LOW = "LOW";
    private static final String MEDIUM = "MEDIUM";
    private static final String HIGH = "HIGH";
    private static final String UNKNOWN = "UNKNOWN";

    private final Map<String, PluginPermissionDescriptor> descriptors = new LinkedHashMap<>();

    public PluginPermissionPolicy() {
        register("local-compute", "Local Compute", "Run local in-process computation.", LOW);
        register("network:http", "HTTP Network", "Call external HTTP endpoints.", MEDIUM);
        register("network:https", "HTTPS Network", "Call external HTTPS endpoints.", MEDIUM);
        register("filesystem:read", "File Read", "Read local filesystem data.", MEDIUM);
        register("filesystem:write", "File Write", "Write local filesystem data.", HIGH);
        register("mcp:call", "MCP Call", "Call MCP tools through platform or external endpoints.", MEDIUM);
        register("model:chat", "Model Chat", "Call chat models through platform capability.", MEDIUM);
        register("resource:read", "Resource Read", "Read platform conversation resources.", MEDIUM);
        register("resource:write", "Resource Write", "Write platform conversation resources.", HIGH);
        register("memory:read", "Memory Read", "Read platform memory data.", MEDIUM);
        register("memory:write", "Memory Write", "Write platform memory data.", HIGH);
    }

    public List<PluginPermissionDescriptor> describe(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }

        return permissions.stream()
                .filter(permission -> permission != null && !permission.isBlank())
                .distinct()
                .map(this::describe)
                .toList();
    }

    public List<String> warnings(List<String> permissions) {
        return describe(permissions).stream()
                .filter(descriptor -> !Boolean.TRUE.equals(descriptor.getKnown()))
                .map(descriptor -> "Unknown plugin permission: " + descriptor.getName())
                .toList();
    }

    public String highestRiskLevel(List<String> permissions) {
        int maxRisk = describe(permissions).stream()
                .mapToInt(descriptor -> riskRank(descriptor.getRiskLevel()))
                .max()
                .orElse(0);

        return switch (maxRisk) {
            case 3 -> HIGH;
            case 2 -> MEDIUM;
            case 1 -> LOW;
            default -> LOW;
        };
    }

    private PluginPermissionDescriptor describe(String permission) {
        PluginPermissionDescriptor known = descriptors.get(permission);
        if (known != null) {
            return known;
        }
        return new PluginPermissionDescriptor(
                permission,
                permission,
                "Unknown permission declaration.",
                UNKNOWN,
                false
        );
    }

    private void register(String name, String displayName, String description, String riskLevel) {
        descriptors.put(name, new PluginPermissionDescriptor(name, displayName, description, riskLevel, true));
    }

    private int riskRank(String riskLevel) {
        if (HIGH.equals(riskLevel) || UNKNOWN.equals(riskLevel)) {
            return 3;
        }
        if (MEDIUM.equals(riskLevel)) {
            return 2;
        }
        if (LOW.equals(riskLevel)) {
            return 1;
        }
        return 0;
    }
}
