package com.natureguard.backend.domain.dto;

import lombok.*;

import java.util.List;

@Data
public class ReportRequestDTO {
    private String title;
    private String description;
    private List<String> tags;

    private Double lat;
    private Double lng;
    private String address;
}
