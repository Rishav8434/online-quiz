CREATE TABLE IF NOT EXISTS user_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT NULL DEFAULT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    marks_awarded DOUBLE NOT NULL DEFAULT 0.0,
    CONSTRAINT fk_user_answers_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_answers_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_answers_option FOREIGN KEY (selected_option_id) REFERENCES options (id) ON DELETE SET NULL
);

CREATE INDEX idx_user_answers_attempt_id ON user_answers (attempt_id);
CREATE INDEX idx_user_answers_question_id ON user_answers (question_id);
