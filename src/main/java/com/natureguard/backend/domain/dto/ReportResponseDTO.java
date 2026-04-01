package com.natureguard.backend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReportResponseDTO {
    private Long id;
    private String title;
    private String description;
    private List<String> tags;

    private Double lat;
    private Double lng;
    private String address;

    private LocalDateTime createdAt;
    private String userName;
    private List<String> images;
}
