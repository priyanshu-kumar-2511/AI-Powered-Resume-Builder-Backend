package com.airesume.ai.service;

import com.airesume.ai.dto.AiRequest;
import com.airesume.ai.dto.TemplateExtractionResponse;
import java.util.List;
import java.util.Map;

public interface AiService {
    /**
     * Generates a professional summary based on the provided job title and context.
     * @param request AI context payload
     * @return generated summary data
     */
    Map<String, Object> generateSummary(AiRequest request);

    /**
     * Generates or improves work experience bullet points.
     * @param request AI context payload
     * @return generated bullet points data
     */
    Map<String, Object> generateBullets(AiRequest request);

    /**
     * Checks the resume content against a job title for ATS compatibility.
     * @param request AI context payload
     * @return ATS score and keyword analysis
     */
    Map<String, Object> checkAtsCompatibility(AiRequest request);

    /**
     * Suggests relevant skills for a given job title.
     * @param resumeId context resume
     * @param jobTitle target job role
     * @return list of suggested skills
     */
    List<String> suggestSkills(Long resumeId, String jobTitle);

    /**
     * Retrieves the remaining AI quota for a user.
     * @param userId user identifier
     * @return quota details
     */
    Map<String, Object> getUserQuota(String userId);

    /**
     * Generates a tailored cover letter (Premium feature).
     * @param request AI context payload
     * @return generated cover letter
     */
    Map<String, Object> generateCoverLetter(AiRequest request);

    /**
     * Improves the tone and structure of a specific resume section (Premium feature).
     * @param request AI context payload
     * @return rewritten section
     */
    Map<String, Object> improveSection(AiRequest request);

    /**
     * Tailors the entire resume against a specific job description (Premium feature).
     * @param request AI context payload
     * @return tailored resume content
     */
    Map<String, Object> tailorResume(AiRequest request);

    /**
     * Translates the resume content to a target language (Premium feature).
     * @param request AI context payload
     * @return translated content
     */
    Map<String, Object> translateResume(AiRequest request);

    /**
     * Retrieves the history of AI requests made by the user.
     * @param userId user identifier
     * @return list of historical AI interactions
     */
    List<Map<String, Object>> getUserHistory(String userId);

    /**
     * Analyzes the fit between a resume and a job description.
     * @param request AI context payload
     * @return job fit score
     */
    Map<String, Object> analyzeJobFit(AiRequest request);

    /**
     * Extracts template content from an uploaded PDF file.
     * @param file the uploaded PDF file
     * @return extracted template information
     */
    TemplateExtractionResponse extractTemplateFromPdf(org.springframework.web.multipart.MultipartFile file);

    /**
     * Retrieves platform-wide AI usage statistics for Admin dashboard.
     * @return aggregate usage stats
     */
    Map<String, Object> getUsageStats();

    /**
     * Retrieves token cost breakdowns per user for Admin dashboard.
     * @return cost statistics
     */
    Map<String, Object> getCostByUser();

    /**
     * Executes a background AI processing task and saves the result to user history (Premium feature).
     * @param promptText complete prompt string
     * @param userId user identifier
     * @param actionType action type key
     */
    void processBackgroundAiJob(String promptText, String userId, String actionType);
}
