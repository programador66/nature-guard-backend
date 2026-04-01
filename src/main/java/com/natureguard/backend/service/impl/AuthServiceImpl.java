package com.natureguard.backend.service.impl;

import com.natureguard.backend.domain.dto.AuthResponseDTO;
import com.natureguard.backend.domain.dto.LoginRequestDTO;
import com.natureguard.backend.domain.dto.RegisterRequestDTO;
import com.natureguard.backend.domain.model.User;
import com.natureguard.backend.jwt.JwtService;
import com.natureguard.backend.repository.UserRepository;
import com.natureguard.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmationPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setIsAutonomousMode(request.getIsAutonomousMode());
        user.setCreatedAt(LocalDateTime.now());

        User saved = repository.save(user);
        String token = jwtService.generateToken(saved.getEmail());

        return toAuthResponse(saved, token);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return toAuthResponse(user, token);
    }

    private AuthResponseDTO toAuthResponse(User user, String token) {
        return AuthResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .isAutonomousMode(user.getIsAutonomousMode())
                .createdAt(user.getCreatedAt())
                .token(token)
                .build();
    }
}
