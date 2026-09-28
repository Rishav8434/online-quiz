package com.onlinequiz.dto.response;

import com.onlinequiz.enums.QuizStatus;
import java.time.LocalDateTime;

public class QuizSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private Integer totalQuestions;
    private Double totalMarks;
    private QuizStatus status;
    private boolean negativeMarking;
    private Double passPercentage;
    private String createdBy;
    private LocalDateTime createdAt;

    public QuizSummaryResponse() {}

    public QuizSummaryResponse(Long id, String title, String description, Integer durationMinutes,
                               Integer totalQuestions, Double totalMarks, QuizStatus status,
                               boolean negativeMarking, Double passPercentage,
                               String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.totalQuestions = totalQuestions;
        this.totalMarks = totalMarks;
        this.status = status;
        this.negativeMarking = negativeMarking;
        this.passPercentage = passPercentage;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public boolean isPublished() {
        return this.status == QuizStatus.PUBLISHED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Double totalMarks) { this.totalMarks = totalMarks; }
    public QuizStatus getStatus() { return status; }
    public void setStatus(QuizStatus status) { this.status = status; }
    public boolean isNegativeMarking() { return negativeMarking; }
    public void setNegativeMarking(boolean negativeMarking) { this.negativeMarking = negativeMarking; }
    public Double getPassPercentage() { return passPercentage; }
    public void setPassPercentage(Double passPercentage) { this.passPercentage = passPercentage; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
