CREATE TABLE skill_setting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_name VARCHAR(128) NOT NULL,
    enabled TINYINT(1) NOT NULL,
    source_type VARCHAR(32) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_skill_setting_skill_name (skill_name),
    KEY idx_skill_setting_enabled (enabled),
    KEY idx_skill_setting_source_type (source_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
