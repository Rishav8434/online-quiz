package com.onlinequiz.dto.response;

import java.util.ArrayList;
import java.util.List;

public class QuizAnalyticsResponse {

    private Long quizId;
    private String quizTitle;
    private long totalAttempts;
    private Double highestScore;
    private Double lowestScore;
    private Double averageScore;
    private Double averagePercentage;
    private Double passPercentage;
    private List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();

    public QuizAnalyticsResponse() {}

    public QuizAnalyticsResponse(Long quizId, String quizTitle, long totalAttempts,
                                 Double highestScore, Double lowestScore, Double averageScore,
                                 Double averagePercentage, Double passPercentage,
                                 List<LeaderboardEntryResponse> leaderboard) {
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.totalAttempts = totalAttempts;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.averageScore = averageScore;
        this.averagePercentage = averagePercentage;
        this.passPercentage = passPercentage;
        this.leaderboard = leaderboard;
    }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public long getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(long totalAttempts) { this.totalAttempts = totalAttempts; }

    public Double getHighestScore() { return highestScore; }
    public void setHighestScore(Double highestScore) { this.highestScore = highestScore; }

    public Double getLowestScore() { return lowestScore; }
    public void setLowestScore(Double lowestScore) { this.lowestScore = lowestScore; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public Double getAveragePercentage() { return averagePercentage; }
    public void setAveragePercentage(Double averagePercentage) { this.averagePercentage = averagePercentage; }

    public Double getPassPercentage() { return passPercentage; }
    public void setPassPercentage(Double passPercentage) { this.passPercentage = passPercentage; }

    public List<LeaderboardEntryResponse> getLeaderboard() { return leaderboard; }
    public void setLeaderboard(List<LeaderboardEntryResponse> leaderboard) { this.leaderboard = leaderboard; }
}
