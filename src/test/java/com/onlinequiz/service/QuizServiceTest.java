package com.onlinequiz.service;

import com.onlinequiz.dto.request.CreateOptionRequest;
import com.onlinequiz.dto.request.CreateQuestionRequest;
import com.onlinequiz.dto.request.CreateQuizRequest;
import com.onlinequiz.dto.response.QuizResponse;
import com.onlinequiz.dto.response.QuizSummaryResponse;
import com.onlinequiz.entity.Quiz;
import com.onlinequiz.entity.User;
import com.onlinequiz.enums.QuizStatus;
import com.onlinequiz.exception.BadRequestException;
import com.onlinequiz.mapper.QuizMapper;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.service.impl.QuizServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizMapper quizMapper;

    @InjectMocks
    private QuizServiceImpl quizService;

    private User adminUser;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", "admin@example.com", "pass", "Administrator");
        adminUser.setId(1L);

        quiz = new Quiz("Java Quiz", "A test quiz", 10, adminUser);
        quiz.setId(100L);
    }

    @Test
    @DisplayName("Should create quiz successfully when valid questions and correct options are provided")
    void testCreateQuizSuccess() {
        CreateOptionRequest opt1 = new CreateOptionRequest("Option 1", true);
        CreateOptionRequest opt2 = new CreateOptionRequest("Option 2", false);
        CreateQuestionRequest q1 = new CreateQuestionRequest("Question 1", List.of(opt1, opt2));
        CreateQuizRequest request = new CreateQuizRequest("Java Quiz", "A test quiz", 10, List.of(q1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(quizMapper.toEntity(eq(request), eq(adminUser))).thenReturn(quiz);
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);

        QuizResponse response = new QuizResponse(100L, "Java Quiz", "A test quiz", 10, QuizStatus.DRAFT, 10.0, false, 0.0, 50.0, "Administrator", null, 1, List.of());
        when(quizMapper.toResponse(quiz, true)).thenReturn(response);

        QuizResponse result = quizService.createQuiz(request, 1L);

        assertNotNull(result);
        assertEquals("Java Quiz", result.getTitle());
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when question lacks a correct option")
    void testCreateQuizWithoutCorrectAnswer() {
        CreateOptionRequest opt1 = new CreateOptionRequest("Option 1", false);
        CreateOptionRequest opt2 = new CreateOptionRequest("Option 2", false);
        CreateQuestionRequest q1 = new CreateQuestionRequest("Question 1", List.of(opt1, opt2));
        CreateQuizRequest request = new CreateQuizRequest("Java Quiz", "A test quiz", 10, List.of(q1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(BadRequestException.class, () -> quizService.createQuiz(request, 1L));
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should return list of published quizzes")
    void testGetAllPublishedQuizzes() {
        when(quizRepository.findByStatusAndIsDeletedFalse(QuizStatus.PUBLISHED)).thenReturn(List.of(quiz));
        QuizSummaryResponse summary = new QuizSummaryResponse(100L, "Java Quiz", "A test quiz", 10, 0, 10.0, QuizStatus.PUBLISHED, false, 50.0, "Admin", null);
        when(quizMapper.toSummaryResponse(quiz)).thenReturn(summary);

        List<QuizSummaryResponse> result = quizService.getAllPublishedQuizzes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Quiz", result.get(0).getTitle());
    }
}
