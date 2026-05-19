package com.sharon.agentplatform.resume.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.resume.dto.ResumeFileUploadResponse;
import com.sharon.agentplatform.resume.service.ResumeFileStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume/files")
public class ResumeFileController {

    private final ResumeFileStorageService resumeFileStorageService;

    public ResumeFileController(ResumeFileStorageService resumeFileStorageService) {
        this.resumeFileStorageService = resumeFileStorageService;
    }

    @PostMapping
    public ApiResponse<ResumeFileUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(resumeFileStorageService.store(file));
    }
}
