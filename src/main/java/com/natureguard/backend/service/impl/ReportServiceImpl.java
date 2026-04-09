package com.natureguard.backend.service.impl;

import com.natureguard.backend.domain.dto.ReportRequestDTO;
import com.natureguard.backend.domain.dto.ReportResponseDTO;
import com.natureguard.backend.domain.model.Report;
import com.natureguard.backend.domain.model.User;
import com.natureguard.backend.mapper.ReportMapper;
import com.natureguard.backend.repository.ReportRepository;
import com.natureguard.backend.repository.UserRepository;
import com.natureguard.backend.repository.specification.ReportSpecification;
import com.natureguard.backend.service.FileStorageService;
import com.natureguard.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    public Page<ReportResponseDTO> findAll(Pageable pageable, List<String> tags, LocalDateTime startDate, LocalDateTime endDate, String search) {
        log.info("Fetching reports page: {}, size: {}, tags: {}, startDate: {}, endDate: {}, search: {}",
                pageable.getPageNumber(), pageable.getPageSize(), tags, startDate, endDate, search);

        List<String> matchingUserEmails = Collections.emptyList();
        if (search != null && !search.isBlank()) {
            matchingUserEmails = userRepository.findByNameContainingIgnoreCase(search).stream()
                    .map(User::getEmail)
                    .toList();
        }

        Specification<Report> spec = ReportSpecification.withFilters(tags, startDate, endDate, search, matchingUserEmails);
        Page<Report> reports = reportRepository.findAll(spec, pageable);

        List<String> emails = reports.getContent().stream()
                .map(Report::getUserId)
                .distinct()
                .toList();

        Map<String, User> userMap = userRepository.findAllByEmailIn(emails).stream()
                .collect(Collectors.toMap(User::getEmail, Function.identity()));

        return reports.map(report -> ReportMapper.toDTO(report, userMap.get(report.getUserId())));
    }

    @Override
    public List<ReportResponseDTO> findByUserId(String userId) {
        log.info("Fetching reports for user: {}", userId);

        List<Report> reports = reportRepository.findByUserId(userId);
        User user = userRepository.findByEmail(userId).orElse(null);

        return reports.stream()
                .map(report -> ReportMapper.toDTO(report, user))
                .toList();
    }

    @Override
    public ReportResponseDTO update(Long id, ReportRequestDTO dto, String userId, List<MultipartFile> images) {
        log.info("Updating report id: {} by user: {}", id, userId);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report não encontrado"));

        if (!report.getUserId().equals(userId)) {
            throw new RuntimeException("Você não tem permissão para editar este report");
        }

        List<String> newImageUrls = (images != null && !images.isEmpty())
                ? fileStorageService.storeAll(images)
                : Collections.emptyList();

        ReportMapper.updateEntity(report, dto, newImageUrls);
        Report saved = reportRepository.save(report);

        User user = userRepository.findByEmail(userId).orElse(null);
        return ReportMapper.toDTO(saved, user);
    }
}
