package com.onlinequiz.service;

import com.onlinequiz.dto.request.CreateQuizRequest;
import com.onlinequiz.dto.request.UpdateQuizRequest;
import com.onlinequiz.dto.response.QuizResponse;
import com.onlinequiz.dto.response.QuizSummaryResponse;
import com.onlinequiz.enums.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizService {
    QuizResponse createQuiz(CreateQuizRequest request, Long userId);
    List<QuizSummaryResponse> getAllPublishedQuizzes();
    Page<QuizSummaryResponse> getPublishedQuizzes(String search, Pageable pageable);
    List<QuizSummaryResponse> getAllQuizzesForAdmin();
    Page<QuizSummaryResponse> getAllQuizzesForAdmin(QuizStatus status, String search, Pageable pageable);
    QuizResponse getQuizById(Long id, boolean isAdmin);
    QuizResponse updateQuiz(Long id, UpdateQuizRequest request, Long userId);
    void deleteQuiz(Long id, Long userId);
    QuizResponse publishQuiz(Long id, Long userId);
    QuizResponse unpublishQuiz(Long id, Long userId);
}
