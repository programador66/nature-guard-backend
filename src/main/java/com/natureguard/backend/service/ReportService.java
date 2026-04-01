package com.natureguard.backend.service;

import com.natureguard.backend.domain.dto.ReportRequestDTO;
import com.natureguard.backend.domain.dto.ReportResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportService {

    ReportResponseDTO create(ReportRequestDTO dto, String userId, List<MultipartFile> images);

    List<ReportResponseDTO> findAll();
}
