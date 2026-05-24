CREATE TABLE mcp_external_server (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    base_url VARCHAR(1000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    enabled TINYINT(1) NOT NULL,
    error_message TEXT NULL,
    last_synced_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_mcp_external_server_server_id (server_id),
    UNIQUE KEY uk_mcp_external_server_name (name),
    KEY idx_mcp_external_server_status (status),
    KEY idx_mcp_external_server_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mcp_external_tool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id VARCHAR(64) NOT NULL,
    server_id VARCHAR(64) NOT NULL,
    remote_name VARCHAR(255) NOT NULL,
    local_name VARCHAR(500) NOT NULL,
    description TEXT NULL,
    input_schema_json LONGTEXT NULL,
    enabled TINYINT(1) NOT NULL,
    last_synced_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_mcp_external_tool_tool_id (tool_id),
    KEY idx_mcp_external_tool_server_id (server_id),
    KEY idx_mcp_external_tool_local_name (local_name),
    KEY idx_mcp_external_tool_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
