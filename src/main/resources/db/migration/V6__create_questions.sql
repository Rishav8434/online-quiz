CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text TEXT NOT NULL,
    quiz_id BIGINT NOT NULL,
    marks DOUBLE NOT NULL DEFAULT 1.0,
    negative_marks DOUBLE NOT NULL DEFAULT 0.0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (id) ON DELETE CASCADE
);

CREATE INDEX idx_questions_quiz_id ON questions (quiz_id);
CREATE INDEX idx_questions_is_deleted ON questions (is_deleted);
