CREATE TABLE plugin_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plugin_id VARCHAR(64) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    jar_path VARCHAR(1000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    error_message TEXT NULL,
    uploaded_at DATETIME NULL,
    loaded_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_plugin_package_plugin_id (plugin_id),
    KEY idx_plugin_package_status (status),
    KEY idx_plugin_package_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plugin_skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plugin_id VARCHAR(64) NOT NULL,
    skill_name VARCHAR(128) NOT NULL,
    display_name VARCHAR(255) NULL,
    description TEXT NULL,
    version VARCHAR(64) NULL,
    class_name VARCHAR(500) NOT NULL,
    enabled TINYINT(1) NOT NULL,
    metadata_json LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    KEY idx_plugin_skill_plugin_id (plugin_id),
    KEY idx_plugin_skill_skill_name (skill_name),
    KEY idx_plugin_skill_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
