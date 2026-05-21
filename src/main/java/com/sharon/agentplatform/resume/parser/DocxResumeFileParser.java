package com.sharon.agentplatform.resume.parser;

import com.sharon.agentplatform.common.exception.BusinessException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class DocxResumeFileParser implements ResumeFileParser {

    @Override
    public boolean supports(String fileType) {
        return "docx".equalsIgnoreCase(fileType);
    }

    @Override
    public String parse(Path filePath) {
        try (InputStream inputStream = Files.newInputStream(filePath);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            appendParagraphs(text, document.getParagraphs());
            appendTables(text, document.getTables());
            return ResumeTextCleaner.clean(text.toString());
        } catch (IOException exception) {
            throw new BusinessException("Failed to parse docx resume file", exception);
        }
    }

    private void appendParagraphs(StringBuilder text, Iterable<XWPFParagraph> paragraphs) {
        for (XWPFParagraph paragraph : paragraphs) {
            String paragraphText = paragraph.getText();
            if (paragraphText != null && !paragraphText.isBlank()) {
                text.append(paragraphText).append(System.lineSeparator());
            }
        }
    }

    private void appendTables(StringBuilder text, Iterable<XWPFTable> tables) {
        for (XWPFTable table : tables) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    appendParagraphs(text, cell.getParagraphs());
                    appendTables(text, cell.getTables());
                }
            }
        }
    }
}
