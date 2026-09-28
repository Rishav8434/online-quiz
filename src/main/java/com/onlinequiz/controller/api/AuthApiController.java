package com.onlinequiz.controller.api;

import com.onlinequiz.dto.request.ForgotPasswordRequest;
import com.onlinequiz.dto.request.LoginRequest;
import com.onlinequiz.dto.request.RefreshTokenRequest;
import com.onlinequiz.dto.request.RegisterRequest;
import com.onlinequiz.dto.request.ResetPasswordRequest;
import com.onlinequiz.dto.response.AuthResponse;
import com.onlinequiz.dto.response.ErrorResponse;
import com.onlinequiz.dto.response.UserResponse;
import com.onlinequiz.exception.BadRequestException;
import com.onlinequiz.security.RateLimiterService;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/auth"})
@Tag(name = "Authentication", description = "Endpoints for user registration, login, logout, password reset, and token management")
public class AuthApiController {

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;

    public AuthApiController(AuthService authService, RateLimiterService rateLimiterService) {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
    }

    @Operation(summary = "Register a new user", description = "Creates a participant or admin account with secure BCrypt password hashing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too many registration attempts",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryAcquire("register:" + clientIp)) {
            throw new BadRequestException("Too many registration requests. Please wait before trying again.");
        }

        UserResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Authenticate user and get JWT", description = "Returns an access JWT and refresh token upon successful authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or rate limiting error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryAcquire("login:" + clientIp)) {
            throw new BadRequestException("Too many login attempts. Please wait before trying again.");
        }

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping({"/refresh", "/refresh-token"})
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout user", description = "Revokes refresh tokens for authenticated user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            authService.logout(userPrincipal.getId());
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @Operation(summary = "Request password reset", description = "Generates a reset token and sends a reset email")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                              HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryAcquire("forgot:" + clientIp)) {
            throw new BadRequestException("Too many password reset requests. Please wait.");
        }

        String appBaseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName() +
                (httpRequest.getServerPort() != 80 && httpRequest.getServerPort() != 443 ? ":" + httpRequest.getServerPort() : "");

        authService.forgotPassword(request, appBaseUrl);
        return ResponseEntity.ok(Map.of("message", "If an account with that email exists, password reset instructions have been sent"));
    }

    @Operation(summary = "Reset password", description = "Resets user password using a verified reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                             HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryAcquire("reset:" + clientIp)) {
            throw new BadRequestException("Too many reset attempts. Please wait.");
        }

        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You may now log in with your new password."));
    }

    @Operation(summary = "Get current user profile", description = "Retrieves profile of the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user profile",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse response = authService.getCurrentUserProfile(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
