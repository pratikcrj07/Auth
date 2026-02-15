package com.auth.Service;

import com.auth.Config.JwtUtil;
import com.auth.Dto.AuthResponse;
import com.auth.Dto.LoginRequest;
import com.auth.Dto.RegisterRequest;
import com.auth.Entity.Role;
import com.auth.Entity.User;
import com.auth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    // REGISTER
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        userRepository.save(user);

        String accessToken =
                jwtUtil.generateAccessToken(user.getEmail(),
                        user.getRole().name());

        String refreshToken =
                refreshTokenService.createRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    // LOGIN
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!user.isAccountNonLocked()) {
            if (!loginAttemptService.unlockIfExpired(user)) {
                throw new RuntimeException("Account locked");
            }
        }

        if (!passwordEncoder.matches(request.getPassword(),
                user.getPassword())) {

            loginAttemptService.loginFailed(user);
            throw new RuntimeException("Invalid credentials");
        }

        loginAttemptService.loginSucceeded(user);

        String accessToken =
                jwtUtil.generateAccessToken(user.getEmail(),
                        user.getRole().name());

        String refreshToken =
                refreshTokenService.createRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    // REFRESH
    public String refresh(String refreshToken) {

        String username =
                jwtUtil.extractUsername(refreshToken, false);

        if (!refreshTokenService
                .validateRefreshToken(username, refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        return jwtUtil.generateAccessToken(
                username,
                userRepository.findByEmail(username).get()
                        .getRole().name()
        );
    }

    // LOGOUT
    public void logout(String accessToken, String username) {

        refreshTokenService.deleteRefreshToken(username);

        refreshTokenService.blacklistAccessToken(
                accessToken,
                accessExpiration
        );
    }
}
