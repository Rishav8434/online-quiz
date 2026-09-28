package com.onlinequiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateOptionRequest {

    @NotBlank(message = "Option text cannot be blank")
    @Size(max = 500, message = "Option text cannot exceed 500 characters")
    private String text;

    @NotNull(message = "isCorrect flag must be specified")
    private Boolean isCorrect;

    public CreateOptionRequest() {}

    public CreateOptionRequest(String text, Boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
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
