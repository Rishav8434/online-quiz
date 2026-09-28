package com.onlinequiz.service;

import com.onlinequiz.dto.request.CreateQuestionRequest;
import com.onlinequiz.dto.request.UpdateQuestionRequest;
import com.onlinequiz.dto.response.QuestionResponse;

public interface QuestionService {
    QuestionResponse addQuestionToQuiz(Long quizId, CreateQuestionRequest request, Long userId);
    QuestionResponse updateQuestion(Long questionId, UpdateQuestionRequest request, Long userId);
    void deleteQuestion(Long questionId, Long userId);
    QuestionResponse getQuestionById(Long questionId);
}
