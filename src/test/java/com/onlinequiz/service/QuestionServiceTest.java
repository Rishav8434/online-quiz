package com.onlinequiz.service;

import com.onlinequiz.dto.request.CreateOptionRequest;
import com.onlinequiz.dto.request.CreateQuestionRequest;
import com.onlinequiz.dto.response.QuestionResponse;
import com.onlinequiz.entity.Option;
import com.onlinequiz.entity.Question;
import com.onlinequiz.entity.Quiz;
import com.onlinequiz.entity.User;
import com.onlinequiz.exception.BadRequestException;
import com.onlinequiz.mapper.QuizMapper;
import com.onlinequiz.repository.OptionRepository;
import com.onlinequiz.repository.QuestionRepository;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.service.impl.QuestionServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private QuizMapper quizMapper;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Quiz quiz;
    private Question question;

    @BeforeEach
    void setUp() {
        User user = new User("admin", "admin@quiz.com", "pass", "Admin");
        quiz = new Quiz("Java", "Desc", 10, user);
        quiz.setId(1L);

        question = new Question("Question 1", 2.0, 0.5, quiz);
        question.setId(10L);
    }

    @Test
    @DisplayName("Should successfully add a valid MCQ question to quiz")
    void testAddQuestionSuccess() {
        CreateOptionRequest opt1 = new CreateOptionRequest("A", true);
        CreateOptionRequest opt2 = new CreateOptionRequest("B", false);
        CreateQuestionRequest request = new CreateQuestionRequest("What is A?", 2.0, 0.5, List.of(opt1, opt2));

        when(quizRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(quiz));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(quizMapper.toQuestionResponse(question, true)).thenReturn(new QuestionResponse(10L, "What is A?", 2.0, 0.5, List.of()));

        QuestionResponse response = questionService.addQuestionToQuiz(1L, request, 99L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    @DisplayName("Should reject question with multiple correct options in standard MCQ mode")
    void testAddQuestionMultipleCorrectOptions() {
        CreateOptionRequest opt1 = new CreateOptionRequest("A", true);
        CreateOptionRequest opt2 = new CreateOptionRequest("B", true);
        CreateQuestionRequest request = new CreateQuestionRequest("Invalid Q", 2.0, 0.5, List.of(opt1, opt2));

        when(quizRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(quiz));

        assertThrows(BadRequestException.class, () -> questionService.addQuestionToQuiz(1L, request, 99L));
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    @DisplayName("Should soft-delete a question successfully")
    void testDeleteQuestion() {
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        questionService.deleteQuestion(10L, 99L);

        assertTrue(question.isDeleted());
        assertNotNull(question.getDeletedAt());
        verify(questionRepository, times(1)).save(question);
    }
}
