package com.sharon.agentplatform.resume.web;

import com.sharon.agentplatform.common.exception.BusinessException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JobPageReader {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) "
            + "Chrome/124.0.0.0 Safari/537.36";

    public JobPageReadResult read(String jobUrl) {
        try {
            Document document = Jsoup.connect(jobUrl)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            String text = document.body() == null ? null : document.body().text();
            if (text == null || text.isBlank()) {
                throw new BusinessException("Job page text is empty: " + jobUrl);
            }

            return new JobPageReadResult(jobUrl, document.title(), text);
        } catch (BusinessException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new BusinessException("Failed to read job page: " + jobUrl, exception);
        }
    }

    public static class JobPageReadResult {

        private final String url;
        private final String title;
        private final String text;

        public JobPageReadResult(String url, String title, String text) {
            this.url = url;
            this.title = title;
            this.text = text;
        }

        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }
    }
}
