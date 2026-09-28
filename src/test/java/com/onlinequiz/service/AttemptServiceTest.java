package com.onlinequiz.service;

import com.onlinequiz.dto.request.SubmitAnswerRequest;
import com.onlinequiz.dto.request.SubmitQuizRequest;
import com.onlinequiz.dto.response.AttemptResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.entity.*;
import com.onlinequiz.enums.AttemptStatus;
import com.onlinequiz.mapper.AttemptMapper;
import com.onlinequiz.repository.OptionRepository;
import com.onlinequiz.repository.QuestionRepository;
import com.onlinequiz.repository.QuizAttemptRepository;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.service.impl.AttemptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock
    private QuizAttemptRepository attemptRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private ScoringService scoringService;

    @Mock
    private AttemptMapper attemptMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AttemptServiceImpl attemptService;

    private User user;
    private Quiz quiz;
    private Question question;
    private Option optionCorrect;
    private Option optionWrong;
    private QuizAttempt attempt;

    @BeforeEach
    void setUp() {
        user = new User("alice", "alice@example.com", "pass", "Alice");
        user.setId(10L);

        quiz = new Quiz("Spring Boot", "Test description", 15, user);
        quiz.setId(20L);
        quiz.setStatus(com.onlinequiz.enums.QuizStatus.PUBLISHED);

        question = new Question("What is Spring Boot?", quiz);
        question.setId(30L);

        optionCorrect = new Option("Framework", true, question);
        optionCorrect.setId(40L);
        optionWrong = new Option("Language", false, question);
        optionWrong.setId(41L);

        question.setOptions(List.of(optionCorrect, optionWrong));
        quiz.setQuestions(List.of(question));

        attempt = new QuizAttempt(quiz, user, LocalDateTime.now());
        attempt.setId(100L);
    }

    @Test
    @DisplayName("Should successfully start an attempt and return remaining time")
    void testStartAttempt() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(quizRepository.findByIdWithQuestions(20L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.findFirstByUserIdAndQuizIdAndStatusOrderByStartTimeDesc(10L, 20L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(attemptRepository.save(any(QuizAttempt.class))).thenReturn(attempt);

        AttemptResponse mockResponse = new AttemptResponse(100L, 20L, "Spring Boot", 15, LocalDateTime.now(), 900L, AttemptStatus.IN_PROGRESS, List.of());
        when(attemptMapper.toAttemptResponse(eq(attempt), eq(900L))).thenReturn(mockResponse);

        AttemptResponse result = attemptService.startAttempt(20L, 10L);

        assertNotNull(result);
        assertEquals(100L, result.getAttemptId());
        verify(attemptRepository, times(1)).save(any(QuizAttempt.class));
    }

    @Test
    @DisplayName("Should correctly evaluate score upon submission and calculate percentage")
    void testSubmitAttempt() {
        when(attemptRepository.findByIdWithAnswers(100L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdWithOptionsAndIsDeletedFalse(20L)).thenReturn(List.of(question));

        ScoringService.ScoringResult scoringResult = new ScoringService.ScoringResult(
                1.0, 1.0, 100.0, 1, 1, 1, 0, 0, true,
                List.of(new ScoringService.EvaluatedAnswer(question, optionCorrect, true, 1.0))
        );
        when(scoringService.evaluateQuiz(eq(quiz), any(), any())).thenReturn(scoringResult);
        when(attemptRepository.save(any(QuizAttempt.class))).thenReturn(attempt);

        AttemptResultResponse mockResult = new AttemptResultResponse();
        mockResult.setAttemptId(100L);
        mockResult.setScore(1.0);
        mockResult.setPercentage(100.0);
        when(attemptMapper.toResultResponse(any(QuizAttempt.class))).thenReturn(mockResult);

        SubmitAnswerRequest answer = new SubmitAnswerRequest(30L, 40L);
        SubmitQuizRequest request = new SubmitQuizRequest(List.of(answer));

        AttemptResultResponse result = attemptService.submitAttempt(100L, request, 10L);

        assertNotNull(result);
        assertEquals(1.0, result.getScore());
        assertEquals(100.0, result.getPercentage());
        verify(attemptRepository, times(1)).save(any(QuizAttempt.class));
    }
}
