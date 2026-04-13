package com.natureguard.backend.domain.dto;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponseDTO {
    private Long id;
    private String name;
    private String email;

    @JsonProperty("isAutonomousMode")
    private Boolean autonomousMode;

    private LocalDateTime createdAt;
    private String token;
}
