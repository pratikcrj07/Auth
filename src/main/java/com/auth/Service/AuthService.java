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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

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
                .accountNonLocked(true)
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(accessToken);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(accessToken);
    }
}
