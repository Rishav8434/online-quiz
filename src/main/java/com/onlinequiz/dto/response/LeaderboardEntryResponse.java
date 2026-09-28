package com.onlinequiz.dto.response;

import java.time.LocalDateTime;

public class LeaderboardEntryResponse {

    private int rank;
    private Long attemptId;
    private String username;
    private String fullName;
    private Double score;
    private Double percentage;
    private Long durationTakenSeconds;
    private LocalDateTime attemptedAt;

    public LeaderboardEntryResponse() {}

    public LeaderboardEntryResponse(int rank, Long attemptId, String username, String fullName,
                                    Double score, Double percentage, Long durationTakenSeconds,
                                    LocalDateTime attemptedAt) {
        this.rank = rank;
        this.attemptId = attemptId;
        this.username = username;
        this.fullName = fullName;
        this.score = score;
        this.percentage = percentage;
        this.durationTakenSeconds = durationTakenSeconds;
        this.attemptedAt = attemptedAt;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public Long getDurationTakenSeconds() { return durationTakenSeconds; }
    public void setDurationTakenSeconds(Long durationTakenSeconds) { this.durationTakenSeconds = durationTakenSeconds; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
}
