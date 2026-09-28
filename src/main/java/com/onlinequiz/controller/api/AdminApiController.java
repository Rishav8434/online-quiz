package com.onlinequiz.controller.api;

import com.onlinequiz.dto.response.AdminDashboardStatsResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Operations", description = "Endpoints for admin dashboard metrics, global results, and CSV exports")
public class AdminApiController {

    private final ResultService resultService;

    public AdminApiController(ResultService resultService) {
        this.resultService = resultService;
    }

    @Operation(summary = "Get admin dashboard overview stats", description = "Retrieves live counts of users, quizzes, attempts, and overall averages",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping({"/dashboard", "/analytics"})
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats() {
        AdminDashboardStatsResponse stats = resultService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get all participant attempts (paginated)", description = "Retrieves all completed attempts across all quizzes",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/results")
    public ResponseEntity<Page<AttemptResultResponse>> getAllResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<AttemptResultResponse> results = resultService.getAllAttemptsForAdmin(pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Export quiz results to CSV", description = "Streams CSV file containing all submissions and scores for a specific quiz",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/quizzes/{quizId}/export")
    public ResponseEntity<byte[]> exportQuizResultsCsv(@PathVariable Long quizId) {
        byte[] csvData = resultService.exportQuizResultsCsv(quizId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quiz_" + quizId + "_results.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @Operation(summary = "Export all results to CSV", description = "Streams CSV file containing all quiz attempts across the platform",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/results/export")
    public ResponseEntity<byte[]> exportAllResultsCsv() {
        byte[] csvData = resultService.exportAllResultsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"all_quiz_results.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
