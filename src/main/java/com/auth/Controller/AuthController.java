package com.auth.Controller;

import com.auth.Dto.*;
import com.auth.Service.AuthService;
import com.auth.Service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    // REFRESH TOKEN
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(
            @RequestParam String refreshToken) {

        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authHeader,
            Authentication authentication) {

        String accessToken = authHeader.substring(7);
        String username = authentication.getName();

        authService.logout(accessToken, username);

        return ResponseEntity.ok("Logged out successfully");
    }

    // SEND OTP
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(
            @RequestBody OtpRequest request) {

        String otp = otpService.generateOtp(request.getPhone());

        return ResponseEntity.ok("OTP generated (demo): " + otp);
    }

    // VERIFY OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestBody OtpVerifyRequest request) {

        boolean valid = otpService
                .verifyOtp(request.getPhone(), request.getOtp());

        if (!valid) {
            throw new RuntimeException("Invalid OTP");
        }

        return ResponseEntity.ok("OTP verified successfully");
    }
}

