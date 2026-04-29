package com.airesume.ai.controller;

import com.airesume.ai.dto.AiRequest;
import com.airesume.ai.security.CurrentUserService;
import com.airesume.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for handling all AI-related endpoints.
 * This class exposes endpoints for generating resume content, checking ATS scores, 
 * managing AI usage quotas, and handling premium AI features like translations and tailored cover letters.
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final CurrentUserService currentUserService;

    /**
     * 1. Generate professional summary
     * Uses AI to craft a 3-4 sentence professional summary based on the user's target job title and existing details.
     * Deducts 1 unit from the user's Summary Quota.
     *
     * @param request Contains targetJobTitle, existingContent, tone, and userId
     * @return AI generated text and metadata
     */
    @PostMapping("/generate-summary")
    public ResponseEntity<?> generateSummary(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.generateSummary(request));
    }

    /**
     * 2. Generate work experience bullet points
     * Uses AI to rewrite or generate impactful bullet points with metrics.
     * Deducts 1 unit from the user's Summary Quota.
     *
     * @param request Contains targetJobTitle, existingContent, and userId
     * @return List of generated bullet points
     */
    @PostMapping("/generate-bullets")
    public ResponseEntity<?> generateBullets(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.generateBullets(request));
    }

    /**
     * 3. ATS compatibility check
     * Scores the provided resume content against a job title to determine ATS readability and keyword matches.
     * Deducts 1 unit from the user's ATS Quota.
     *
     * @param request Contains targetJobTitle, existingContent, and userId
     * @return ATS Score (0-100) and list of missing keywords
     */
    @PostMapping("/check-ats")
    public ResponseEntity<?> checkAts(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.checkAtsCompatibility(request));
    }

    /**
     * 4. AI skill suggestions
     * Suggests relevant skills for a given job title. This endpoint is free and does not consume quota.
     *
     * @param resumeId ID of the resume (for context tracking)
     * @param jobTitle The job title to generate skills for
     * @return List of 10 relevant skills
     */
    @GetMapping("/suggest-skills/{resumeId}")
    public ResponseEntity<?> suggestSkills(@PathVariable Long resumeId, @RequestParam String jobTitle) {
        return ResponseEntity.ok(aiService.suggestSkills(resumeId, jobTitle));
    }

    /**
     * 5. Get remaining quota
     * Retrieves the remaining AI quotas (Summary and ATS) for a specific user.
     *
     * @param userId The ID of the user requesting quota
     * @return UserQuota details including premium status
     */
    @GetMapping("/quota/{userId}")
    public ResponseEntity<?> getQuota(@PathVariable String userId) {
        return ResponseEntity.ok(aiService.getUserQuota(currentUserService.requireUserIdAsString()));
    }

    /**
     * 6. Generate personalised cover letter (Premium)
     * Premium feature: Generates a complete cover letter tailored to a job description.
     *
     * @param request Contains targetJobTitle, existingContent, and userId
     * @return AI generated cover letter
     * @throws RuntimeException if the user is not a premium subscriber
     */
    @PostMapping("/generate-cover-letter")
    public ResponseEntity<?> generateCoverLetter(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.generateCoverLetter(request));
    }

    /**
     * 7. Rewrite section (Premium)
     * Premium feature: Rewrites any specific section of a resume (e.g., Education, Projects) for better tone.
     *
     * @param request Contains sectionType, existingContent, tone, and userId
     * @return Rewritten section text
     */
    @PostMapping("/improve-section")
    public ResponseEntity<?> improveSection(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.improveSection(request));
    }

    /**
     * 8. Fully tailor entire resume (Premium)
     * Premium feature: Deep tailoring of the entire resume payload against a target job title.
     *
     * @param request Contains targetJobTitle, full resume JSON (existingContent), and userId
     * @return Fully revised resume JSON
     */
    @PostMapping("/tailor-resume")
    public ResponseEntity<?> tailorResume(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.tailorResume(request));
    }

    /**
     * 9. Translate full resume (Premium)
     * Premium feature: Translates the entire resume content into a target language while preserving structure.
     *
     * @param request Contains target language, existingContent, and userId
     * @return Translated resume content
     */
    @PostMapping("/translate-resume")
    public ResponseEntity<?> translateResume(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.translateResume(request));
    }

    /**
     * 10. View AI history (Premium)
     * Retrieves the chronological history of all AI prompts and responses made by the user.
     *
     * @param userId ID of the user
     * @return List of AI history records
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getHistory(@PathVariable String userId) {
        return ResponseEntity.ok(aiService.getUserHistory(currentUserService.requireUserIdAsString()));
    }

    /**
     * 11. Compute resume-to-job match score (Internal)
     * Internal microservice endpoint used by the JobMatch-Service to get ATS scores.
     *
     * @param request Contains job description and resume content
     * @return ATS Score details
     */
    @PostMapping("/internal/analyze-job-fit")
    public ResponseEntity<?> analyzeJobFit(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.analyzeJobFit(request));
    }

    /**
     * 12. Token usage stats (Admin)
     * Admin dashboard endpoint to view platform-wide AI usage statistics.
     *
     * @return Total usage and model stats
     */
    @GetMapping("/admin/usage-stats")
    public ResponseEntity<?> getUsageStats() {
        return ResponseEntity.ok(aiService.getUsageStats());
    }

    /**
     * 13. Token cost breakdown (Admin)
     * Admin dashboard endpoint to view AI cost breakdowns.
     *
     * @return Cost statistics
     */
    @GetMapping("/admin/cost-by-user")
    public ResponseEntity<?> getCostByUser() {
        return ResponseEntity.ok(aiService.getCostByUser());
    }
}
