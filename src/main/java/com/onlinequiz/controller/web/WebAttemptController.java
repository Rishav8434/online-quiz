package com.onlinequiz.controller.web;

import com.onlinequiz.dto.response.AttemptResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.dto.response.QuizSummaryResponse;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.AttemptService;
import com.onlinequiz.service.QuizService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebAttemptController {

    private final AttemptService attemptService;
    private final QuizService quizService;

    public WebAttemptController(AttemptService attemptService, QuizService quizService) {
        this.attemptService = attemptService;
        this.quizService = quizService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        List<AttemptResultResponse> history = attemptService.getUserAttemptHistory(userPrincipal.getId());
        List<QuizSummaryResponse> availableQuizzes = quizService.getAllPublishedQuizzes();

        int totalAttempts = history.size();
        double avgScore = history.stream()
                .mapToDouble(AttemptResultResponse::getPercentage)
                .average()
                .orElse(0.0);

        long passedCount = history.stream()
                .filter(a -> a.getPercentage() >= 70.0)
                .count();

        model.addAttribute("currentUser", userPrincipal);
        model.addAttribute("recentAttempts", history.stream().limit(5).toList());
        model.addAttribute("availableQuizzes", availableQuizzes);
        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("avgScore", Math.round(avgScore * 10.0) / 10.0);
        model.addAttribute("passedCount", passedCount);

        return "user/dashboard";
    }

    @GetMapping("/history")
    public String attemptHistory(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        List<AttemptResultResponse> history = attemptService.getUserAttemptHistory(userPrincipal.getId());
        model.addAttribute("currentUser", userPrincipal);
        model.addAttribute("history", history);
        return "user/history";
    }

    @GetMapping("/quizzes/{id}/attempt")
    public String attemptQuiz(@PathVariable Long id,
                              @AuthenticationPrincipal UserPrincipal userPrincipal,
                              Model model) {
        AttemptResponse attempt = attemptService.startAttempt(id, userPrincipal.getId());
        model.addAttribute("currentUser", userPrincipal);
        model.addAttribute("attempt", attempt);
        return "user/quiz-attempt";
    }

    @GetMapping("/quizzes/{id}/result")
    public String quizResult(@PathVariable Long id,
                             @RequestParam Long attemptId,
                             @AuthenticationPrincipal UserPrincipal userPrincipal,
                             Model model) {
        AttemptResultResponse result = attemptService.getAttemptResult(attemptId, userPrincipal.getId());
        model.addAttribute("currentUser", userPrincipal);
        model.addAttribute("result", result);
        return "user/quiz-result";
    }
}
