package com.onlinequiz.controller.api;

import com.onlinequiz.dto.request.UpdateQuestionRequest;
import com.onlinequiz.dto.response.QuestionResponse;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Question Management", description = "Endpoints for editing, deleting, and inspecting individual quiz questions")
public class QuestionApiController {

    private final QuestionService questionService;

    public QuestionApiController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Operation(summary = "Get question by ID (Admin only)", description = "Retrieves question details along with option correctness",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long id) {
        QuestionResponse response = questionService.getQuestionById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update question (Admin only)", description = "Updates question text, marks, negative marks, and options",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateQuestionRequest request,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        QuestionResponse response = questionService.updateQuestion(id, request, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete question (Admin only)", description = "Soft-deletes a question from its quiz",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id,
                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        questionService.deleteQuestion(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
