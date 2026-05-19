package com.sharon.agentplatform.resume.parser;

import java.nio.file.Path;

public interface ResumeFileParser {

    boolean supports(String fileType);

    String parse(Path filePath);
}
