package com.onlinequiz.dto.request;

import com.onlinequiz.enums.QuizStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateQuizRequest {

    @NotBlank(message = "Quiz title is required")
    @Size(min = 3, max = 150, message = "Quiz title must be between 3 and 150 characters")
    private String title;

    private String description;

    @NotNull(message = "Quiz duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    private QuizStatus status;
    private Boolean negativeMarking;
    private Double negativeMarks;
    private Double passPercentage;

    public UpdateQuizRequest() {}

    public UpdateQuizRequest(String title, String description, Integer durationMinutes,
                             QuizStatus status, Boolean negativeMarking, Double negativeMarks, Double passPercentage) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.negativeMarking = negativeMarking;
        this.negativeMarks = negativeMarks;
        this.passPercentage = passPercentage;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public QuizStatus getStatus() { return status; }
    public void setStatus(QuizStatus status) { this.status = status; }
    public Boolean getNegativeMarking() { return negativeMarking; }
    public void setNegativeMarking(Boolean negativeMarking) { this.negativeMarking = negativeMarking; }
    public Double getNegativeMarks() { return negativeMarks; }
    public void setNegativeMarks(Double negativeMarks) { this.negativeMarks = negativeMarks; }
    public Double getPassPercentage() { return passPercentage; }
    public void setPassPercentage(Double passPercentage) { this.passPercentage = passPercentage; }
}
