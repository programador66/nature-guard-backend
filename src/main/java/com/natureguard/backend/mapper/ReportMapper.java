package com.natureguard.backend.mapper;


import com.natureguard.backend.domain.dto.ReportRequestDTO;
import com.natureguard.backend.domain.dto.ReportResponseDTO;
import com.natureguard.backend.domain.model.Location;
import com.natureguard.backend.domain.model.Report;
import com.natureguard.backend.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ReportMapper {

    public static Report toEntity(ReportRequestDTO dto, String userId, List<String> imageUrls) {
        return Report.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .tags(dto.getTags())
                .location(Location.builder()
                        .lat(dto.getLat())
                        .lng(dto.getLng())
                        .address(dto.getAddress())
                        .build())
                .userId(userId)
                .images(imageUrls)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ReportResponseDTO toDTO(Report report, User user) {
        String userName = (user != null && Boolean.TRUE.equals(user.getIsAutonomousMode()))
                ? "Anônimo"
                : (user != null ? user.getName() : "Anônimo");

        return ReportResponseDTO.builder()
                .id(report.getId())
                .title(report.getTitle())
                .description(report.getDescription())
                .tags(report.getTags())
                .lat(report.getLocation().getLat())
                .lng(report.getLocation().getLng())
                .address(report.getLocation().getAddress())
                .createdAt(report.getCreatedAt())
                .userName(userName)
                .images(report.getImages())
                .build();
    }
}
