package com.sharon.agentplatform.resume.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ResumeOptimizeAsyncExecutor {

    private final ResumeOptimizeTaskRunner resumeOptimizeTaskRunner;

    public ResumeOptimizeAsyncExecutor(ResumeOptimizeTaskRunner resumeOptimizeTaskRunner) {
        this.resumeOptimizeTaskRunner = resumeOptimizeTaskRunner;
    }

    @Async("resumeTaskExecutor")
    public void execute(Long taskId) {
        try {
            resumeOptimizeTaskRunner.runExistingTask(taskId);
        } catch (RuntimeException ignored) {
            // The runner has already persisted FAILED status and a readable error message.
        }
    }
}
