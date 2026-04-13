package com.natureguard.backend.controller;

import com.natureguard.backend.domain.dto.AuthResponseDTO;
import com.natureguard.backend.domain.dto.LoginRequestDTO;
import com.natureguard.backend.domain.dto.RegisterRequestDTO;
import com.natureguard.backend.jwt.JwtAuthenticationFilter;
import com.natureguard.backend.jwt.JwtService;
import com.natureguard.backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private AuthResponseDTO sampleResponse() {
        return AuthResponseDTO.builder()
                .id(1L)
                .name("João Silva")
                .email("joao@email.com")
                .autonomousMode(false)
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .token("jwt-token")
                .build();
    }

    @Nested
    @DisplayName("POST /auth/register")
    class Register {

        @Test
        @DisplayName("deve registrar com sucesso e retornar 200")
        void shouldRegisterSuccessfully() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO();
            request.setName("João Silva");
            request.setEmail("joao@email.com");
            request.setPassword("123456");
            request.setConfirmationPassword("123456");
            request.setAutonomousMode(false);

            when(authService.register(any(RegisterRequestDTO.class))).thenReturn(sampleResponse());

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("João Silva"))
                    .andExpect(jsonPath("$.email").value("joao@email.com"))
                    .andExpect(jsonPath("$.token").value("jwt-token"));

            verify(authService).register(any(RegisterRequestDTO.class));
        }

        @Test
        @DisplayName("deve retornar 400 quando nome é vazio")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO();
            request.setName("");
            request.setEmail("joao@email.com");
            request.setPassword("123456");
            request.setConfirmationPassword("123456");
            request.setAutonomousMode(false);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("deve retornar 400 quando email é inválido")
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO();
            request.setName("João Silva");
            request.setEmail("invalido");
            request.setPassword("123456");
            request.setConfirmationPassword("123456");
            request.setAutonomousMode(false);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("deve retornar 400 quando senha tem menos de 6 caracteres")
        void shouldReturn400WhenPasswordIsTooShort() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO();
            request.setName("João Silva");
            request.setEmail("joao@email.com");
            request.setPassword("123");
            request.setConfirmationPassword("123");
            request.setAutonomousMode(false);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("deve retornar 400 quando autonomousMode é null")
        void shouldReturn400WhenAutonomousModeIsNull() throws Exception {
            String json = """
                    {
                        "name": "João Silva",
                        "email": "joao@email.com",
                        "password": "123456",
                        "confirmationPassword": "123456"
                    }
                    """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("deve retornar 400 quando senhas não coincidem (via service)")
        void shouldReturn400WhenPasswordsMismatch() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO();
            request.setName("João Silva");
            request.setEmail("joao@email.com");
            request.setPassword("123456");
            request.setConfirmationPassword("654321");
            request.setAutonomousMode(false);

            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenThrow(new RuntimeException("As senhas não coincidem"));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("As senhas não coincidem"));
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("deve fazer login com sucesso e retornar 200")
        void shouldLoginSuccessfully() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("joao@email.com");
            request.setPassword("123456");

            when(authService.login(any(LoginRequestDTO.class))).thenReturn(sampleResponse());

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("João Silva"))
                    .andExpect(jsonPath("$.email").value("joao@email.com"))
                    .andExpect(jsonPath("$.token").value("jwt-token"));

            verify(authService).login(any(LoginRequestDTO.class));
        }

        @Test
        @DisplayName("deve retornar 400 quando email é vazio")
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("");
            request.setPassword("123456");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any());
        }

        @Test
        @DisplayName("deve retornar 400 quando senha é vazia")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("joao@email.com");
            request.setPassword("");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any());
        }

        @Test
        @DisplayName("deve retornar 400 quando credenciais inválidas")
        void shouldReturn400WhenCredentialsAreInvalid() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("joao@email.com");
            request.setPassword("senhaerrada");

            when(authService.login(any(LoginRequestDTO.class)))
                    .thenThrow(new RuntimeException("Credenciais inválidas"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
        }
    }
}

