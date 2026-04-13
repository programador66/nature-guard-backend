package com.natureguard.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FileStorageServiceImplTest {

    private FileStorageServiceImpl fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImpl();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
        fileStorageService.init();
    }

    @Test
    @DisplayName("deve salvar arquivo e retornar URL com prefixo /uploads/")
    void shouldStoreFileAndReturnUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", "conteudo-fake".getBytes());

        String result = fileStorageService.store(file);

        assertThat(result).startsWith("/uploads/");
        assertThat(result).endsWith(".jpg");

        // Verifica se o arquivo foi realmente salvo
        String filename = result.replace("/uploads/", "");
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }

    @Test
    @DisplayName("deve salvar arquivo sem extensão")
    void shouldStoreFileWithoutExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "arquivo", "application/octet-stream", "data".getBytes());

        String result = fileStorageService.store(file);

        assertThat(result).startsWith("/uploads/");
        assertThat(result).doesNotContain(".");
    }

    @Test
    @DisplayName("deve salvar múltiplos arquivos")
    void shouldStoreMultipleFiles() {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "foto1.jpg", "image/jpeg", "data1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "foto2.png", "image/png", "data2".getBytes());

        List<String> results = fileStorageService.storeAll(List.of(file1, file2));

        assertThat(results).hasSize(2);
        assertThat(results.get(0)).startsWith("/uploads/").endsWith(".jpg");
        assertThat(results.get(1)).startsWith("/uploads/").endsWith(".png");
    }

    @Test
    @DisplayName("deve gerar nomes únicos para cada arquivo")
    void shouldGenerateUniqueFilenames() {
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", "data1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", "data2".getBytes());

        String result1 = fileStorageService.store(file1);
        String result2 = fileStorageService.store(file2);

        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("deve lançar exceção quando diretório de uploads é inválido")
    void shouldThrowWhenUploadDirIsInvalid() throws IOException {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        // Define um path dentro de um arquivo (impossível criar diretório)
        Path invalidPath = tempDir.resolve("file.txt");
        Files.writeString(invalidPath, "not a directory");
        ReflectionTestUtils.setField(service, "uploadDir", invalidPath.resolve("subdir").toString());

        assertThatThrownBy(service::init)
                .isInstanceOf(RuntimeException.class);
    }
}

