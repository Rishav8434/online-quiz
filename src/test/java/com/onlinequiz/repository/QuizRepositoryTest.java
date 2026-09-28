package com.onlinequiz.repository;

import com.onlinequiz.entity.Quiz;
import com.onlinequiz.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuizRepositoryTest {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find published quizzes correctly")
    void testFindByPublishedTrue() {
        User creator = new User("admin_test", "admin_test@example.com", "secret", "Admin Test");
        userRepository.save(creator);

        Quiz q1 = new Quiz("Quiz 1", "Desc 1", 10, creator);
        q1.setStatus(com.onlinequiz.enums.QuizStatus.PUBLISHED);
        quizRepository.save(q1);

        Quiz q2 = new Quiz("Quiz 2", "Desc 2", 15, creator);
        q2.setStatus(com.onlinequiz.enums.QuizStatus.DRAFT);
        quizRepository.save(q2);

        List<Quiz> published = quizRepository.findByStatusAndIsDeletedFalse(com.onlinequiz.enums.QuizStatus.PUBLISHED);

        assertEquals(1, published.size());
        assertEquals("Quiz 1", published.get(0).getTitle());
    }
}
