package com.natureguard.backend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponseDTO {
    private Long id;
    private String name;
    private String email;
    private Boolean isAutonomousMode;
    private LocalDateTime createdAt;
    private String token;
}
