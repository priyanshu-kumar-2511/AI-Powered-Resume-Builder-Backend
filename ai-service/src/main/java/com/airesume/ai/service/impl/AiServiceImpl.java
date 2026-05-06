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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${ai-app.quota.free-limit:10}")
    private int defaultFreeLimit;

    @Value("${ai-app.quota.ats-limit:3}")
    private int defaultAtsLimit;

    @Override
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

    @Override
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

    @Override
    public Map<String, Object> checkAtsCompatibility(AiRequest request) {
        validateQuota(request.getUserId(), "ATS");
        String targetContext = firstNonBlank(request.getJobDescription(), request.getTargetJobTitle(), "the target role");
        String promptText = String.format(
                "Analyze this resume content against the following target context: %s. Return strict JSON with keys 'score' (0-100), 'suggestions' (array of short improvement suggestions), and 'missingKeywords' (array of important missing keywords or phrases). Content: %s",
                targetContext,
                request.getExistingContent()
        );
        Map<String, Object> raw = callAiAndSaveHistory(promptText, request.getUserId(), "CHECK_ATS");
        consumeQuota(request.getUserId(), "ATS");
        return parseAtsReport(raw);
    }

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

    @Override
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

    @Override
    public Map<String, Object> improveSection(AiRequest request) {
        validatePremium(request.getUserId());
        String promptText = String.format(
                "Rewrite the following %s section for better clarity and professional impact: %s",
                request.getSectionType(),
                request.getExistingContent()
        );
        return callAiAndSaveHistory(promptText, request.getUserId(), "IMPROVE_SECTION");
    }

    @Override
    public Map<String, Object> tailorResume(AiRequest request) {
        validatePremium(request.getUserId());
        String targetContext = firstNonBlank(request.getJobDescription(), request.getTargetJobTitle(), "the job description");
        String promptText = String.format(
                "Tailor the following resume content to better match this target opportunity: %s. Content: %s",
                targetContext,
                request.getExistingContent()
        );
        return callAiAndSaveHistory(promptText, request.getUserId(), "TAILOR_RESUME");
    }

    @Override
    public Map<String, Object> translateResume(AiRequest request) {
        validatePremium(request.getUserId());
        String language = firstNonBlank(request.getTargetLanguage(), request.getLanguage());
        String promptText = String.format(
                "Translate the following resume content to %s. Preserve the structure and professional terminology. Content: %s",
                language,
                request.getExistingContent()
        );
        return callAiAndSaveHistory(promptText, request.getUserId(), "TRANSLATE_RESUME");
    }

    @Override
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
            item.put("tokensUsed", 0);
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
        return Map.of("model", PROVIDER_MODEL, "totalRequests", aiHistoryRepository.count());
    }

    @Override
    public Map<String, Object> getCostByUser() {
        return Map.of("totalCost", 0.0);
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
        } else if ("ATS".equals(type) && quota.getRemainingAtsCount() > 0) {
            quota.setRemainingAtsCount(quota.getRemainingAtsCount() - 1);
        }

        userQuotaRepository.save(quota);
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

        try {
            log.info("Calling AI (Groq) for action={} user={}", actionType, userId);
            responseContent = chatModel
                    .call(new Prompt(new UserMessage(promptText)))
                    .getResult()
                    .getOutput()
                    .getText();

            if (responseContent == null || responseContent.isBlank()) {
                throw new RuntimeException("AI returned an empty response. Please try again.");
            }

            result.put("content", responseContent);
            result.put("model", PROVIDER_MODEL);
            result.put("requestType", actionType);
            result.put("tokensUsed", 0);
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
            if (normalized.startsWith("```")) {
                normalized = normalized.replaceFirst("^```json\\s*", "")
                        .replaceFirst("^```\\s*", "")
                        .replaceFirst("\\s*```$", "");
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> parsed = mapper.readValue(normalized, Map.class);
            return Map.of(
                    "score", parseScore(parsed.get("score")),
                    "suggestions", toStringList(parsed.get("suggestions")),
                    "missingKeywords", toStringList(parsed.get("missingKeywords"))
            );
        } catch (Exception ex) {
            log.warn("Could not parse ATS JSON response, falling back to plain text parsing: {}", ex.getMessage());
            return Map.of(
                    "score", 0,
                    "suggestions", Arrays.stream(text.split("\\r?\\n"))
                            .map(String::trim)
                            .filter(line -> !line.isBlank())
                            .limit(6)
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
}
