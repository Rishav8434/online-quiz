package com.onlinequiz.service;

import com.onlinequiz.entity.Option;
import com.onlinequiz.entity.Question;
import com.onlinequiz.entity.Quiz;
import com.onlinequiz.entity.User;
import com.onlinequiz.service.impl.ScoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScoringServiceTest {

    private ScoringService scoringService;
    private Quiz quiz;
    private Question q1;
    private Question q2;
    private Option q1Opt1;
    private Option q1Opt2;
    private Option q2Opt1;
    private Option q2Opt2;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringServiceImpl();

        User user = new User("admin", "admin@quiz.com", "pass", "Admin");
        quiz = new Quiz("Java Advanced", "Core Java Assessment", 20, user);
        quiz.setPassPercentage(50.0);

        q1 = new Question("What is Virtual Thread?", 4.0, 1.0, quiz);
        q1.setId(1L);
        q1Opt1 = new Option("Lightweight thread", true, q1);
        q1Opt1.setId(10L);
        q1Opt2 = new Option("OS thread", false, q1);
        q1Opt2.setId(11L);
        q1.setOptions(List.of(q1Opt1, q1Opt2));

        q2 = new Question("What is Sealed Class?", 4.0, 1.0, quiz);
        q2.setId(2L);
        q2Opt1 = new Option("Restricts subclassing", true, q2);
        q2Opt1.setId(20L);
        q2Opt2 = new Option("Final class only", false, q2);
        q2Opt2.setId(21L);
        q2.setOptions(List.of(q2Opt1, q2Opt2));

        quiz.setQuestions(List.of(q1, q2));
        quiz.recalculateTotalMarks();
    }

    @Test
    @DisplayName("Should evaluate all correct answers accurately with 100% score")
    void testAllCorrectAnswers() {
        Map<Long, Long> answers = Map.of(1L, 10L, 2L, 20L);

        ScoringService.ScoringResult result = scoringService.evaluateQuiz(quiz, List.of(q1, q2), answers);

        assertEquals(8.0, result.getMarksObtained());
        assertEquals(8.0, result.getMaxMarks());
        assertEquals(100.0, result.getPercentage());
        assertEquals(2, result.getCorrectCount());
        assertEquals(0, result.getWrongCount());
        assertEquals(0, result.getUnansweredCount());
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Should apply negative marking when enabled on quiz and answer is wrong")
    void testNegativeMarking() {
        quiz.setNegativeMarking(true);
        quiz.setNegativeMarks(1.0);

        // Q1 correct (+4), Q2 wrong (-1) -> Total 3.0 out of 8.0
        Map<Long, Long> answers = Map.of(1L, 10L, 2L, 21L);

        ScoringService.ScoringResult result = scoringService.evaluateQuiz(quiz, List.of(q1, q2), answers);

        assertEquals(3.0, result.getMarksObtained());
        assertEquals(8.0, result.getMaxMarks());
        assertEquals(37.5, result.getPercentage());
        assertEquals(1, result.getCorrectCount());
        assertEquals(1, result.getWrongCount());
        assertEquals(0, result.getUnansweredCount());
        assertFalse(result.isPassed()); // 37.5% < 50.0%
    }

    @Test
    @DisplayName("Should floor total score at 0.0 when negative deductions exceed correct marks")
    void testScoreFlooredAtZero() {
        quiz.setNegativeMarking(true);
        quiz.setNegativeMarks(5.0); // Extreme negative penalty

        // Q1 wrong (-5.0), Q2 unanswered (0.0) -> Score floored to 0.0
        Map<Long, Long> answers = Map.of(1L, 11L);

        ScoringService.ScoringResult result = scoringService.evaluateQuiz(quiz, List.of(q1, q2), answers);

        assertEquals(0.0, result.getMarksObtained());
        assertEquals(0.0, result.getPercentage());
        assertEquals(0, result.getCorrectCount());
        assertEquals(1, result.getWrongCount());
        assertEquals(1, result.getUnansweredCount());
        assertFalse(result.isPassed());
    }
}
