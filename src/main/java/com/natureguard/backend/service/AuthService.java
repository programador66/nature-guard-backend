package com.natureguard.backend.service;

import com.natureguard.backend.domain.dto.AuthResponseDTO;
import com.natureguard.backend.domain.dto.LoginRequestDTO;
import com.natureguard.backend.domain.dto.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}
