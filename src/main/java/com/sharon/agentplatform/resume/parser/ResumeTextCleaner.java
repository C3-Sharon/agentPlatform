package com.sharon.agentplatform.resume.parser;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class ResumeTextCleaner {

    private ResumeTextCleaner() {
    }

    public static String clean(String text) {
        if (text == null) {
            return "";
        }

        String normalized = text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replace('\u2007', ' ')
                .replace('\u202F', ' ')
                .replaceAll("[\u200B\u200C\u200D\uFEFF\u2060]", "")
                .replaceAll("[\\p{Cc}&&[^\n\t]]", "");

        String trimmedLines = Arrays.stream(normalized.split("\n", -1))
                .map(line -> line.replaceAll(" {2,}", " ").trim())
                .collect(Collectors.joining("\n"));

        return trimmedLines
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}
