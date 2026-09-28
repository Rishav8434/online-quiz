package com.onlinequiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinequiz.controller.api.AuthApiController;
import com.onlinequiz.dto.request.LoginRequest;
import com.onlinequiz.dto.request.RegisterRequest;
import com.onlinequiz.dto.response.AuthResponse;
import com.onlinequiz.dto.response.UserResponse;
import com.onlinequiz.security.CustomUserDetailsService;
import com.onlinequiz.security.JwtAuthenticationEntryPoint;
import com.onlinequiz.security.JwtTokenProvider;
import com.onlinequiz.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.onlinequiz.security.RateLimiterService rateLimiterService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("POST /auth/register - Should return 201 Created on valid input")
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest("johnny", "johnny@example.com", "Password@123", "John Doe", "ROLE_PARTICIPANT");
        UserResponse response = new UserResponse(1L, "johnny", "johnny@example.com", "John Doe", Set.of("ROLE_PARTICIPANT"));

        when(rateLimiterService.tryAcquire(any())).thenReturn(true);
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("johnny"))
                .andExpect(jsonPath("$.email").value("johnny@example.com"));
    }

    @Test
    @DisplayName("POST /auth/login - Should return 200 OK with AuthResponse")
    void testLogin() throws Exception {
        LoginRequest request = new LoginRequest("johnny", "Password@123");
        UserResponse userResponse = new UserResponse(1L, "johnny", "johnny@example.com", "John Doe", Set.of("ROLE_PARTICIPANT"));
        AuthResponse response = new AuthResponse("mock.jwt.token", "mock.refresh.token", 86400000L, userResponse);

        when(rateLimiterService.tryAcquire(any())).thenReturn(true);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.refreshToken").value("mock.refresh.token"));
    }
}
