package com.onlinequiz.service;

import com.onlinequiz.dto.request.LoginRequest;
import com.onlinequiz.dto.request.RegisterRequest;
import com.onlinequiz.dto.response.AuthResponse;
import com.onlinequiz.dto.response.UserResponse;
import com.onlinequiz.entity.RefreshToken;
import com.onlinequiz.entity.Role;
import com.onlinequiz.entity.User;
import com.onlinequiz.enums.RoleType;
import com.onlinequiz.exception.DuplicateResourceException;
import com.onlinequiz.mapper.UserMapper;
import com.onlinequiz.repository.RefreshTokenRepository;
import com.onlinequiz.repository.RoleRepository;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.security.JwtTokenProvider;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private com.onlinequiz.repository.PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private Role participantRole;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenDurationMs", 604800000L);

        participantRole = new Role(RoleType.ROLE_PARTICIPANT);
        participantRole.setId(1L);

        user = new User("testuser", "test@example.com", "encodedPassword", "Test User");
        user.setId(1L);
        user.setRoles(Set.of(participantRole));
    }

    @Test
    @DisplayName("Should successfully register a new participant user")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("newuser", "new@example.com", "Password@123", "New User", "ROLE_PARTICIPANT");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleType.ROLE_PARTICIPANT)).thenReturn(Optional.of(participantRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse expectedResponse = new UserResponse(1L, "testuser", "test@example.com", "Test User", Set.of("ROLE_PARTICIPANT"));
        when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when username exists")
    void testRegisterDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("existinguser", "new@example.com", "Password@123", "New User", "ROLE_PARTICIPANT");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully and return access token and refresh token")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("testuser", "Password@123");

        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.string");
        when(tokenProvider.getJwtExpirationInMs()).thenReturn(86400000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        RefreshToken refreshToken = new RefreshToken(user, "refresh-uuid-token", Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        UserResponse userResponse = new UserResponse(1L, "testuser", "test@example.com", "Test User", Set.of("ROLE_PARTICIPANT"));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse authResponse = authService.login(request);

        assertNotNull(authResponse);
        assertEquals("jwt.token.string", authResponse.getAccessToken());
        assertEquals("refresh-uuid-token", authResponse.getRefreshToken());
    }
}
