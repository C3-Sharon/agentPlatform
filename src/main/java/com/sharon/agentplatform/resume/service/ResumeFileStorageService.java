package com.sharon.agentplatform.resume.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.resume.dto.ResumeFileUploadResponse;
import com.sharon.agentplatform.resume.persistence.entity.ResumeFileEntity;
import com.sharon.agentplatform.resume.persistence.repository.ResumeFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class ResumeFileStorageService {

    private static final Path STORAGE_DIR = Path.of("data", "resume", "files")
            .toAbsolutePath()
            .normalize();

    private final ResumeFileRepository resumeFileRepository;

    public ResumeFileStorageService(ResumeFileRepository resumeFileRepository) {
        this.resumeFileRepository = resumeFileRepository;
    }

    public ResumeFileUploadResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Resume file is empty");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException("Resume file name is empty");
        }

        String fileType = getSupportedFileType(originalFileName);
        String fileId = UUID.randomUUID().toString();
        String storedFileName = fileId + "." + fileType;
        Path targetPath = STORAGE_DIR.resolve(storedFileName).normalize();

        try {
            Files.createDirectories(STORAGE_DIR);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            LocalDateTime now = LocalDateTime.now();
            ResumeFileEntity entity = new ResumeFileEntity();
            entity.setFileId(fileId);
            entity.setOriginalFileName(originalFileName);
            entity.setFileType(fileType);
            entity.setStoragePath(targetPath.toString());
            entity.setParsedText(null);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);

            ResumeFileEntity savedEntity = resumeFileRepository.save(entity);
            return toResponse(savedEntity);
        } catch (IOException | RuntimeException exception) {
            throw new BusinessException("Failed to store resume file", exception);
        }
    }

    private String getSupportedFileType(String originalFileName) {
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFileName.length() - 1) {
            throw new BusinessException("Only pdf and docx files are supported");
        }

        String fileType = originalFileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!"pdf".equals(fileType) && !"docx".equals(fileType)) {
            throw new BusinessException("Only pdf and docx files are supported");
        }

        return fileType;
    }

    private ResumeFileUploadResponse toResponse(ResumeFileEntity entity) {
        ResumeFileUploadResponse response = new ResumeFileUploadResponse();
        response.setFileId(entity.getFileId());
        response.setOriginalFileName(entity.getOriginalFileName());
        response.setFileType(entity.getFileType());
        response.setStoragePath(entity.getStoragePath());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
