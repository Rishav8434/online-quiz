package com.onlinequiz.dto.response;

public class OptionResponse {

    private Long id;
    private String text;
    private Boolean isCorrect; // null when participant is attempting quiz

    public OptionResponse() {}

    public OptionResponse(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public OptionResponse(Long id, String text, Boolean isCorrect) {
        this.id = id;
        this.text = text;
        this.isCorrect = isCorrect;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
