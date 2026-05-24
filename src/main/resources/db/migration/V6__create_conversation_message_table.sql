CREATE TABLE conversation_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    content LONGTEXT NOT NULL,
    model_id VARCHAR(128) NULL,
    metadata_json LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_conversation_message_conversation_id (conversation_id),
    KEY idx_conversation_message_created_at (created_at),
    KEY idx_conversation_message_conversation_created (conversation_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
