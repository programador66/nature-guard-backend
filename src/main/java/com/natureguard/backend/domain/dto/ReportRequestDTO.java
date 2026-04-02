package com.natureguard.backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
public class ReportRequestDTO {
    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotEmpty(message = "Pelo menos uma tag é obrigatória")
    private List<String> tags;

    @NotNull(message = "Latitude é obrigatória")
    private Double lat;

    @NotNull(message = "Longitude é obrigatória")
    private Double lng;

    @NotBlank(message = "Endereço é obrigatório")
    private String address;
}
