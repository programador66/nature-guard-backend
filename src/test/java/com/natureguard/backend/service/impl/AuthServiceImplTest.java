package com.natureguard.backend.service.impl;

import com.natureguard.backend.domain.dto.AuthResponseDTO;
import com.natureguard.backend.domain.dto.LoginRequestDTO;
import com.natureguard.backend.domain.dto.RegisterRequestDTO;
import com.natureguard.backend.domain.model.User;
import com.natureguard.backend.jwt.JwtService;
import com.natureguard.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("João Silva");
        sampleUser.setEmail("joao@email.com");
        sampleUser.setPassword("encoded-password");
        sampleUser.setAutonomousMode(false);
        sampleUser.setCreatedAt(LocalDateTime.of(2026, 4, 1, 10, 0));
    }

    @Nested
    @DisplayName("register")
    class Register {

        private RegisterRequestDTO validRequest() {
            RegisterRequestDTO dto = new RegisterRequestDTO();
            dto.setName("João Silva");
            dto.setEmail("joao@email.com");
            dto.setPassword("123456");
            dto.setConfirmationPassword("123456");
            dto.setAutonomousMode(false);
            return dto;
        }

        @Test
        @DisplayName("deve registrar usuário com sucesso e retornar AuthResponseDTO")
        void shouldRegisterSuccessfully() {
            RegisterRequestDTO request = validRequest();

            when(repository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(encoder.encode(request.getPassword())).thenReturn("encoded-password");
            when(repository.save(any(User.class))).thenReturn(sampleUser);
            when(jwtService.generateToken(sampleUser.getEmail())).thenReturn("jwt-token");

            AuthResponseDTO response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("João Silva");
            assertThat(response.getEmail()).isEqualTo("joao@email.com");
            assertThat(response.getAutonomousMode()).isFalse();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getCreatedAt()).isNotNull();

            verify(repository).findByEmail(request.getEmail());
            verify(encoder).encode(request.getPassword());
            verify(repository).save(any(User.class));
            verify(jwtService).generateToken(sampleUser.getEmail());
        }

        @Test
        @DisplayName("deve lançar exceção quando senhas não coincidem")
        void shouldThrowWhenPasswordsDoNotMatch() {
            RegisterRequestDTO request = validRequest();
            request.setConfirmationPassword("diferente");

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("As senhas não coincidem");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando email já está cadastrado")
        void shouldThrowWhenEmailAlreadyExists() {
            RegisterRequestDTO request = validRequest();

            when(repository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Não foi possível completar o cadastro. Verifique os dados e tente novamente.");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        private LoginRequestDTO validLogin() {
            LoginRequestDTO dto = new LoginRequestDTO();
            dto.setEmail("joao@email.com");
            dto.setPassword("123456");
            return dto;
        }

        @Test
        @DisplayName("deve fazer login com sucesso e retornar AuthResponseDTO")
        void shouldLoginSuccessfully() {
            LoginRequestDTO request = validLogin();

            when(repository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleUser));
            when(encoder.matches(request.getPassword(), sampleUser.getPassword())).thenReturn(true);
            when(jwtService.generateToken(sampleUser.getEmail())).thenReturn("jwt-token");

            AuthResponseDTO response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("João Silva");
            assertThat(response.getEmail()).isEqualTo("joao@email.com");
            assertThat(response.getToken()).isEqualTo("jwt-token");

            verify(repository).findByEmail(request.getEmail());
            verify(encoder).matches(request.getPassword(), sampleUser.getPassword());
            verify(jwtService).generateToken(sampleUser.getEmail());
        }

        @Test
        @DisplayName("deve lançar exceção quando email não encontrado")
        void shouldThrowWhenEmailNotFound() {
            LoginRequestDTO request = validLogin();

            when(repository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Credenciais inválidas");

            verify(encoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("deve lançar exceção quando senha incorreta")
        void shouldThrowWhenPasswordIsWrong() {
            LoginRequestDTO request = validLogin();

            when(repository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleUser));
            when(encoder.matches(request.getPassword(), sampleUser.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Credenciais inválidas");

            verify(jwtService, never()).generateToken(anyString());
        }
    }
}

