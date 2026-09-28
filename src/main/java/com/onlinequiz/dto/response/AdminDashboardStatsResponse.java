package com.onlinequiz.dto.response;

public class AdminDashboardStatsResponse {

    private long totalUsers;
    private long totalQuizzes;
    private long publishedQuizzes;
    private long draftQuizzes;
    private long totalAttempts;
    private Double overallAverageScore;
    private Double overallAveragePercentage;
    private Double overallPassPercentage;

    public AdminDashboardStatsResponse() {}

    public AdminDashboardStatsResponse(long totalUsers, long totalQuizzes, long publishedQuizzes,
                                       long draftQuizzes, long totalAttempts, Double overallAverageScore,
                                       Double overallAveragePercentage, Double overallPassPercentage) {
        this.totalUsers = totalUsers;
        this.totalQuizzes = totalQuizzes;
        this.publishedQuizzes = publishedQuizzes;
        this.draftQuizzes = draftQuizzes;
        this.totalAttempts = totalAttempts;
        this.overallAverageScore = overallAverageScore;
        this.overallAveragePercentage = overallAveragePercentage;
        this.overallPassPercentage = overallPassPercentage;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(long totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public long getPublishedQuizzes() { return publishedQuizzes; }
    public void setPublishedQuizzes(long publishedQuizzes) { this.publishedQuizzes = publishedQuizzes; }

    public long getDraftQuizzes() { return draftQuizzes; }
    public void setDraftQuizzes(long draftQuizzes) { this.draftQuizzes = draftQuizzes; }

    public long getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(long totalAttempts) { this.totalAttempts = totalAttempts; }

    public Double getOverallAverageScore() { return overallAverageScore; }
    public void setOverallAverageScore(Double overallAverageScore) { this.overallAverageScore = overallAverageScore; }

    public Double getOverallAveragePercentage() { return overallAveragePercentage; }
    public void setOverallAveragePercentage(Double overallAveragePercentage) { this.overallAveragePercentage = overallAveragePercentage; }

    public Double getOverallPassPercentage() { return overallPassPercentage; }
    public void setOverallPassPercentage(Double overallPassPercentage) { this.overallPassPercentage = overallPassPercentage; }
}
