package com.onlinequiz.service.impl;

import com.onlinequiz.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@onlinequiz.com}")
    private String fromEmail;

    @Value("${app.mail.enabled:false}")
    private boolean emailEnabled;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendRegistrationSuccessEmail(String toEmail, String fullName) {
        String subject = "Welcome to Online Quiz Platform!";
        String content = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #1e293b; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
                        .header { text-align: center; border-bottom: 2px solid #e2e8f0; padding-bottom: 20px; margin-bottom: 24px; }
                        .header h1 { color: #4f46e5; margin: 0; font-size: 26px; }
                        .content { line-height: 1.6; font-size: 16px; }
                        .btn { display: inline-block; background-color: #4f46e5; color: #ffffff !important; padding: 12px 28px; text-decoration: none; border-radius: 8px; font-weight: 600; margin-top: 20px; }
                        .footer { margin-top: 30px; text-align: center; font-size: 13px; color: #94a3b8; border-top: 1px solid #e2e8f0; padding-top: 16px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Online Quiz Platform</h1>
                        </div>
                        <div class="content">
                            <p>Hello <strong>%s</strong>,</p>
                            <p>Thank you for registering with our Enterprise Online Quiz platform! Your account is active and you are ready to test your knowledge.</p>
                            <p>Browse through challenging quizzes created by domain experts and compete on global leaderboards.</p>
                            <div style="text-align: center;">
                                <a href="http://localhost:8080/quizzes" class="btn">Explore Quizzes</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 Online Quiz Platform. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(fullName));

        sendHtmlEmail(toEmail, subject, content);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        sendPasswordResetEmail(toEmail, resetLink, "User");
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink, String fullName) {
        String subject = "Password Reset Request";
        String displayName = (fullName != null && !fullName.isBlank()) ? fullName : "User";
        String content = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #1e293b; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
                        .header h1 { color: #dc2626; margin: 0; font-size: 24px; }
                        .content { line-height: 1.6; font-size: 16px; }
                        .btn { display: inline-block; background-color: #dc2626; color: #ffffff !important; padding: 12px 28px; text-decoration: none; border-radius: 8px; font-weight: 600; margin-top: 20px; }
                        .footer { margin-top: 30px; text-align: center; font-size: 13px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Password Reset</h1>
                        </div>
                        <div class="content">
                            <p>Hello <strong>%s</strong>,</p>
                            <p>We received a request to reset your password. Click the button below to choose a new password:</p>
                            <div style="text-align: center;">
                                <a href="%s" class="btn">Reset Password</a>
                            </div>
                            <p style="margin-top: 20px;">If you did not request this, you can safely ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 Online Quiz Platform. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(displayName), resetLink);

        sendHtmlEmail(toEmail, subject, content);
    }

    @Override
    @Async
    public void sendQuizResultEmail(String toEmail, String fullName, String quizTitle, Double score, double percentage, String resultUrl) {
        String subject = "Your Quiz Results: " + quizTitle;
        String gradeColor = percentage >= 75.0 ? "#16a34a" : (percentage >= 50.0 ? "#ca8a04" : "#dc2626");

        String content = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #1e293b; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
                        .header h1 { color: #4f46e5; margin: 0; font-size: 24px; text-align: center; }
                        .score-box { background-color: #f1f5f9; border-radius: 8px; padding: 20px; text-align: center; margin: 24px 0; }
                        .score-value { font-size: 42px; font-weight: bold; color: %s; }
                        .btn { display: inline-block; background-color: #4f46e5; color: #ffffff !important; padding: 12px 28px; text-decoration: none; border-radius: 8px; font-weight: 600; }
                        .footer { margin-top: 30px; text-align: center; font-size: 13px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Quiz Completion Report</h1>
                        </div>
                        <div class="content">
                            <p>Hello <strong>%s</strong>,</p>
                            <p>You have successfully completed <strong>%s</strong>.</p>
                            <div class="score-box">
                                <div>Your Score</div>
                                <div class="score-value">%.1f%%</div>
                                <div style="color: #64748b; margin-top: 8px;">Points: %d</div>
                            </div>
                            <div style="text-align: center;">
                                <a href="%s" class="btn">View Detailed Analysis</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 Online Quiz Platform. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(gradeColor, escapeHtml(fullName), escapeHtml(quizTitle), percentage, score, resultUrl);

        sendHtmlEmail(toEmail, subject, content);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!emailEnabled) {
            logger.info("Email service disabled in properties. Email to '{}' with subject '{}' logged instead.", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("Successfully sent email to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}", to, e);
        }
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
