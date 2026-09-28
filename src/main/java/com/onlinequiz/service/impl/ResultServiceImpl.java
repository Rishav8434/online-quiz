package com.onlinequiz.service.impl;

import com.onlinequiz.dto.response.AdminDashboardStatsResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.dto.response.LeaderboardEntryResponse;
import com.onlinequiz.dto.response.QuizAnalyticsResponse;
import com.onlinequiz.entity.Quiz;
import com.onlinequiz.entity.QuizAttempt;
import com.onlinequiz.enums.AttemptStatus;
import com.onlinequiz.enums.QuizStatus;
import com.onlinequiz.exception.ResourceNotFoundException;
import com.onlinequiz.mapper.AttemptMapper;
import com.onlinequiz.repository.QuizAttemptRepository;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.service.ResultService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ResultServiceImpl implements ResultService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final QuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final AttemptMapper attemptMapper;

    public ResultServiceImpl(QuizAttemptRepository attemptRepository,
                             QuizRepository quizRepository,
                             UserRepository userRepository,
                             AttemptMapper attemptMapper) {
        this.attemptRepository = attemptRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.attemptMapper = attemptMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));

        List<QuizAttempt> attempts = attemptRepository.findLeaderboardByQuizId(quiz.getId());
        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();

        for (int i = 0; i < attempts.size(); i++) {
            leaderboard.add(attemptMapper.toLeaderboardEntry(attempts.get(i), i + 1));
        }

        return leaderboard;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAnalyticsResponse getQuizAnalytics(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));

        long totalAttempts = attemptRepository.countAttemptsByQuizId(quizId);
        Double highestScore = attemptRepository.findHighestScoreByQuizId(quizId);
        Double lowestScore = attemptRepository.findLowestScoreByQuizId(quizId);
        Double averageScore = attemptRepository.findAverageScoreByQuizId(quizId);
        Double averagePercentage = attemptRepository.findAveragePercentageByQuizId(quizId);

        List<QuizAttempt> completedAttempts = attemptRepository.findByQuizIdAndStatusIn(
                quizId,
                Set.of(AttemptStatus.SUBMITTED, AttemptStatus.AUTO_SUBMITTED)
        );

        long passedCount = completedAttempts.stream().filter(QuizAttempt::isPassed).count();
        Double passPercentage = !completedAttempts.isEmpty()
                ? Math.round(((double) passedCount / completedAttempts.size()) * 10000.0) / 100.0
                : 0.0;

        List<LeaderboardEntryResponse> leaderboard = getLeaderboard(quizId);

        return new QuizAnalyticsResponse(
                quiz.getId(),
                quiz.getTitle(),
                totalAttempts,
                highestScore != null ? Math.round(highestScore * 100.0) / 100.0 : 0.0,
                lowestScore != null ? Math.round(lowestScore * 100.0) / 100.0 : 0.0,
                averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0,
                averagePercentage != null ? Math.round(averagePercentage * 100.0) / 100.0 : 0.0,
                passPercentage,
                leaderboard
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getAdminDashboardStats() {
        long totalUsers = userRepository.count();
        long totalQuizzes = quizRepository.countByIsDeletedFalse();
        long publishedQuizzes = quizRepository.countByStatusAndIsDeletedFalse(QuizStatus.PUBLISHED);
        long draftQuizzes = quizRepository.countByStatusAndIsDeletedFalse(QuizStatus.DRAFT);
        long totalAttempts = attemptRepository.countSubmittedAttempts();

        Double avgScore = attemptRepository.findOverallAverageScore();
        Double avgPercentage = attemptRepository.findOverallAveragePercentage();

        Double overallAvgScore = avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0;
        Double overallAvgPercentage = avgPercentage != null ? Math.round(avgPercentage * 100.0) / 100.0 : 0.0;

        long passedCount = attemptRepository.countPassedAttempts();
        Double passPercentage = totalAttempts > 0
                ? Math.round(((double) passedCount / totalAttempts) * 10000.0) / 100.0
                : 0.0;

        return new AdminDashboardStatsResponse(
                totalUsers,
                totalQuizzes,
                publishedQuizzes,
                draftQuizzes,
                totalAttempts,
                overallAvgScore,
                overallAvgPercentage,
                passPercentage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportQuizResultsCsv(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));

        List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndStatusIn(
                quiz.getId(),
                Set.of(AttemptStatus.SUBMITTED, AttemptStatus.AUTO_SUBMITTED)
        );

        return buildCsv(attempts);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAllResultsCsv() {
        List<QuizAttempt> attempts = attemptRepository.findAll();
        return buildCsv(attempts);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttemptResultResponse> getAllAttemptsForAdmin(Pageable pageable) {
        return attemptRepository.findAllByOrderByStartTimeDesc(pageable)
                .map(attemptMapper::toResultResponse);
    }

    private byte[] buildCsv(List<QuizAttempt> attempts) {
        StringBuilder csv = new StringBuilder();
        csv.append("Attempt ID,Quiz Title,Username,Full Name,Score,Max Score,Percentage,Total Questions,Attempted,Correct,Wrong,Unanswered,Passed,Status,Start Time,End Time,Duration (Seconds)\n");

        for (QuizAttempt a : attempts) {
            long duration = 0;
            if (a.getStartTime() != null && a.getEndTime() != null) {
                duration = Duration.between(a.getStartTime(), a.getEndTime()).getSeconds();
            }

            String startTimeStr = a.getStartTime() != null ? a.getStartTime().format(DATE_FORMATTER) : "";
            String endTimeStr = a.getEndTime() != null ? a.getEndTime().format(DATE_FORMATTER) : "";

            csv.append(escapeCsv(String.valueOf(a.getId()))).append(",")
               .append(escapeCsv(a.getQuiz() != null ? a.getQuiz().getTitle() : "")).append(",")
               .append(escapeCsv(a.getUser() != null ? a.getUser().getUsername() : "")).append(",")
               .append(escapeCsv(a.getUser() != null ? a.getUser().getFullName() : "")).append(",")
               .append(a.getScore() != null ? a.getScore() : 0.0).append(",")
               .append(a.getMaxScore() != null ? a.getMaxScore() : 0.0).append(",")
               .append(a.getPercentage() != null ? a.getPercentage() : 0.0).append(",")
               .append(a.getTotalQuestions() != null ? a.getTotalQuestions() : 0).append(",")
               .append(a.getAttemptedQuestions() != null ? a.getAttemptedQuestions() : 0).append(",")
               .append(a.getCorrectAnswers() != null ? a.getCorrectAnswers() : 0).append(",")
               .append(a.getWrongAnswers() != null ? a.getWrongAnswers() : 0).append(",")
               .append(a.getUnansweredQuestions() != null ? a.getUnansweredQuestions() : 0).append(",")
               .append(a.isPassed()).append(",")
               .append(a.getStatus()).append(",")
               .append(escapeCsv(startTimeStr)).append(",")
               .append(escapeCsv(endTimeStr)).append(",")
               .append(duration).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
