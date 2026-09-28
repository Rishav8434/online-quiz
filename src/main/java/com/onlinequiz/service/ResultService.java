package com.onlinequiz.service;

import com.onlinequiz.dto.response.AdminDashboardStatsResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.dto.response.LeaderboardEntryResponse;
import com.onlinequiz.dto.response.QuizAnalyticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResultService {
    List<LeaderboardEntryResponse> getLeaderboard(Long quizId);
    QuizAnalyticsResponse getQuizAnalytics(Long quizId);
    AdminDashboardStatsResponse getAdminDashboardStats();
    byte[] exportQuizResultsCsv(Long quizId);
    byte[] exportAllResultsCsv();
    Page<AttemptResultResponse> getAllAttemptsForAdmin(Pageable pageable);
}
