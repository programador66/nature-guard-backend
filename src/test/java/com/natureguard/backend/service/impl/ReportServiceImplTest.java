package com.natureguard.backend.service.impl;

import com.natureguard.backend.domain.dto.ReportRequestDTO;
import com.natureguard.backend.domain.dto.ReportResponseDTO;
import com.natureguard.backend.domain.model.Location;
import com.natureguard.backend.domain.model.Report;
import com.natureguard.backend.domain.model.User;
import com.natureguard.backend.repository.ReportRepository;
import com.natureguard.backend.repository.UserRepository;
import com.natureguard.backend.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User sampleUser;
    private Report sampleReport;
    private ReportRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("João Silva");
        sampleUser.setEmail("joao@email.com");
        sampleUser.setPassword("encoded");
        sampleUser.setAutonomousMode(false);
        sampleUser.setCreatedAt(LocalDateTime.of(2026, 4, 1, 10, 0));

        sampleReport = Report.builder()
                .id(1L)
                .title("Queimada forte")
                .description("Fogo grande na mata")
                .tags(List.of("QUEIMADA"))
                .location(Location.builder().lat(-26.3).lng(-48.8).address("Joinville").build())
                .images(new ArrayList<>(List.of("/uploads/img1.jpg")))
                .userId("joao@email.com")
                .createdAt(LocalDateTime.of(2026, 4, 1, 12, 0))
                .build();

        sampleRequest = new ReportRequestDTO();
        sampleRequest.setTitle("Queimada forte");
        sampleRequest.setDescription("Fogo grande na mata");
        sampleRequest.setTags(List.of("QUEIMADA"));
        sampleRequest.setLat(-26.3);
        sampleRequest.setLng(-48.8);
        sampleRequest.setAddress("Joinville");
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("deve criar report com imagens e retornar DTO")
        void shouldCreateReportWithImages() {
            List<MultipartFile> images = List.of(
                    new MockMultipartFile("images", "foto.jpg", "image/jpeg", "data".getBytes())
            );

            when(fileStorageService.storeAll(images)).thenReturn(List.of("/uploads/foto.jpg"));
            when(reportRepository.save(any(Report.class))).thenReturn(sampleReport);
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));

            ReportResponseDTO response = reportService.create(sampleRequest, "joao@email.com", images);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Queimada forte");
            assertThat(response.getUserName()).isEqualTo("João Silva");
            assertThat(response.getLat()).isEqualTo(-26.3);
            assertThat(response.getLng()).isEqualTo(-48.8);

            verify(fileStorageService).storeAll(images);
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("deve criar report sem imagens")
        void shouldCreateReportWithoutImages() {
            when(reportRepository.save(any(Report.class))).thenReturn(sampleReport);
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));

            ReportResponseDTO response = reportService.create(sampleRequest, "joao@email.com", null);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Queimada forte");

            verify(fileStorageService, never()).storeAll(any());
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("deve retornar 'Usuário anônimo' quando autonomousMode é true")
        void shouldReturnAnonymousUserWhenAutonomousMode() {
            sampleUser.setAutonomousMode(true);

            when(reportRepository.save(any(Report.class))).thenReturn(sampleReport);
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));

            ReportResponseDTO response = reportService.create(sampleRequest, "joao@email.com", null);

            assertThat(response.getUserName()).isEqualTo("Usuário anônimo");
        }

        @Test
        @DisplayName("deve retornar 'Usuário anônimo' quando usuário não encontrado")
        void shouldReturnAnonymousWhenUserNotFound() {
            when(reportRepository.save(any(Report.class))).thenReturn(sampleReport);
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

            ReportResponseDTO response = reportService.create(sampleRequest, "joao@email.com", null);

            assertThat(response.getUserName()).isEqualTo("Usuário anônimo");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("deve retornar página de reports sem filtros")
        void shouldReturnPageOfReportsWithoutFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Report> reportPage = new PageImpl<>(List.of(sampleReport), pageable, 1);

            when(reportRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(reportPage);
            when(userRepository.findAllByEmailIn(anyList())).thenReturn(List.of(sampleUser));

            Page<ReportResponseDTO> result = reportService.findAll(pageable, null, null, null, null);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Queimada forte");
            assertThat(result.getContent().get(0).getUserName()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("deve buscar por texto incluindo nomes de usuários")
        void shouldSearchByTextIncludingUserNames() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Report> reportPage = new PageImpl<>(List.of(sampleReport), pageable, 1);

            when(userRepository.findByNameContainingIgnoreCase("João")).thenReturn(List.of(sampleUser));
            when(reportRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(reportPage);
            when(userRepository.findAllByEmailIn(anyList())).thenReturn(List.of(sampleUser));

            Page<ReportResponseDTO> result = reportService.findAll(pageable, null, null, null, "João");

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(userRepository).findByNameContainingIgnoreCase("João");
        }

        @Test
        @DisplayName("deve retornar página vazia quando não há reports")
        void shouldReturnEmptyPageWhenNoReports() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Report> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(reportRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
            when(userRepository.findAllByEmailIn(anyList())).thenReturn(Collections.emptyList());

            Page<ReportResponseDTO> result = reportService.findAll(pageable, null, null, null, null);

            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("deve retornar lista de reports do usuário")
        void shouldReturnUserReports() {
            when(reportRepository.findByUserId("joao@email.com")).thenReturn(List.of(sampleReport));
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));

            List<ReportResponseDTO> result = reportService.findByUserId("joao@email.com");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Queimada forte");
            assertThat(result.get(0).getUserName()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há reports")
        void shouldReturnEmptyListWhenNoReports() {
            when(reportRepository.findByUserId("joao@email.com")).thenReturn(Collections.emptyList());
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));

            List<ReportResponseDTO> result = reportService.findByUserId("joao@email.com");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("deve atualizar report com sucesso")
        void shouldUpdateReportSuccessfully() {
            ReportRequestDTO updateRequest = new ReportRequestDTO();
            updateRequest.setTitle("Título atualizado");
            updateRequest.setDescription("Descrição atualizada");
            updateRequest.setTags(List.of("DESMATAMENTO"));
            updateRequest.setLat(-26.5);
            updateRequest.setLng(-49.0);
            updateRequest.setAddress("Florianópolis");

            Report updatedReport = Report.builder()
                    .id(1L)
                    .title("Título atualizado")
                    .description("Descrição atualizada")
                    .tags(List.of("DESMATAMENTO"))
                    .location(Location.builder().lat(-26.5).lng(-49.0).address("Florianópolis").build())
                    .images(new ArrayList<>(List.of("/uploads/img1.jpg")))
                    .userId("joao@email.com")
                    .createdAt(LocalDateTime.of(2026, 4, 1, 12, 0))
                    .build();

            when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
            when(reportRepository.save(any(Report.class))).thenReturn(updatedReport);
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));

            ReportResponseDTO response = reportService.update(1L, updateRequest, "joao@email.com", null);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Título atualizado");
            assertThat(response.getDescription()).isEqualTo("Descrição atualizada");

            verify(reportRepository).findById(1L);
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("deve atualizar report com novas imagens")
        void shouldUpdateReportWithNewImages() {
            List<MultipartFile> newImages = List.of(
                    new MockMultipartFile("images", "new.jpg", "image/jpeg", "data".getBytes())
            );

            when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
            when(fileStorageService.storeAll(newImages)).thenReturn(List.of("/uploads/new.jpg"));
            when(reportRepository.save(any(Report.class))).thenReturn(sampleReport);
            when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));

            ReportResponseDTO response = reportService.update(1L, sampleRequest, "joao@email.com", newImages);

            assertThat(response).isNotNull();
            verify(fileStorageService).storeAll(newImages);
        }

        @Test
        @DisplayName("deve lançar exceção quando report não encontrado")
        void shouldThrowWhenReportNotFound() {
            when(reportRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reportService.update(99L, sampleRequest, "joao@email.com", null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Report não encontrado");
        }

        @Test
        @DisplayName("deve lançar exceção quando usuário não é o dono do report")
        void shouldThrowWhenUserIsNotOwner() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));

            assertThatThrownBy(() -> reportService.update(1L, sampleRequest, "outro@email.com", null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Você não tem permissão para editar este report");

            verify(reportRepository, never()).save(any());
        }
    }
}

