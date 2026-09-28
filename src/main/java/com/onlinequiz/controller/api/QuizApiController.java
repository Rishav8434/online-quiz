package com.onlinequiz.controller.api;

import com.onlinequiz.dto.request.CreateQuestionRequest;
import com.onlinequiz.dto.request.CreateQuizRequest;
import com.onlinequiz.dto.request.UpdateQuizRequest;
import com.onlinequiz.dto.response.ErrorResponse;
import com.onlinequiz.dto.response.QuestionResponse;
import com.onlinequiz.dto.response.QuizResponse;
import com.onlinequiz.dto.response.QuizSummaryResponse;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.QuestionService;
import com.onlinequiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@Tag(name = "Quiz Management", description = "Endpoints for creating, managing, browsing, and publishing quizzes")
public class QuizApiController {

    private final QuizService quizService;
    private final QuestionService questionService;

    public QuizApiController(QuizService quizService, QuestionService questionService) {
        this.quizService = quizService;
        this.questionService = questionService;
    }

    @Operation(summary = "Create a new quiz (Admin only)", description = "Creates a quiz with questions, options, and correct answers",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quiz created successfully",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quiz payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> createQuiz(@Valid @RequestBody CreateQuizRequest request,
                                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        QuizResponse response = quizService.createQuiz(request, userPrincipal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "List published quizzes with search and pagination", description = "Public endpoint to retrieve quizzes available for participants")
    @GetMapping
    public ResponseEntity<Page<QuizSummaryResponse>> getPublishedQuizzes(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);
        Page<QuizSummaryResponse> quizzes = quizService.getPublishedQuizzes(search, pageable);
        return ResponseEntity.ok(quizzes);
    }

    @Operation(summary = "Get quiz details by ID", description = "Retrieves quiz details. Answer keys are hidden for regular participants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz details",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isAdmin = userPrincipal != null && userPrincipal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        QuizResponse response = quizService.getQuizById(id, isAdmin);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update quiz metadata (Admin only)", description = "Updates title, description, duration, negative marking, and pass percentage",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> updateQuiz(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateQuizRequest request,
                                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        QuizResponse response = quizService.updateQuiz(id, request, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete quiz (Admin only)", description = "Soft-deletes a quiz",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quizService.deleteQuiz(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Publish quiz (Admin only)", description = "Transitions quiz status to PUBLISHED making it attemptable",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> publishQuiz(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserPrincipal userPrincipal) {
        QuizResponse response = quizService.publishQuiz(id, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Unpublish quiz (Admin only)", description = "Transitions quiz status to DRAFT",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> unpublishQuiz(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        QuizResponse response = quizService.unpublishQuiz(id, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add question to quiz (Admin only)", description = "Adds a new question with multiple-choice options to the quiz",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> addQuestion(@PathVariable Long id,
                                                        @Valid @RequestBody CreateQuestionRequest request,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        QuestionResponse response = questionService.addQuestionToQuiz(id, request, userPrincipal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
