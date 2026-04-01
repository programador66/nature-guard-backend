package com.natureguard.backend.service.impl;

import com.natureguard.backend.domain.dto.ReportRequestDTO;
import com.natureguard.backend.domain.dto.ReportResponseDTO;
import com.natureguard.backend.domain.model.Report;
import com.natureguard.backend.domain.model.User;
import com.natureguard.backend.mapper.ReportMapper;
import com.natureguard.backend.repository.ReportRepository;
import com.natureguard.backend.repository.UserRepository;
import com.natureguard.backend.service.FileStorageService;
import com.natureguard.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    public ReportResponseDTO create(ReportRequestDTO dto, String userId, List<MultipartFile> images) {
        log.info("Creating report with data: {}", dto);

        List<String> imageUrls = (images != null && !images.isEmpty())
                ? fileStorageService.storeAll(images)
                : Collections.emptyList();

        Report report = ReportMapper.toEntity(dto, userId, imageUrls);
        Report saved = reportRepository.save(report);

        User user = userRepository.findByEmail(userId).orElse(null);
        return ReportMapper.toDTO(saved, user);
    }

    @Override
    public List<ReportResponseDTO> findAll() {
        log.info("Fetching all reports");

        List<Report> reports = reportRepository.findAll();

        List<String> emails = reports.stream()
                .map(Report::getUserId)
                .distinct()
                .toList();

        Map<String, User> userMap = userRepository.findAllByEmailIn(emails).stream()
                .collect(Collectors.toMap(User::getEmail, Function.identity()));

        return reports.stream()
                .map(report -> ReportMapper.toDTO(report, userMap.get(report.getUserId())))
                .toList();
    }
}
