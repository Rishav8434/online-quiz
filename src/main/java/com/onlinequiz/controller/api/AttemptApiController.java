package com.onlinequiz.controller.api;

import com.onlinequiz.dto.request.SubmitQuizRequest;
import com.onlinequiz.dto.response.AttemptResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.dto.response.ErrorResponse;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.AttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@Tag(name = "Quiz Attempts", description = "Endpoints for starting and submitting quiz attempts")
public class AttemptApiController {

    private final AttemptService attemptService;

    public AttemptApiController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Operation(summary = "Start a quiz attempt", description = "Starts an attempt session with timer tracking. Returns questions without answers.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempt started or resumed",
                    content = @Content(schema = @Schema(implementation = AttemptResponse.class))),
            @ApiResponse(responseCode = "400", description = "Quiz is not published",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/start")
    public ResponseEntity<AttemptResponse> startAttempt(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        AttemptResponse response = attemptService.startAttempt(id, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Submit a quiz attempt", description = "Evaluates submitted answers, scores quiz, and auto-submits upon timer timeout.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz submitted and evaluated successfully",
                    content = @Content(schema = @Schema(implementation = AttemptResultResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid submission payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attempt not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/submit")
    public ResponseEntity<AttemptResultResponse> submitAttempt(
            @PathVariable Long id,
            @RequestParam(required = false) Long attemptId,
            @Valid @RequestBody SubmitQuizRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long targetAttemptId = attemptId;
        if (targetAttemptId == null) {
            // Find in-progress attempt for user & quiz
            AttemptResponse active = attemptService.startAttempt(id, userPrincipal.getId());
            targetAttemptId = active.getAttemptId();
        }

        AttemptResultResponse response = attemptService.submitAttempt(targetAttemptId, request, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Submit quiz directly by attempt ID", description = "Evaluates answers for a specific attempt ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<AttemptResultResponse> submitAttemptById(
            @PathVariable Long attemptId,
            @Valid @RequestBody SubmitQuizRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        AttemptResultResponse response = attemptService.submitAttempt(attemptId, request, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }
}
