package com.natureguard.backend.controller;

import com.natureguard.backend.domain.dto.ReportRequestDTO;
import com.natureguard.backend.domain.dto.ReportResponseDTO;
import com.natureguard.backend.service.ReportService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;
    private final JsonMapper jsonMapper;
    private final Validator validator;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReportResponseDTO create(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {
        ReportRequestDTO dto = jsonMapper.readValue(dataJson, ReportRequestDTO.class);

        Set<ConstraintViolation<ReportRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new RuntimeException(errors);
        }

        String userId = authentication.getName();
        return service.create(dto, userId, images);
    }

    @GetMapping
    public Page<ReportResponseDTO> findAll(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String search) {
        return service.findAll(pageable, tags, startDate, endDate, search);
    }

    @GetMapping("/my-reports")
    public List<ReportResponseDTO> findMyReports(Authentication authentication) {
        String userId = authentication.getName();
        return service.findByUserId(userId);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReportResponseDTO update(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {
        ReportRequestDTO dto = jsonMapper.readValue(dataJson, ReportRequestDTO.class);

        Set<ConstraintViolation<ReportRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new RuntimeException(errors);
        }

        String userId = authentication.getName();
        return service.update(id, dto, userId, images);
    }
}