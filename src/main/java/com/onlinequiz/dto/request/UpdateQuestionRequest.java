package com.onlinequiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class UpdateQuestionRequest {

    @NotBlank(message = "Question text cannot be blank")
    private String text;

    private Double marks = 1.0;
    private Double negativeMarks = 0.0;

    @NotEmpty(message = "Question must have at least 2 options")
    @Size(min = 2, max = 6, message = "Question must have between 2 and 6 options")
    @Valid
    private List<CreateOptionRequest> options = new ArrayList<>();

    public UpdateQuestionRequest() {}

    public UpdateQuestionRequest(String text, Double marks, Double negativeMarks, List<CreateOptionRequest> options) {
        this.text = text;
        this.marks = marks;
        this.negativeMarks = negativeMarks;
        this.options = options;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public Double getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(Double negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public List<CreateOptionRequest> getOptions() {
        return options;
    }

    public void setOptions(List<CreateOptionRequest> options) {
        this.options = options;
    }
}
