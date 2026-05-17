package com.balu.application_tracking_system.service;

import com.balu.application_tracking_system.entity.RefreshToken;
import com.balu.application_tracking_system.entity.User;
import com.balu.application_tracking_system.exception.ResourceNotFoundException;
import com.balu.application_tracking_system.repository.RefreshTokenRepository;
import com.balu.application_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    // VALIDATE refresh token
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found with id " + token));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token has expired. Please login again.");
        }

        return refreshToken;
    }

    // REVOKE on logout
    @Transactional
    public void revokedRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found with id " + token));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    // DELETE all tokens for user (force logout all devices)
    @Transactional
    public void deleteRefreshToken(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
