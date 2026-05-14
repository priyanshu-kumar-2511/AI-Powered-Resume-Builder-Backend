package com.airesume.ai.service.impl;

import com.airesume.ai.dto.AiRequest;
import com.airesume.ai.entity.AiHistory;
import com.airesume.ai.entity.UserQuota;
import com.airesume.ai.repository.AiHistoryRepository;
import com.airesume.ai.repository.UserQuotaRepository;
import com.airesume.ai.security.CurrentUserService;
import com.airesume.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of AiService using Spring AI and Groq Llama 3.1.
 * This service handles content generation, ATS scoring, and quota enforcement.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private static final String PROVIDER_MODEL = "Groq";

    private final ChatModel chatModel;
    private final AiHistoryRepository aiHistoryRepository;
    private final UserQuotaRepository userQuotaRepository;
    private final CurrentUserService currentUserService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Value("${ai-app.quota.free-limit:10}")
    private int defaultFreeLimit;

    @Value("${ai-app.quota.ats-limit:3}")
    private int defaultAtsLimit;

    /**
     * Generates a professional summary based on job title and existing content.
     * Validates user quota and caches/evicts history on completion.
     */
    @Override
    @CacheEvict(value = "ai_history", key = "#request.userId")
    public Map<String, Object> generateSummary(AiRequest request) {
        validateQuota(request.getUserId(), "SUMMARY");
        String promptText = String.format(
                "Generate a professional resume summary for a %s role. Existing details: %s. Tone: %s. Keep it under 3-4 sentences.",
                firstNonBlank(request.getTargetJobTitle(), "the target role"),
                request.getExistingContent(),
                firstNonBlank(request.getTone(), "professional")
        );
        Map<String, Object> result = callAiAndSaveHistory(promptText, request.getUserId(), "GENERATE_SUMMARY");
        consumeQuota(request.getUserId(), "SUMMARY");
        return result;
    }

    /**
     * Generates high-impact bullet points for work experience sections.
     * Uses results-oriented prompting with action verbs.
     */
    @Override
    @CacheEvict(value = "ai_history", key = "#request.userId")
    public Map<String, Object> generateBullets(AiRequest request) {
        validateQuota(request.getUserId(), "SUMMARY");
        String promptText = String.format(
                "Generate 3-5 high-impact, results-oriented resume bullet points for a %s. Based on these responsibilities: %s. Use action verbs and include metrics if possible.",
                firstNonBlank(request.getTargetJobTitle(), "target role"),
                request.getExistingContent()
        );
        Map<String, Object> result = callAiAndSaveHistory(promptText, request.getUserId(), "GENERATE_BULLETS");
        consumeQuota(request.getUserId(), "SUMMARY");
        return result;
    }

    /**
     * Performs an ATS (Applicant Tracking System) check.
     * Returns a JSON-formatted report with a score, suggestions, and missing keywords.
     */
    @Override
    @CacheEvict(value = "ai_history", key = "#request.userId")
    public Map<String, Object> checkAtsCompatibility(AiRequest request) {
        validateQuota(request.getUserId(), "ATS");
        String targetContext = firstNonBlank(request.getJobDescription(), request.getTargetJobTitle(), "the target role");
        String promptText = """
                You are an expert ATS (Applicant Tracking System) analyzer. Analyze this resume content against the target context: %s. 
                IMPORTANT: You MUST return ONLY a JSON object. No markdown, no preamble, no explanations outside the JSON.
                JSON Structure: { "score": 85, "suggestions": ["Add more keywords"], "missingKeywords": ["Java"] }
                Resume Content: %s""".formatted(targetContext, request.getExistingContent());
        Map<String, Object> raw = callAiAndSaveHistory(promptText, request.getUserId(), "CHECK_ATS");
        consumeQuota(request.getUserId(), "ATS");
        return parseAtsReport(raw);
    }

    /**
     * Suggests relevant skills for a specific job title.
     * 
     * @param resumeId the resume context
     * @param jobTitle the job title to analyze
     * @return a list of suggested skill strings
     */
    @Override
    public List<String> suggestSkills(Long resumeId, String jobTitle) {
        String promptText = "Suggest 10 relevant resume skills for a " + jobTitle + ". Return only a comma-separated list.";
        Map<String, Object> response = callAiAndSaveHistory(promptText, "system", "SUGGEST_SKILLS");
        String content = (String) response.get("content");
        if (content == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(content.split(",\\s*"))
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .toList();
    }

    /**
     * Retrieves the remaining AI quotas for a specific user.
     * Handles premium status check and quota reset logic.
     * 
     * @param userId the ID of the user
     * @return a map containing quota details
     */
    @Override
    public Map<String, Object> getUserQuota(String userId) {
        UserQuota quota = getOrCreateQuota(userId);
        quota.setPremium(currentUserService.isPremium());
        if (quota.isPremium()) {
            quota.setRemainingSummaryCount(defaultFreeLimit);
            quota.setRemainingAtsCount(defaultAtsLimit);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("remainingSummary", quota.getRemainingSummaryCount());
        response.put("summaryLimit", defaultFreeLimit);
        response.put("remainingAts", quota.getRemainingAtsCount());
        response.put("atsLimit", defaultAtsLimit);
        response.put("isPremium", quota.isPremium());
        return response;
    }

    /**
     * Generates a personalized cover letter based on user content and job details.
     * 
     * @param request the request containing user details and job context
     * @return a map containing the generated cover letter content
     */
    @Override
    @CacheEvict(value = "ai_history", key = "#request.userId")
    public Map<String, Object> generateCoverLetter(AiRequest request) {
        validatePremium(request.getUserId());
        String targetContext = firstNonBlank(request.getJobDescription(), request.getTargetJobTitle(), "the target role");
        String promptText = String.format(
                "Write a personalized cover letter for a %s position. Applicant details: %s. Focus on clarity and professional tone.",
                targetContext,
                request.getExistingContent()
        );
        return callAiAndSaveHistory(promptText, request.getUserId(), "GENERATE_COVER_LETTER");
    }

    /**
     * Rewrites a specific resume section for professional impact.
     * 
     * @param request the request containing section type and content
     * @return a map containing the improved section text
     */
    @Override
    @CacheEvict(value = "ai_history", key = "#request.userId")
    public Map<String, Object> improveSection(AiRequest request) {
        validatePremium(request.getUserId());
        String promptText = String.format(
                "Rewrite the following %s section for better clarity and professional impact: %s",
                request.getSectionType(),
                request.getExistingContent()
        );
        return callAiAndSaveHistory(promptText, request.getUserId(), "IMPROVE_SECTION");
    }

    /**
     * Tailors entire resume content to a specific target job opportunity.
     * 
     * @param request the request containing target job description and resume content
     * @return a map containing either the queued status or the result content
     */
    @Override
    @CacheEvict(value = "ai_history", key = "#request.userId")
    public Map<String, Object> tailorResume(AiRequest request) {
        validatePremium(request.getUserId());
        String targetContext = firstNonBlank(request.getJobDescription(), request.getTargetJobTitle(), "the job description");
        String promptText = String.format(
                "Tailor the following resume content to better match this target opportunity: %s. Content: %s",
                targetContext,
                request.getExistingContent()
        );

        log.info("[RABBITMQ] Publishing tailor task to queue for user: {}", request.getUserId());
        try {
            com.airesume.ai.dto.AiJobMessage message = com.airesume.ai.dto.AiJobMessage.builder()
                    .userId(request.getUserId())
                    .actionType("TAILOR_RESUME")
                    .promptText(promptText)
                    .build();
            rabbitTemplate.convertAndSend("x.airesume", "ai.job", message);
            log.info("[RABBITMQ] Successfully published tailor task for user: {}", request.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "QUEUED");
            response.put("message", "Resume tailoring task has been submitted successfully to the background queue.");
            response.put("timestamp", LocalDateTime.now().toString());
            return response;
        } catch (Exception e) {
            log.error("[RABBITMQ] Failed to publish tailor task for user: {}. Error: {}", request.getUserId(), e.getMessage());
            log.info("[FALLBACK] Executing tailor task synchronously due to queue error.");
            return callAiAndSaveHistory(promptText, request.getUserId(), "TAILOR_RESUME");
        }
    }

    /**
     * Translates resume content to a specified target language.
     * 
     * @param request the request containing target language and source content
     * @return a map containing either the queued status or the translated result
     */
    @Override
    @CacheEvict(value = "ai_history", key = "#request.userId")
    public Map<String, Object> translateResume(AiRequest request) {
        validatePremium(request.getUserId());
        String language = firstNonBlank(request.getTargetLanguage(), request.getLanguage());
        String promptText = String.format(
                "Translate the following resume content to %s. Preserve the structure and professional terminology. Content: %s",
                language,
                request.getExistingContent()
        );

        log.info("[RABBITMQ] Publishing translation task to queue for user: {}", request.getUserId());
        try {
            com.airesume.ai.dto.AiJobMessage message = com.airesume.ai.dto.AiJobMessage.builder()
                    .userId(request.getUserId())
                    .actionType("TRANSLATE_RESUME")
                    .promptText(promptText)
                    .build();
            rabbitTemplate.convertAndSend("x.airesume", "ai.job", message);
            log.info("[RABBITMQ] Successfully published translation task for user: {}", request.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "QUEUED");
            response.put("message", "Resume translation task has been submitted successfully to the background queue.");
            response.put("timestamp", LocalDateTime.now().toString());
            return response;
        } catch (Exception e) {
            log.error("[RABBITMQ] Failed to publish translation task for user: {}. Error: {}", request.getUserId(), e.getMessage());
            log.info("[FALLBACK] Executing translation task synchronously due to queue error.");
            return callAiAndSaveHistory(promptText, request.getUserId(), "TRANSLATE_RESUME");
        }
    }

    /**
     * Fetches the historical list of AI interactions for a specific user.
     * 
     * @param userId the ID of the user
     * @return a list of maps containing history record details
     */
    @Override
    @Cacheable(value = "ai_history", key = "#userId")
    public List<Map<String, Object>> getUserHistory(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<AiHistory> historyList = aiHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> response = new ArrayList<>();
        for (AiHistory history : historyList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", history.getId());
            item.put("requestType", history.getActionType());
            item.put("model", history.getModelUsed());
            item.put("tokensUsed", history.getTokensUsed() != null ? history.getTokensUsed() : 0);
            item.put("timestamp", history.getCreatedAt());
            item.put("inputPrompt", history.getPromptUsed());
            item.put("response", history.getResponseContent());
            response.add(item);
        }
        return response;
    }

    @Override
    public Map<String, Object> analyzeJobFit(AiRequest request) {
        return checkAtsCompatibility(request);
    }

    @Override
    public Map<String, Object> getUsageStats() {
        List<AiHistory> allHistory = aiHistoryRepository.findAll();
        
        Map<String, Integer> callsByModel = new HashMap<>();
        Map<String, Integer> userCallMap = new HashMap<>();

        for (AiHistory h : allHistory) {
            String model = h.getModelUsed() != null ? h.getModelUsed() : PROVIDER_MODEL;
            callsByModel.put(model, callsByModel.getOrDefault(model, 0) + 1);
            userCallMap.put(h.getUserId(), userCallMap.getOrDefault(h.getUserId(), 0) + 1);
        }

        // Daily Trend
        List<Map<String, Object>> dailyTrend = aiHistoryRepository.getDailyStats().stream()
                .map(row -> Map.<String, Object>of(
                        "date", row[0].toString(),
                        "count", row[1]
                ))
                .toList();

        // Top users by call count
        List<Map<String, Object>> topUsers = userCallMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    String uid = e.getKey() != null ? e.getKey() : "anonymous";
                    return Map.<String, Object>of(
                        "userId", uid,
                        "username", uid,
                        "callCount", e.getValue()
                    );
                })
                .toList();

        return Map.of(
                "totalAiCalls", (long) allHistory.size(),
                "callsByModel", callsByModel,
                "topUsersByUsage", topUsers,
                "dailyTrend", dailyTrend,
                "totalTokensUsed", 0,
                "totalCostEstimate", 0.0
        );
    }

    @Override
    public Map<String, Object> getCostByUser() {
        return getUsageStats();
    }

    /**
     * Helper to get user quota or create a new one if not exists.
     * Quotas track usage for Free tier users.
     */
    private UserQuota getOrCreateQuota(String userId) {
        final String uid = (userId == null) ? "anonymous" : userId;
        return userQuotaRepository.findById(uid).orElseGet(() -> {
            UserQuota newQuota = UserQuota.builder()
                    .userId(uid)
                    .remainingSummaryCount(defaultFreeLimit)
                    .remainingAtsCount(defaultAtsLimit)
                    .isPremium(currentUserService.isPremium())
                    .build();
            return userQuotaRepository.save(newQuota);
        });
    }

    private void validateQuota(String userId, String type) {
        UserQuota quota = getOrCreateQuota(userId);
        quota.setPremium(currentUserService.isPremium());
        if (quota.isPremium()) {
            return;
        }

        if ("SUMMARY".equals(type)) {
            if (quota.getRemainingSummaryCount() <= 0) {
                throw new RuntimeException("Summary quota exceeded. Please upgrade to Premium.");
            }
        } else if ("ATS".equals(type)) {
            if (quota.getRemainingAtsCount() <= 0) {
                throw new RuntimeException("ATS check quota exceeded. Please upgrade to Premium.");
            }
        }
    }

    private void consumeQuota(String userId, String type) {
        UserQuota quota = getOrCreateQuota(userId);
        quota.setPremium(currentUserService.isPremium());
        if (quota.isPremium()) {
            return;
        }

        if ("SUMMARY".equals(type) && quota.getRemainingSummaryCount() > 0) {
            quota.setRemainingSummaryCount(quota.getRemainingSummaryCount() - 1);
            userQuotaRepository.save(quota);
        } else if ("ATS".equals(type) && quota.getRemainingAtsCount() > 0) {
            quota.setRemainingAtsCount(quota.getRemainingAtsCount() - 1);
            userQuotaRepository.save(quota);
        }
    }

    private void validatePremium(String userId) {
        UserQuota quota = getOrCreateQuota(userId);
        quota.setPremium(currentUserService.isPremium());
        if (!quota.isPremium()) {
            throw new RuntimeException("Premium feature locked. Please upgrade to Premium to use this feature.");
        }
    }

    /**
     * Core method to communicate with Groq AI via Spring AI.
     * Saves request/response history for auditing.
     */
    private Map<String, Object> callAiAndSaveHistory(String promptText, String userId, String actionType) {
        Map<String, Object> result = new HashMap<>();
        String responseContent = null;
        Integer tokensUsed = 0;

        try {
            log.info("Calling AI (Groq) for action={} user={}", actionType, userId);
            ChatResponse response = chatModel.call(new Prompt(new UserMessage(promptText)));
            
            responseContent = response.getResult().getOutput().getText();
            
            // Extract tokens from metadata
            Usage usage = response.getMetadata().getUsage();
            if (usage != null) {
                tokensUsed = (int) usage.getTotalTokens();
                log.info("AI call success. Tokens used: {}", tokensUsed);
            }

            if (responseContent == null || responseContent.isBlank()) {
                throw new RuntimeException("AI returned an empty response. Please try again.");
            }

            result.put("content", responseContent);
            result.put("model", PROVIDER_MODEL);
            result.put("requestType", actionType);
            result.put("tokensUsed", tokensUsed);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("status", "SUCCESS");
        } catch (RuntimeException runtimeException) {
            throw runtimeException;
        } catch (Exception ex) {
            log.error("Groq AI call failed for action={}: {}", actionType, ex.getMessage(), ex);
            throw new RuntimeException("AI service temporarily unavailable. Please check your Groq API key or try again in a few minutes.");
        } finally {
            if (userId != null && !"system".equals(userId)) {
                try {
                    AiHistory history = AiHistory.builder()
                            .userId(userId)
                            .actionType(actionType)
                            .promptUsed(promptText.substring(0, Math.min(promptText.length(), 500)))
                            .responseContent(responseContent != null ? responseContent : "ERROR")
                            .modelUsed(PROVIDER_MODEL)
                            .tokensUsed(tokensUsed)
                            .build();
                    aiHistoryRepository.save(history);
                } catch (Exception historyEx) {
                    log.warn("Could not save AI history: {}", historyEx.getMessage());
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAtsReport(Map<String, Object> raw) {
        Object content = raw.get("content");
        if (!(content instanceof String text)) {
            return Map.of("score", 0, "suggestions", List.of("AI did not return a valid ATS report."), "missingKeywords", List.of());
        }

        try {
            String normalized = text.trim();
            if (normalized.contains("{")) {
                normalized = normalized.substring(normalized.indexOf("{"), normalized.lastIndexOf("}") + 1);
                normalized = normalized.replaceFirst("^```json\\s*", "")
                        .replaceFirst("^```\\s*", "")
                        .replaceFirst("\\s*```$", "");
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> parsed = mapper.readValue(normalized, Map.class);
                return Map.of(
                        "score", parseScore(parsed.get("score")),
                        "suggestions", toStringList(parsed.get("suggestions")),
                        "missingKeywords", toStringList(parsed.get("missingKeywords"))
                );
            }
            throw new IllegalArgumentException("No JSON found");
        } catch (Exception ex) {
            log.warn("ATS JSON parsing failed, extracting from text: {}", ex.getMessage());
            
            // Extract score using regex if JSON fails
            int extractedScore = 0;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:Score|score):?\\s*(\\d+)").matcher(text);
            if (m.find()) {
                extractedScore = Integer.parseInt(m.group(1));
            }

            return Map.of(
                    "score", extractedScore,
                    "suggestions", Arrays.stream(text.split("\\r?\\n"))
                            .map(String::trim)
                            .filter(line -> !line.isBlank() && !line.toLowerCase().contains("score"))
                            .limit(10)
                            .toList(),
                    "missingKeywords", List.of()
            );
        }
    }

    private int parseScore(Object scoreValue) {
        if (scoreValue instanceof Number number) {
            return Math.max(0, Math.min(100, number.intValue()));
        }
        if (scoreValue instanceof String text) {
            try {
                return Math.max(0, Math.min(100, Integer.parseInt(text.replaceAll("[^0-9]", ""))));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
        return List.of();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    @Override
    public void processBackgroundAiJob(String promptText, String userId, String actionType) {
        log.info("[RABBITMQ-CONSUMER] Executing background AI task: actionType={}, userId={}", actionType, userId);
        try {
            callAiAndSaveHistory(promptText, userId, actionType);
        } catch (Exception e) {
            log.error("[RABBITMQ-CONSUMER] Background AI task execution failed: {}", e.getMessage(), e);
        }
    }

    @Override
    public com.airesume.ai.dto.TemplateExtractionResponse extractTemplateFromPdf(MultipartFile file) {
        log.info("Extracting template from uploaded PDF: {}", file.getOriginalFilename());
        
        try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
            // 1. Generate Thumbnail Image
            org.apache.pdfbox.rendering.PDFRenderer renderer = new org.apache.pdfbox.rendering.PDFRenderer(document);
            // Render first page at 150 DPI
            BufferedImage image = renderer.renderImageWithDPI(0, 150);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            String thumbnailDataUri = "data:image/jpeg;base64," + base64Image;
            
            // 2. Extract Text
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            stripper.getText(document);
            
            // 3. Generate HTML via Groq AI
            String systemMessageStr = """
                    You are a specialized 'Visual Layout Reverse-Engineer'. Your mission is to REPLICATE the EXACT style and structure of the resume provided in the text as closely as possible.

                    CLONING RULES (CRITICAL):
                    1. ANALYZE LAYOUT: Look at the text patterns. If headers are centered, center them in HTML. If there are lines (rules) between sections, include them in CSS. If skills are listed horizontally, use a flex-wrap layout.
                    2. MIMIC TYPOGRAPHY: Observe what text is UPPERCASE or BOLD and replicate that hierarchy exactly. Use professional fonts that match the document's vibe.
                    3. PRESERVE SPACING: If the document looks dense, use tight margins. If it looks airy, use generous padding.
                    4. NO GENERIC CARDS: Do NOT use card-based or blog layouts unless the source document specifically looks like one. Stick to a clean, document-like presentation.

                    MUSTACHE PLACEHOLDERS (MANDATORY):
                    - Profile: {{fullName}}, {{jobTitle}}, {{email}}, {{phone}}, {{location}}, {{linkedin}}, {{github}}, {{website}}, {{summary}}
                    - Lists: Use {{#experience}}, {{#education}}, {{#skills}}, {{#projects}}, and {{#sections}} for any other parts.

                    FORMAT: Return ONLY a valid JSON object: { "html": "...", "css": "..." }. Do NOT add any extra text or markdown.""";
            
            org.springframework.ai.chat.messages.SystemMessage systemMessage = new org.springframework.ai.chat.messages.SystemMessage(systemMessageStr);
            org.springframework.ai.chat.prompt.Prompt prompt = new org.springframework.ai.chat.prompt.Prompt(List.of(systemMessage));
            
            org.springframework.ai.chat.model.ChatResponse chatResponse = chatModel.call(prompt);
            String aiContent = chatResponse.getResult().getOutput().getText().trim();
            
            // Clean up AI response if it includes markdown code blocks
            if (aiContent.contains("```")) {
                aiContent = aiContent.replace("```json", "").replace("```", "").trim();
            }

            String generatedHtml = "";
            String generatedCss = "";

            // Attempt 1: Standard JSON Parsing
            try {
                String jsonToParse = aiContent;
                int startIndex = jsonToParse.indexOf('{');
                int endIndex = jsonToParse.lastIndexOf('}');
                if (startIndex != -1 && endIndex != -1) {
                    jsonToParse = jsonToParse.substring(startIndex, endIndex + 1);
                }

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, String> parsed = mapper.readValue(jsonToParse, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>(){});
                generatedHtml = parsed.getOrDefault("html", "");
                generatedCss = parsed.getOrDefault("css", "");
            } catch (Exception ex) {
                log.warn("Standard JSON parsing failed, trying Regex extraction. Content head: {}", aiContent.substring(0, Math.min(100, aiContent.length())));
                
                // Attempt 2: Regex Extraction (handles backticks and malformed JSON)
                generatedHtml = extractByRegex(aiContent, "html");
                generatedCss = extractByRegex(aiContent, "css");
            }

            // Final fallback if both failed
            if (generatedHtml.isEmpty()) {
                if (!aiContent.isEmpty()) {
                    generatedHtml = aiContent;
                    generatedCss = "/* AI returned raw content or malformed JSON. Manual cleanup might be needed. */";
                } else {
                    generatedHtml = "<div class='resume'><h1>{{fullName}}</h1><p>Template generation failed. Please try again.</p></div>";
                    generatedCss = ".resume { padding: 20px; }";
                }
            }
            
            return com.airesume.ai.dto.TemplateExtractionResponse.builder()
                    .thumbnailUrl(thumbnailDataUri)
                    .htmlLayout(generatedHtml.trim())
                    .cssStyles(generatedCss.trim())
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to extract template from PDF", e);
            throw new RuntimeException("Failed to process PDF file: " + e.getMessage());
        }
    }

    private static final java.util.regex.Pattern JSON_KEY_VALUE_PATTERN = 
            java.util.regex.Pattern.compile("([\"'])(\\w+)\\1\\s*:\\s*([\"'`])(.*?)\\3", java.util.regex.Pattern.DOTALL);

    /**
     * Extracts content for a specific key from a potentially malformed JSON string using regex.
     * Supports double quotes, single quotes, and backticks.
     */
    private String extractByRegex(String content, String key) {
        java.util.regex.Matcher matcher = JSON_KEY_VALUE_PATTERN.matcher(content);
        
        while (matcher.find()) {
            if (key.equals(matcher.group(2))) {
                String value = matcher.group(4);
                // If it was a double-quoted string, it might have escaped characters
                if (matcher.group(3).equals("\"")) {
                    value = value.replace("\\n", "\n").replace("\\\"", "\"").replace("\\t", "\t");
                }
                return value;
            }
        }
        return "";
    }
}
