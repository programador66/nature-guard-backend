package com.natureguard.backend.domain.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String name;
    private String email;
    private String password;
    private String confirmationPassword;
    private Boolean isAutonomousMode;
}

