CREATE TABLE resume_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id VARCHAR(64) NOT NULL UNIQUE,
    original_file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    parsed_text LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE job_posting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_url VARCHAR(1000) NOT NULL,
    page_title VARCHAR(500) NULL,
    raw_text LONGTEXT NULL,
    requirement_summary LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_job_posting_job_url (job_url(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE resume_analysis_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id VARCHAR(128) NULL,
    model_id VARCHAR(128) NULL,
    resume_file_id BIGINT NOT NULL,
    job_posting_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    error_message LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_resume_analysis_task_resume_file_id (resume_file_id),
    INDEX idx_resume_analysis_task_job_posting_id (job_posting_id),
    INDEX idx_resume_analysis_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE resume_optimization_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    job_requirement_summary LONGTEXT NULL,
    match_analysis LONGTEXT NULL,
    optimization_suggestions LONGTEXT NULL,
    optimized_resume LONGTEXT NULL,
    interview_suggestions LONGTEXT NULL,
    raw_model_response LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_resume_optimization_result_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
