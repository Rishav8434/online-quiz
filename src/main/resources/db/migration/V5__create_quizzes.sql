CREATE TABLE IF NOT EXISTS quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_marks DOUBLE NOT NULL DEFAULT 0.0,
    negative_marking BOOLEAN NOT NULL DEFAULT FALSE,
    negative_marks DOUBLE NOT NULL DEFAULT 0.0,
    pass_percentage DOUBLE NOT NULL DEFAULT 50.0,
    created_by_id BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quizzes_created_by FOREIGN KEY (created_by_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_quizzes_status ON quizzes (status);
CREATE INDEX idx_quizzes_created_by ON quizzes (created_by_id);
CREATE INDEX idx_quizzes_is_deleted ON quizzes (is_deleted);
