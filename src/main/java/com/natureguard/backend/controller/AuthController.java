package com.natureguard.backend.controller;

import com.natureguard.backend.domain.dto.AuthResponseDTO;
import com.natureguard.backend.domain.dto.LoginRequestDTO;
import com.natureguard.backend.domain.dto.RegisterRequestDTO;
import com.natureguard.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return service.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return service.login(request);
    }
}
