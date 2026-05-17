package com.balu.application_tracking_system.controller;

import com.balu.application_tracking_system.dto.*;
import com.balu.application_tracking_system.entity.RefreshToken;
import com.balu.application_tracking_system.entity.User;
import com.balu.application_tracking_system.security.JwtUtil;
import com.balu.application_tracking_system.service.RefreshTokenService;
import com.balu.application_tracking_system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Authentication", description = "Register, Login, Logout APIs")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Register a new candidate")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(dto));
    }

    @Operation(summary = "Login and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    @Operation(summary = "Logged out a candidate")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        refreshTokenService.revokedRefreshToken(dto.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }

    @Operation(summary = "Refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO dto) {

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(dto.getRefreshToken());

        User user = refreshToken.getUser();

        String newAccessToken = jwtUtil.createToken(user.getEmail(), user.getRole().name());

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        LoginResponseDTO response = new LoginResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                newAccessToken,
                "Bearer ",
                newRefreshToken.getToken()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change password")
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequestDTO dto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        userService.changePassword(email, dto);

        return ResponseEntity.ok("Password changed successfully");
    }
}
