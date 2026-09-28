package com.onlinequiz.dto.response;

import com.onlinequiz.enums.AttemptStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttemptResponse {

    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private Long remainingSeconds;
    private AttemptStatus status;
    private List<QuestionResponse> questions = new ArrayList<>();

    public AttemptResponse() {}

    public AttemptResponse(Long attemptId, Long quizId, String quizTitle, Integer durationMinutes,
                           LocalDateTime startTime, Long remainingSeconds, AttemptStatus status,
                           List<QuestionResponse> questions) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.remainingSeconds = remainingSeconds;
        this.status = status;
        this.questions = questions;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
}
