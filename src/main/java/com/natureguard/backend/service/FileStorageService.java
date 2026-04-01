package com.natureguard.backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    String store(MultipartFile file);
    List<String> storeAll(List<MultipartFile> files);
}

