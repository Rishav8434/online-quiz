package com.onlinequiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SubmitQuizRequest {

    @NotNull(message = "Answers list must not be null")
    @Valid
    private List<SubmitAnswerRequest> answers = new ArrayList<>();

    public SubmitQuizRequest() {}

    public SubmitQuizRequest(List<SubmitAnswerRequest> answers) {
        this.answers = answers;
    }

    public List<SubmitAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SubmitAnswerRequest> answers) {
        this.answers = answers;
    }
}
