package com.natureguard.backend.controller;

import com.natureguard.backend.domain.dto.ReportResponseDTO;
import com.natureguard.backend.jwt.JwtAuthenticationFilter;
import com.natureguard.backend.jwt.JwtService;
import com.natureguard.backend.service.ReportService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.natureguard.backend.config.SecurityBeansConfig;
import com.natureguard.backend.config.SecurityConfig;
import com.natureguard.backend.exceptions.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import({SecurityConfig.class, SecurityBeansConfig.class, GlobalExceptionHandler.class})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ReportResponseDTO sampleResponse;

    @BeforeEach
    void setUp() throws Exception {
        sampleResponse = ReportResponseDTO.builder()
                .id(1L)
                .title("Queimada forte")
                .description("Fogo grande na mata")
                .tags(List.of("QUEIMADA"))
                .lat(-26.3)
                .lng(-48.8)
                .address("Joinville")
                .createdAt(LocalDateTime.of(2026, 4, 1, 12, 0))
                .userName("João Silva")
                .images(List.of("/uploads/img1.jpg"))
                .build();

        // Configura o filtro JWT mockado para simplesmente passar a requisição adiante
        lenient().doAnswer(invocation -> {
            HttpServletRequest req = invocation.getArgument(0);
            HttpServletResponse resp = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, resp);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any(FilterChain.class)
        );
    }

    @Nested
    @DisplayName("POST /reports")
    class Create {

        @Test
        @DisplayName("deve criar report com dados e imagens via multipart")
        void shouldCreateReportWithMultipart() throws Exception {
            String dataJson = """
                    {
                        "title": "Queimada forte",
                        "description": "Fogo grande na mata",
                        "tags": ["QUEIMADA"],
                        "lat": -26.3,
                        "lng": -48.8,
                        "address": "Joinville"
                    }
                    """;

            MockMultipartFile dataPart = new MockMultipartFile(
                    "data", "", "application/json", dataJson.getBytes());
            MockMultipartFile imagePart = new MockMultipartFile(
                    "images", "foto.jpg", "image/jpeg", "fake-image-data".getBytes());

            when(reportService.create(any(), eq("joao@email.com"), anyList())).thenReturn(sampleResponse);

            mockMvc.perform(multipart("/reports")
                            .file(dataPart)
                            .file(imagePart)
                            .with(user("joao@email.com"))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Queimada forte"))
                    .andExpect(jsonPath("$.userName").value("João Silva"))
                    .andExpect(jsonPath("$.lat").value(-26.3))
                    .andExpect(jsonPath("$.lng").value(-48.8));

            verify(reportService).create(any(), eq("joao@email.com"), anyList());
        }

        @Test
        @DisplayName("deve criar report sem imagens")
        void shouldCreateReportWithoutImages() throws Exception {
            String dataJson = """
                    {
                        "title": "Queimada forte",
                        "description": "Fogo grande na mata",
                        "tags": ["QUEIMADA"],
                        "lat": -26.3,
                        "lng": -48.8,
                        "address": "Joinville"
                    }
                    """;

            MockMultipartFile dataPart = new MockMultipartFile(
                    "data", "", "application/json", dataJson.getBytes());

            when(reportService.create(any(), eq("joao@email.com"), isNull())).thenReturn(sampleResponse);

            mockMvc.perform(multipart("/reports")
                            .file(dataPart)
                            .with(user("joao@email.com"))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar erro quando título está vazio")
        void shouldReturnErrorWhenTitleIsBlank() throws Exception {
            String dataJson = """
                    {
                        "title": "",
                        "description": "Fogo grande na mata",
                        "tags": ["QUEIMADA"],
                        "lat": -26.3,
                        "lng": -48.8,
                        "address": "Joinville"
                    }
                    """;

            MockMultipartFile dataPart = new MockMultipartFile(
                    "data", "", "application/json", dataJson.getBytes());

            mockMvc.perform(multipart("/reports")
                            .file(dataPart)
                            .with(user("joao@email.com"))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar erro quando tags estão vazias")
        void shouldReturnErrorWhenTagsAreEmpty() throws Exception {
            String dataJson = """
                    {
                        "title": "Queimada forte",
                        "description": "Fogo grande na mata",
                        "tags": [],
                        "lat": -26.3,
                        "lng": -48.8,
                        "address": "Joinville"
                    }
                    """;

            MockMultipartFile dataPart = new MockMultipartFile(
                    "data", "", "application/json", dataJson.getBytes());

            mockMvc.perform(multipart("/reports")
                            .file(dataPart)
                            .with(user("joao@email.com"))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /reports")
    class FindAll {

        @Test
        @DisplayName("deve retornar página de reports")
        void shouldReturnPageOfReports() throws Exception {
            Page<ReportResponseDTO> page = new PageImpl<>(
                    List.of(sampleResponse), PageRequest.of(0, 10), 1);

            when(reportService.findAll(any(), isNull(), isNull(), isNull(), isNull())).thenReturn(page);

            mockMvc.perform(get("/reports")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Queimada forte"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("deve retornar página vazia quando não há reports")
        void shouldReturnEmptyPage() throws Exception {
            Page<ReportResponseDTO> emptyPage = new PageImpl<>(
                    Collections.emptyList(), PageRequest.of(0, 10), 0);

            when(reportService.findAll(any(), isNull(), isNull(), isNull(), isNull())).thenReturn(emptyPage);

            mockMvc.perform(get("/reports")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("deve filtrar por tags")
        void shouldFilterByTags() throws Exception {
            Page<ReportResponseDTO> page = new PageImpl<>(
                    List.of(sampleResponse), PageRequest.of(0, 10), 1);

            when(reportService.findAll(any(), eq(List.of("QUEIMADA")), isNull(), isNull(), isNull()))
                    .thenReturn(page);

            mockMvc.perform(get("/reports")
                            .param("tags", "QUEIMADA"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].tags[0]").value("QUEIMADA"));
        }

        @Test
        @DisplayName("deve filtrar por busca textual")
        void shouldFilterBySearch() throws Exception {
            Page<ReportResponseDTO> page = new PageImpl<>(
                    List.of(sampleResponse), PageRequest.of(0, 10), 1);

            when(reportService.findAll(any(), isNull(), isNull(), isNull(), eq("queimada")))
                    .thenReturn(page);

            mockMvc.perform(get("/reports")
                            .param("search", "queimada"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("GET /reports/my-reports")
    class FindMyReports {

        @Test
        @DisplayName("deve retornar reports do usuário autenticado")
        void shouldReturnUserReports() throws Exception {
            when(reportService.findByUserId("joao@email.com")).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/reports/my-reports")
                            .with(user("joao@email.com")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Queimada forte"));

            verify(reportService).findByUserId("joao@email.com");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando usuário não tem reports")
        void shouldReturnEmptyListWhenNoReports() throws Exception {
            when(reportService.findByUserId("joao@email.com")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/reports/my-reports")
                            .with(user("joao@email.com")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("PUT /reports/{id}")
    class Update {

        @Test
        @DisplayName("deve atualizar report com sucesso")
        void shouldUpdateReportSuccessfully() throws Exception {
            String dataJson = """
                    {
                        "title": "Título atualizado",
                        "description": "Descrição atualizada",
                        "tags": ["DESMATAMENTO"],
                        "lat": -26.5,
                        "lng": -49.0,
                        "address": "Florianópolis"
                    }
                    """;

            ReportResponseDTO updatedResponse = ReportResponseDTO.builder()
                    .id(1L)
                    .title("Título atualizado")
                    .description("Descrição atualizada")
                    .tags(List.of("DESMATAMENTO"))
                    .lat(-26.5)
                    .lng(-49.0)
                    .address("Florianópolis")
                    .createdAt(LocalDateTime.of(2026, 4, 1, 12, 0))
                    .userName("João Silva")
                    .images(List.of("/uploads/img1.jpg"))
                    .build();

            MockMultipartFile dataPart = new MockMultipartFile(
                    "data", "", "application/json", dataJson.getBytes());

            when(reportService.update(eq(1L), any(), eq("joao@email.com"), isNull()))
                    .thenReturn(updatedResponse);

            mockMvc.perform(multipart("/reports/1")
                            .file(dataPart)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .with(user("joao@email.com"))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Título atualizado"))
                    .andExpect(jsonPath("$.description").value("Descrição atualizada"));
        }

        @Test
        @DisplayName("deve retornar erro quando report não pertence ao usuário")
        void shouldReturnErrorWhenNotOwner() throws Exception {
            String dataJson = """
                    {
                        "title": "Título",
                        "description": "Descrição",
                        "tags": ["QUEIMADA"],
                        "lat": -26.3,
                        "lng": -48.8,
                        "address": "Joinville"
                    }
                    """;

            MockMultipartFile dataPart = new MockMultipartFile(
                    "data", "", "application/json", dataJson.getBytes());

            when(reportService.update(eq(1L), any(), eq("joao@email.com"), isNull()))
                    .thenThrow(new RuntimeException("Você não tem permissão para editar este report"));

            mockMvc.perform(multipart("/reports/1")
                            .file(dataPart)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .with(user("joao@email.com"))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Você não tem permissão para editar este report"));
        }

        @Test
        @DisplayName("deve retornar erro quando report não encontrado")
        void shouldReturnErrorWhenReportNotFound() throws Exception {
            String dataJson = """
                    {
                        "title": "Título",
                        "description": "Descrição",
                        "tags": ["QUEIMADA"],
                        "lat": -26.3,
                        "lng": -48.8,
                        "address": "Joinville"
                    }
                    """;

            MockMultipartFile dataPart = new MockMultipartFile(
                    "data", "", "application/json", dataJson.getBytes());

            when(reportService.update(eq(99L), any(), eq("joao@email.com"), isNull()))
                    .thenThrow(new RuntimeException("Report não encontrado"));

            mockMvc.perform(multipart("/reports/99")
                            .file(dataPart)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .with(user("joao@email.com"))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Report não encontrado"));
        }
    }
}

