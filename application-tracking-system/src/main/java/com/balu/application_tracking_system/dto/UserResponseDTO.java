package com.balu.application_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    public Long id;
    public String fullName;
    public String email;
    public String phone;
    public String role;
    public LocalDateTime createdAt;
}
