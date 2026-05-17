package com.balu.application_tracking_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email.")
    public String email;

    @NotBlank(message = "Password is required.")
    public String password;
}
