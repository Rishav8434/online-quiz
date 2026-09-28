package com.onlinequiz.controller.web;

import com.onlinequiz.dto.response.LeaderboardEntryResponse;
import com.onlinequiz.dto.response.QuizResponse;
import com.onlinequiz.dto.response.QuizSummaryResponse;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/quizzes")
public class WebQuizController {

    private final QuizService quizService;
    private final ResultService resultService;

    public WebQuizController(QuizService quizService, ResultService resultService) {
        this.quizService = quizService;
        this.resultService = resultService;
    }

    @GetMapping
    public String listQuizzes(Model model, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<QuizSummaryResponse> quizzes = quizService.getAllPublishedQuizzes();
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("currentUser", userPrincipal);
        return "user/quiz-list";
    }

    @GetMapping("/{id}")
    public String quizDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isAdmin = userPrincipal != null && userPrincipal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        QuizResponse quiz = quizService.getQuizById(id, isAdmin);
        List<LeaderboardEntryResponse> leaderboard = resultService.getLeaderboard(id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("currentUser", userPrincipal);
        return "user/quiz-details";
    }
}
