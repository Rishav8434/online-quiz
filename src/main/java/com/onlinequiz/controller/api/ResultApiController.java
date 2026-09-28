package com.onlinequiz.controller.api;

import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.dto.response.LeaderboardEntryResponse;
import com.onlinequiz.dto.response.QuizAnalyticsResponse;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.AttemptService;
import com.onlinequiz.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Results & Analytics", description = "Endpoints for scores, attempt history, leaderboard, and analytics")
public class ResultApiController {

    private final AttemptService attemptService;
    private final ResultService resultService;

    public ResultApiController(AttemptService attemptService, ResultService resultService) {
        this.attemptService = attemptService;
        this.resultService = resultService;
    }

    @Operation(summary = "Get user attempt history (paginated)", description = "Retrieves quiz attempts made by the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping({"/results/my", "/attempts/my"})
    public ResponseEntity<Page<AttemptResultResponse>> getMyAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<AttemptResultResponse> results = attemptService.getUserAttemptHistory(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get all user attempt history as list", description = "Retrieves all quiz attempts made by the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/results")
    public ResponseEntity<List<AttemptResultResponse>> getUserResults(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<AttemptResultResponse> results = attemptService.getUserAttemptHistory(userPrincipal.getId());
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get detailed result for a specific attempt", description = "Retrieves score breakdown and question-by-question analysis",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detailed attempt result",
                    content = @Content(schema = @Schema(implementation = AttemptResultResponse.class)))
    })
    @GetMapping("/results/{attemptId}")
    public ResponseEntity<AttemptResultResponse> getAttemptResult(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        AttemptResultResponse result = attemptService.getAttemptResult(attemptId, userPrincipal.getId());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get quiz leaderboard", description = "Retrieves global participant rankings for a quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leaderboard entries",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeaderboardEntryResponse.class))))
    })
    @GetMapping("/quizzes/{quizId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(@PathVariable Long quizId) {
        List<LeaderboardEntryResponse> leaderboard = resultService.getLeaderboard(quizId);
        return ResponseEntity.ok(leaderboard);
    }

    @Operation(summary = "Get quiz analytics (Admin only)", description = "Retrieves aggregate statistics and performance metrics for a quiz",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz analytics",
                    content = @Content(schema = @Schema(implementation = QuizAnalyticsResponse.class)))
    })
    @GetMapping("/quizzes/{quizId}/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizAnalyticsResponse> getQuizAnalytics(@PathVariable Long quizId) {
        QuizAnalyticsResponse analytics = resultService.getQuizAnalytics(quizId);
        return ResponseEntity.ok(analytics);
    }
}
