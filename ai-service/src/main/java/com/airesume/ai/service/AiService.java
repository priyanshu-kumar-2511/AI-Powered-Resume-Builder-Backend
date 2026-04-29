package com.airesume.ai.service;

import com.airesume.ai.dto.AiRequest;
import java.util.List;
import java.util.Map;

public interface AiService {
    Map<String, Object> generateSummary(AiRequest request);
    Map<String, Object> generateBullets(AiRequest request);
    Map<String, Object> checkAtsCompatibility(AiRequest request);
    List<String> suggestSkills(Long resumeId, String jobTitle);
    Map<String, Object> getUserQuota(String userId);
    Map<String, Object> generateCoverLetter(AiRequest request);
    Map<String, Object> improveSection(AiRequest request);
    Map<String, Object> tailorResume(AiRequest request);
    Map<String, Object> translateResume(AiRequest request);
    List<Map<String, Object>> getUserHistory(String userId);
    Map<String, Object> analyzeJobFit(AiRequest request);
    Map<String, Object> getUsageStats();
    Map<String, Object> getCostByUser();
}
