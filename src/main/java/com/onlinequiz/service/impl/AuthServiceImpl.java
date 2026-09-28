package com.onlinequiz.service.impl;

import com.onlinequiz.dto.request.ForgotPasswordRequest;
import com.onlinequiz.dto.request.LoginRequest;
import com.onlinequiz.dto.request.RefreshTokenRequest;
import com.onlinequiz.dto.request.RegisterRequest;
import com.onlinequiz.dto.request.ResetPasswordRequest;
import com.onlinequiz.dto.response.AuthResponse;
import com.onlinequiz.dto.response.UserResponse;
import com.onlinequiz.entity.PasswordResetToken;
import com.onlinequiz.entity.RefreshToken;
import com.onlinequiz.entity.Role;
import com.onlinequiz.entity.User;
import com.onlinequiz.enums.RoleType;
import com.onlinequiz.exception.BadRequestException;
import com.onlinequiz.exception.DuplicateResourceException;
import com.onlinequiz.exception.ResourceNotFoundException;
import com.onlinequiz.mapper.UserMapper;
import com.onlinequiz.repository.PasswordResetTokenRepository;
import com.onlinequiz.repository.RefreshTokenRepository;
import com.onlinequiz.repository.RoleRepository;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.security.JwtTokenProvider;
import com.onlinequiz.security.UserPrincipal;
import com.onlinequiz.service.AuthService;
import com.onlinequiz.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-expiration-ms:604800000}") // 7 days
    private long refreshTokenDurationMs;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider,
                           UserMapper userMapper,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userMapper = userMapper;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName()
        );

        Set<Role> roles = new HashSet<>();
        if ("ROLE_ADMIN".equalsIgnoreCase(request.getRole())) {
            Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_ADMIN)));
            roles.add(adminRole);
        }

        Role participantRole = roleRepository.findByName(RoleType.ROLE_PARTICIPANT)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_PARTICIPANT)));
        roles.add(participantRole);

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        log.info("New user registered successfully: username={}, email={}", savedUser.getUsername(), savedUser.getEmail());

        // Send registration email asynchronously
        try {
            emailService.sendRegistrationSuccessEmail(savedUser.getEmail(), savedUser.getFullName());
        } catch (Exception e) {
            log.warn("Failed to dispatch registration email to {}: {}", savedUser.getEmail(), e.getMessage());
        }

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = tokenProvider.generateToken(authentication);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        RefreshToken refreshToken = createRefreshToken(user);

        log.info("User logged in successfully: username={}", user.getUsername());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                tokenProvider.getJwtExpirationInMs(),
                userMapper.toResponse(user)
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token has expired. Please log in again.");
        }

        User user = refreshToken.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateTokenFromUserPrincipal(userPrincipal);

        return new AuthResponse(
                newAccessToken,
                refreshToken.getToken(),
                tokenProvider.getJwtExpirationInMs(),
                userMapper.toResponse(user)
        );
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            refreshTokenRepository.deleteByUser(user);
            log.info("User logged out and refresh token revoked: userId={}", userId);
        });
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, String appBaseUrl) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            // Invalidate any existing unused reset tokens for this user
            passwordResetTokenRepository.findByUserAndUsedFalse(user).ifPresent(existingToken -> {
                existingToken.setUsed(true);
                passwordResetTokenRepository.save(existingToken);
            });

            String tokenString = UUID.randomUUID().toString();
            Instant expiry = Instant.now().plus(1, ChronoUnit.HOURS);

            PasswordResetToken resetToken = new PasswordResetToken(user, tokenString, expiry);
            passwordResetTokenRepository.save(resetToken);

            String base = (appBaseUrl != null && !appBaseUrl.isBlank()) ? appBaseUrl : "http://localhost:8080";
            String resetUrl = base + "/reset-password?token=" + tokenString;

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), resetUrl, user.getFullName());
                log.info("Password reset token generated and email dispatched for user: {}", user.getUsername());
            } catch (Exception e) {
                log.warn("Failed to dispatch password reset email to {}: {}", user.getEmail(), e.getMessage());
            }
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or already used password reset token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens on password change
        refreshTokenRepository.deleteByUser(user);

        log.info("Password reset successfully completed for user: {}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toResponse(user);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(refreshToken);
    }
}
