package com.onlinequiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class CreateQuizRequest {

    @NotBlank(message = "Quiz title is required")
    @Size(min = 3, max = 150, message = "Quiz title must be between 3 and 150 characters")
    private String title;

    private String description;

    @NotNull(message = "Quiz duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    private Boolean negativeMarking = false;
    private Double negativeMarks = 0.0;
    private Double passPercentage = 50.0;

    @NotEmpty(message = "A quiz must have at least one question")
    @Valid
    private List<CreateQuestionRequest> questions = new ArrayList<>();

    public CreateQuizRequest() {}

    public CreateQuizRequest(String title, String description, Integer durationMinutes, List<CreateQuestionRequest> questions) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.questions = questions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Boolean getNegativeMarking() {
        return negativeMarking;
    }

    public void setNegativeMarking(Boolean negativeMarking) {
        this.negativeMarking = negativeMarking;
    }

    public Double getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(Double negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public Double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(Double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public List<CreateQuestionRequest> getQuestions() {
        return questions;
    }

    public void setQuestions(List<CreateQuestionRequest> questions) {
        this.questions = questions;
    }
}
