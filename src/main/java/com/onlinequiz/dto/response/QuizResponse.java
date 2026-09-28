package com.onlinequiz.dto.response;

import com.onlinequiz.enums.QuizStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private QuizStatus status;
    private Double totalMarks;
    private boolean negativeMarking;
    private Double negativeMarks;
    private Double passPercentage;
    private String createdBy;
    private LocalDateTime createdAt;
    private Integer totalQuestions;
    private List<QuestionResponse> questions = new ArrayList<>();

    public QuizResponse() {}

    public QuizResponse(Long id, String title, String description, Integer durationMinutes,
                        QuizStatus status, Double totalMarks, boolean negativeMarking,
                        Double negativeMarks, Double passPercentage, String createdBy,
                        LocalDateTime createdAt, Integer totalQuestions, List<QuestionResponse> questions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.totalMarks = totalMarks;
        this.negativeMarking = negativeMarking;
        this.negativeMarks = negativeMarks;
        this.passPercentage = passPercentage;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.totalQuestions = totalQuestions;
        this.questions = questions;
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
    public QuizStatus getStatus() { return status; }
    public void setStatus(QuizStatus status) { this.status = status; }
    public Double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Double totalMarks) { this.totalMarks = totalMarks; }
    public boolean isNegativeMarking() { return negativeMarking; }
    public void setNegativeMarking(boolean negativeMarking) { this.negativeMarking = negativeMarking; }
    public Double getNegativeMarks() { return negativeMarks; }
    public void setNegativeMarks(Double negativeMarks) { this.negativeMarks = negativeMarks; }
    public Double getPassPercentage() { return passPercentage; }
    public void setPassPercentage(Double passPercentage) { this.passPercentage = passPercentage; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public List<QuestionResponse> getQuestions() { return questions; }
    public void setQuestions(List<QuestionResponse> questions) { this.questions = questions; }
}
