package com.balu.application_tracking_system.service;

import com.balu.application_tracking_system.dto.*;
import com.balu.application_tracking_system.entity.RefreshToken;
import com.balu.application_tracking_system.entity.User;
import com.balu.application_tracking_system.exception.DuplicateUserFoundException;
import com.balu.application_tracking_system.exception.InvalidCredentialsException;
import com.balu.application_tracking_system.exception.ResourceNotFoundException;
import com.balu.application_tracking_system.repository.UserRepository;
import com.balu.application_tracking_system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public UserResponseDTO register(RegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateUserFoundException("Email already exists");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole(User.Role.CANDIDATE);

        User saved = userRepository.save(user);
        return mapToDto(saved);
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email" + dto.getEmail()));

        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        String accessToken = jwtUtil.createToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new LoginResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                accessToken,
                "Bearer ",
                refreshToken.getToken()
        );
    }

    public void changePassword(String email, ChangePasswordRequestDTO dto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email address" + email));

        // Step 2: Verify current password is correct
        if (!encoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        // Step 3: Check new password and confirm password match
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new InvalidCredentialsException("New password and confirm new password do not match");
        }

        // Step 4: Check new password is different from current
        if (encoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("New password must be different from current password");
        }

        // Step 5: Encode and save new password
        user.setPassword(encoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // Step 6: Invalidate all refresh tokens — force re-login on all devices
        refreshTokenService.deleteRefreshToken(user.getId());
    }

    // createHr (Admin only):
    public UserResponseDTO createHr(RegisterRequestDTO dto, String adminEmail) {

        // 1. Validate requesting user is ADMIN
        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (adminUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admins can create hr accounts.");
        }

        // 2. Check email not already registered
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateUserFoundException("Email already registered. Please register with new email.");
        }

        // 3. Create user with HR role
        User hrUser = new User();
        hrUser.setFullName(dto.getFullName());
        hrUser.setEmail(dto.getEmail());
        hrUser.setPassword(encoder.encode(dto.getPassword()));
        hrUser.setPhone(dto.getPhone());
        hrUser.setRole(User.Role.HR);

        // 4. Send credentials (for now just return them)
        // 5. Save and return
        User hrCreated = userRepository.save(hrUser);
        return mapToDto(hrCreated);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserResponseDTO mapToDto(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
