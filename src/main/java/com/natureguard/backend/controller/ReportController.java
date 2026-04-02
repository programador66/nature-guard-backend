package com.natureguard.backend.controller;

import com.natureguard.backend.domain.dto.ReportRequestDTO;
import com.natureguard.backend.domain.dto.ReportResponseDTO;
import com.natureguard.backend.service.ReportService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
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
    public List<ReportResponseDTO> findAll() {
        return service.findAll();
    }
}