package com.onlinequiz.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class QuizExpiredException extends RuntimeException {

    public QuizExpiredException(String message) {
        super(message);
    }
}
