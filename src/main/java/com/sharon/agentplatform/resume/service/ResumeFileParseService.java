package com.sharon.agentplatform.resume.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.resume.dto.ResumeFileParseResponse;
import com.sharon.agentplatform.resume.parser.ResumeFileParser;
import com.sharon.agentplatform.resume.persistence.entity.ResumeFileEntity;
import com.sharon.agentplatform.resume.persistence.repository.ResumeFileRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResumeFileParseService {

    private final ResumeFileRepository resumeFileRepository;
    private final List<ResumeFileParser> resumeFileParsers;

    public ResumeFileParseService(
            ResumeFileRepository resumeFileRepository,
            List<ResumeFileParser> resumeFileParsers
    ) {
        this.resumeFileRepository = resumeFileRepository;
        this.resumeFileParsers = resumeFileParsers;
    }

    public ResumeFileParseResponse parse(String fileId) {
        ResumeFileEntity entity = resumeFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new BusinessException("Resume file not found: " + fileId));

        Path filePath = Path.of(entity.getStoragePath());
        if (!Files.exists(filePath)) {
            throw new BusinessException("Resume file does not exist: " + fileId);
        }

        String fileType = entity.getFileType();
        ResumeFileParser parser = resumeFileParsers.stream()
                .filter(candidate -> candidate.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Unsupported resume file type: " + fileType));

        String parsedText = parser.parse(filePath);
        if (parsedText == null || parsedText.isBlank()) {
            throw new BusinessException("Parsed resume text is empty");
        }

        LocalDateTime now = LocalDateTime.now();
        entity.setParsedText(parsedText);
        entity.setUpdatedAt(now);

        ResumeFileEntity savedEntity = resumeFileRepository.save(entity);
        return toResponse(savedEntity);
    }

    private ResumeFileParseResponse toResponse(ResumeFileEntity entity) {
        String parsedText = entity.getParsedText();
        ResumeFileParseResponse response = new ResumeFileParseResponse();
        response.setFileId(entity.getFileId());
        response.setOriginalFileName(entity.getOriginalFileName());
        response.setFileType(entity.getFileType());
        response.setTextLength(parsedText.length());
        response.setPreview(createPreview(parsedText));
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private String createPreview(String parsedText) {
        int previewLength = Math.min(parsedText.length(), 500);
        return parsedText.substring(0, previewLength);
    }
}
