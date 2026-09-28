package com.onlinequiz.controller.web;

import com.onlinequiz.dto.response.LeaderboardEntryResponse;
import com.onlinequiz.dto.response.QuizAnalyticsResponse;
import com.onlinequiz.dto.response.QuizResponse;
import com.onlinequiz.dto.response.QuizSummaryResponse;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class WebAdminController {

    private final QuizService quizService;
    private final ResultService resultService;

    public WebAdminController(QuizService quizService, ResultService resultService) {
        this.quizService = quizService;
        this.resultService = resultService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        List<QuizSummaryResponse> allQuizzes = quizService.getAllQuizzesForAdmin();
        var stats = resultService.getAdminDashboardStats();

        model.addAttribute("currentUser", userPrincipal);
        model.addAttribute("quizzes", allQuizzes);
        model.addAttribute("stats", stats);
        model.addAttribute("totalQuizzes", stats.getTotalQuizzes());
        model.addAttribute("publishedQuizzes", stats.getPublishedQuizzes());

        return "admin/dashboard";
    }

    @GetMapping("/quizzes/create")
    public String createQuizPage(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        model.addAttribute("currentUser", userPrincipal);
        return "admin/quiz-create";
    }

    @GetMapping("/quizzes/{id}/edit")
    public String editQuizPage(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        QuizResponse quiz = quizService.getQuizById(id, true);
        model.addAttribute("currentUser", userPrincipal);
        model.addAttribute("quiz", quiz);
        return "admin/quiz-edit";
    }

    @GetMapping({"/analytics", "/quizzes/{id}/analytics"})
    public String quizAnalytics(@PathVariable(required = false) Long id,
                                @AuthenticationPrincipal UserPrincipal userPrincipal,
                                Model model) {
        model.addAttribute("currentUser", userPrincipal);
        if (id != null) {
            QuizAnalyticsResponse analytics = resultService.getQuizAnalytics(id);
            model.addAttribute("analytics", analytics);
        } else {
            var stats = resultService.getAdminDashboardStats();
            model.addAttribute("globalStats", stats);
            List<QuizSummaryResponse> allQuizzes = quizService.getAllQuizzesForAdmin();
            model.addAttribute("quizzes", allQuizzes);
        }
        return "admin/analytics";
    }

    @GetMapping("/results")
    public String allResults(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        var stats = resultService.getAdminDashboardStats();
        List<QuizSummaryResponse> allQuizzes = quizService.getAllQuizzesForAdmin();

        model.addAttribute("currentUser", userPrincipal);
        model.addAttribute("stats", stats);
        model.addAttribute("quizzes", allQuizzes);
        return "admin/dashboard";
    }

    @GetMapping("/quizzes/{id}/leaderboard")
    public String quizLeaderboard(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        QuizResponse quiz = quizService.getQuizById(id, true);
        List<LeaderboardEntryResponse> leaderboard = resultService.getLeaderboard(id);

        model.addAttribute("currentUser", userPrincipal);
        model.addAttribute("quiz", quiz);
        model.addAttribute("leaderboard", leaderboard);
        return "admin/leaderboard";
    }
}
