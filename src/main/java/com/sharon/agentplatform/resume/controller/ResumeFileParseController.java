package com.sharon.agentplatform.resume.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.resume.dto.ResumeFileParseResponse;
import com.sharon.agentplatform.resume.service.ResumeFileParseService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume/files")
public class ResumeFileParseController {

    private final ResumeFileParseService resumeFileParseService;

    public ResumeFileParseController(ResumeFileParseService resumeFileParseService) {
        this.resumeFileParseService = resumeFileParseService;
    }

    @PostMapping("/{fileId}/parse")
    public ApiResponse<ResumeFileParseResponse> parse(@PathVariable String fileId) {
        return ApiResponse.success(resumeFileParseService.parse(fileId));
    }
}
