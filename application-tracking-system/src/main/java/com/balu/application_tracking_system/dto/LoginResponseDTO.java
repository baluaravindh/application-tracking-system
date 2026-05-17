package com.balu.application_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

    public Long id;
    public String fullName;
    public String email;
    public String phone;
    public String role;
    private String token;
    private String tokenType;
    private String refreshToken;
}
