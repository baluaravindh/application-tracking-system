package com.balu.application_tracking_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, message = "Name must between 2 to 100 characters.")
    public String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    public String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be 6 characters.")
    public String password;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Please enter a valid phone number.")
    public String phone;
}
