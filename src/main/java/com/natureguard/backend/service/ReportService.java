package com.natureguard.backend.service;

import com.natureguard.backend.domain.dto.ReportRequestDTO;
import com.natureguard.backend.domain.dto.ReportResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {

    ReportResponseDTO create(ReportRequestDTO dto, String userId, List<MultipartFile> images);

    Page<ReportResponseDTO> findAll(Pageable pageable, List<String> tags, LocalDateTime startDate, LocalDateTime endDate, String search);

    List<ReportResponseDTO> findByUserId(String userId);

    ReportResponseDTO update(Long id, ReportRequestDTO dto, String userId, List<MultipartFile> images);
}
