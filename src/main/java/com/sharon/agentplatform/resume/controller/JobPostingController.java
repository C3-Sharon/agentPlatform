package com.sharon.agentplatform.resume.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.resume.dto.JobPostingReadRequest;
import com.sharon.agentplatform.resume.dto.JobPostingReadResponse;
import com.sharon.agentplatform.resume.service.JobPostingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume/job-postings")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @PostMapping("/read")
    public ApiResponse<JobPostingReadResponse> read(@RequestBody JobPostingReadRequest request) {
        return ApiResponse.success(jobPostingService.readAndSave(request));
    }
}
