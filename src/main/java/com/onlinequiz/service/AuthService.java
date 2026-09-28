package com.onlinequiz.service;

import com.onlinequiz.dto.request.ForgotPasswordRequest;
import com.onlinequiz.dto.request.LoginRequest;
import com.onlinequiz.dto.request.RefreshTokenRequest;
import com.onlinequiz.dto.request.RegisterRequest;
import com.onlinequiz.dto.request.ResetPasswordRequest;
import com.onlinequiz.dto.response.AuthResponse;
import com.onlinequiz.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    UserResponse getCurrentUserProfile(Long userId);
    void logout(Long userId);
    void forgotPassword(ForgotPasswordRequest request, String appBaseUrl);
    void resetPassword(ResetPasswordRequest request);
}
