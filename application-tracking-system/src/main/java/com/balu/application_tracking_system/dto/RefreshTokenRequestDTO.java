package com.balu.application_tracking_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token is required.")
    private String refreshToken;
}
