CREATE TABLE conversation_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(128) NOT NULL,
    resource_type VARCHAR(64) NOT NULL,
    resource_id VARCHAR(128) NOT NULL,
    purpose VARCHAR(64) NULL,
    display_name VARCHAR(255) NULL,
    mime_type VARCHAR(255) NULL,
    size_bytes BIGINT NULL,
    metadata_json LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_conversation_resource_conversation_id (conversation_id),
    INDEX idx_conversation_resource_type (conversation_id, resource_type),
    INDEX idx_conversation_resource_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
