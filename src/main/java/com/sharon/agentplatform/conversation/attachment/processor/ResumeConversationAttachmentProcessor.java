package com.sharon.agentplatform.conversation.attachment.processor;

import com.sharon.agentplatform.conversation.attachment.core.ConversationAttachmentProcessor;
import com.sharon.agentplatform.conversation.resource.dto.ConversationAttachmentUploadResponse;
import com.sharon.agentplatform.conversation.resource.dto.ConversationResourceResponse;
import com.sharon.agentplatform.conversation.resource.service.ConversationResourceService;
import com.sharon.agentplatform.resume.dto.ResumeFileUploadResponse;
import com.sharon.agentplatform.resume.service.ResumeFileStorageService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;

@Component
public class ResumeConversationAttachmentProcessor implements ConversationAttachmentProcessor {

    private static final String PURPOSE_RESUME = "resume";
    private static final String RESOURCE_TYPE_RESUME_FILE = "resume_file";

    private final ResumeFileStorageService resumeFileStorageService;
    private final ConversationResourceService conversationResourceService;

    public ResumeConversationAttachmentProcessor(ResumeFileStorageService resumeFileStorageService,
                                                 ConversationResourceService conversationResourceService) {
        this.resumeFileStorageService = resumeFileStorageService;
        this.conversationResourceService = conversationResourceService;
    }

    @Override
    public boolean supports(String purpose, MultipartFile file) {
        return PURPOSE_RESUME.equals(normalizePurpose(purpose)) && isPdfOrDocx(file);
    }

    @Override
    public ConversationAttachmentUploadResponse process(String conversationId, String purpose, MultipartFile file) {
        ResumeFileUploadResponse uploadResponse = resumeFileStorageService.store(file);
        Map<String, Object> metadata = Map.of(
                "originalFileName", uploadResponse.getOriginalFileName(),
                "fileType", uploadResponse.getFileType(),
                "storagePath", uploadResponse.getStoragePath(),
                "resumeFileId", uploadResponse.getFileId()
        );

        ConversationResourceResponse resource = conversationResourceService.createResource(
                conversationId,
                RESOURCE_TYPE_RESUME_FILE,
                uploadResponse.getFileId(),
                normalizePurpose(purpose),
                uploadResponse.getOriginalFileName(),
                file.getContentType(),
                file.getSize(),
                metadata
        );

        ConversationAttachmentUploadResponse response = new ConversationAttachmentUploadResponse();
        response.setConversationId(resource.getConversationId());
        response.setResourceType(resource.getResourceType());
        response.setResourceId(resource.getResourceId());
        response.setPurpose(resource.getPurpose());
        response.setDisplayName(resource.getDisplayName());
        response.setMimeType(resource.getMimeType());
        response.setSizeBytes(resource.getSizeBytes());
        response.setMetadata(resource.getMetadata());
        response.setCreatedAt(resource.getCreatedAt());
        return response;
    }

    private String normalizePurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            return PURPOSE_RESUME;
        }
        return purpose.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isPdfOrDocx(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return false;
        }

        String lowerName = originalFilename.toLowerCase(Locale.ROOT);
        return lowerName.endsWith(".pdf") || lowerName.endsWith(".docx");
    }
}
