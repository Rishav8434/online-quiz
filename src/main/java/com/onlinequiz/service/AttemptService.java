package com.onlinequiz.service;

import com.onlinequiz.dto.request.SubmitQuizRequest;
import com.onlinequiz.dto.response.AttemptResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AttemptService {
    AttemptResponse startAttempt(Long quizId, Long userId);
    AttemptResultResponse submitAttempt(Long attemptId, SubmitQuizRequest request, Long userId);
    AttemptResultResponse getAttemptResult(Long attemptId, Long userId);
    List<AttemptResultResponse> getUserAttemptHistory(Long userId);
    Page<AttemptResultResponse> getUserAttemptHistory(Long userId, Pageable pageable);
}
