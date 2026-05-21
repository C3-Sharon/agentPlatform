package com.sharon.agentplatform.resume.prompt;

import org.springframework.stereotype.Component;

@Component
public class ResumePromptBuilder {

    private static final int MAX_JOB_PAGE_TEXT_LENGTH = 8000;
    private static final int MAX_RESUME_TEXT_LENGTH = 8000;

    public String buildOptimizePrompt(String jobPageText, String resumeText) {
        String truncatedJobPageText = truncate(jobPageText, MAX_JOB_PAGE_TEXT_LENGTH);
        String truncatedResumeText = truncate(resumeText, MAX_RESUME_TEXT_LENGTH);

        return """
                请用中文完成一次面向校园招聘的简历优化分析。

                你必须从岗位要求出发优化简历，不能泛泛而谈。请严格使用以下 Markdown 标题输出，标题文字不能增删改：

                ## 岗位要求摘要
                ## 简历匹配分析
                ## 简历优化建议
                ## 优化后的简历
                ## 面试准备建议

                输出要求：
                1. 岗位要求摘要：提炼岗位职责、硬性技能、软性能力、加分项和隐含筛选标准。
                2. 简历匹配分析：说明简历与岗位的匹配点、缺口、风险点和可强化证据。
                3. 简历优化建议：给出可直接修改简历的建议，尽量具体到项目、经历、措辞和量化表达。
                4. 优化后的简历：基于简历原文重写一版更匹配岗位的中文简历，不要虚构不存在的经历。
                5. 面试准备建议：必须包含可能被追问的问题、建议重点准备的项目经历、回答思路、需要补强的能力表达。

                岗位网页正文：
                %s

                简历原文：
                %s
                """.formatted(truncatedJobPageText, truncatedResumeText);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength);
    }
}
