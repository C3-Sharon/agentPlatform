CREATE TABLE agent_run (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL,
    conversation_id VARCHAR(128) NOT NULL,
    model_id VARCHAR(128) NULL,
    user_message TEXT NULL,
    answer LONGTEXT NULL,
    used_model VARCHAR(128) NULL,
    used_skills_json TEXT NULL,
    status VARCHAR(32) NOT NULL,
    error_message TEXT NULL,
    started_at DATETIME NULL,
    finished_at DATETIME NULL,
    duration_ms BIGINT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_agent_run_run_id (run_id),
    INDEX idx_agent_run_conversation_id (conversation_id),
    INDEX idx_agent_run_status (status),
    INDEX idx_agent_run_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE agent_run_trace (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL,
    step_order INT NOT NULL,
    step VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    detail VARCHAR(1000) NULL,
    data_json LONGTEXT NULL,
    duration_ms BIGINT NULL,
    trace_timestamp DATETIME NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_agent_run_trace_run_id (run_id),
    INDEX idx_agent_run_trace_step (step),
    INDEX idx_agent_run_trace_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
