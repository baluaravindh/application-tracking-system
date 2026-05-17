package com.balu.application_tracking_system.controller;

import com.balu.application_tracking_system.dto.RegisterRequestDTO;
import com.balu.application_tracking_system.dto.UserResponseDTO;
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

@Tag(name = "Admin", description = "Admin management APIs")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    //  POST /api/admin/create-hr   → Admin creates HR account
    @Operation(summary = "Create HR account")
    @PostMapping("/create-hr")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createHr(@Valid @RequestBody RegisterRequestDTO dto) {

        String adminEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createHr(dto, adminEmail));
    }

    //  GET  /api/admin/users   → Admin views all users
    @Operation(summary = "Get all users")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
