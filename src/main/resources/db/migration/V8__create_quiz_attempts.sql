CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    expected_end_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NULL DEFAULT NULL,
    score DOUBLE NOT NULL DEFAULT 0.0,
    max_score DOUBLE NOT NULL DEFAULT 0.0,
    percentage DOUBLE NOT NULL DEFAULT 0.0,
    total_questions INT NOT NULL DEFAULT 0,
    attempted_questions INT NOT NULL DEFAULT 0,
    correct_answers INT NOT NULL DEFAULT 0,
    wrong_answers INT NOT NULL DEFAULT 0,
    unanswered_questions INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    passed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (id) ON DELETE CASCADE,
    CONSTRAINT fk_attempts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_attempts_quiz_id ON quiz_attempts (quiz_id);
CREATE INDEX idx_attempts_user_id ON quiz_attempts (user_id);
CREATE INDEX idx_attempts_status ON quiz_attempts (status);
