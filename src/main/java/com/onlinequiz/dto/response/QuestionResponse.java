package com.onlinequiz.dto.response;

import java.util.ArrayList;
import java.util.List;

public class QuestionResponse {

    private Long id;
    private String text;
    private Double marks = 1.0;
    private Double negativeMarks = 0.0;
    private List<OptionResponse> options = new ArrayList<>();

    public QuestionResponse() {}

    public QuestionResponse(Long id, String text, Double marks, Double negativeMarks, List<OptionResponse> options) {
        this.id = id;
        this.text = text;
        this.marks = marks;
        this.negativeMarks = negativeMarks;
        this.options = options;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Double getMarks() { return marks; }
    public void setMarks(Double marks) { this.marks = marks; }
    public Double getNegativeMarks() { return negativeMarks; }
    public void setNegativeMarks(Double negativeMarks) { this.negativeMarks = negativeMarks; }
    public List<OptionResponse> getOptions() { return options; }
    public void setOptions(List<OptionResponse> options) { this.options = options; }
}
