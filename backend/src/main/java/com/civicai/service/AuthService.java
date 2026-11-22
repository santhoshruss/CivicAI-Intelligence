package com.civicai.service;

import com.civicai.dto.*;
import com.civicai.entity.RefreshToken;
import com.civicai.entity.Role;
import com.civicai.entity.User;
import com.civicai.exception.BadRequestException;
import com.civicai.exception.ResourceNotFoundException;
import com.civicai.exception.UnauthorizedException;
import com.civicai.repository.RefreshTokenRepository;
import com.civicai.repository.UserRepository;
import com.civicai.security.CustomUserDetails;
import com.civicai.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Value("${civicai.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("Email address is already registered.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CITIZEN);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        auditService.logAction(savedUser, "USER_REGISTERED", "User", savedUser.getId(), "Citizen registered", null);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String accessToken = tokenProvider.generateToken(userDetails);
        String refreshTokenStr = createRefreshToken(savedUser);

        return new AuthResponse(
                accessToken,
                refreshTokenStr,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                null
        );
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!user.getEnabled()) {
            throw new UnauthorizedException("Account is disabled. Please contact administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = tokenProvider.generateToken(userDetails);
        String refreshTokenStr = createRefreshToken(user);

        auditService.logAction(user, "USER_LOGIN", "User", user.getId(), "User logged in", null);

        Long deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        return new AuthResponse(
                accessToken,
                refreshTokenStr,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                deptId
        );
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        if (token.getRevoked() || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token is expired or revoked.");
        }

        User user = token.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = tokenProvider.generateToken(userDetails);

        // Rotate refresh token
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        String newRefreshTokenStr = createRefreshToken(user);

        Long deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        return new AuthResponse(
                newAccessToken,
                newRefreshTokenStr,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                deptId
        );
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null) {
            refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    public UserResponseDto getCurrentUser(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
            dto.setDepartmentName(user.getDepartment().getName());
        }
        return dto;
    }

    private String createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        token.setRevoked(false);
        refreshTokenRepository.save(token);
        return token.getToken();
    }
}
