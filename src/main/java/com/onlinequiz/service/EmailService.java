package com.onlinequiz.service;

public interface EmailService {
    void sendRegistrationSuccessEmail(String toEmail, String fullName);
    void sendPasswordResetEmail(String toEmail, String resetLink);
    void sendPasswordResetEmail(String toEmail, String resetLink, String fullName);
    void sendQuizResultEmail(String toEmail, String fullName, String quizTitle, Double score, double percentage, String resultUrl);
}
