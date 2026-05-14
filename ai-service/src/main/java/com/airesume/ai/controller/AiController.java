package com.airesume.ai.controller;

import com.airesume.ai.dto.AiRequest;
import com.airesume.ai.security.CurrentUserService;
import com.airesume.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "AI Controller", description = "Endpoints for AI-powered resume content generation, suggestions, and ATS optimization")
public class AiController {

    private final AiService aiService;
    private final CurrentUserService currentUserService;

    /**
     * 1. Generate professional summary
     * 
     * @param request the AI request containing job title and existing content
     * @return a response containing the generated summary
     */
    @PostMapping("/generate-summary")
    @Operation(summary = "Generate professional summary")
    public ResponseEntity<?> generateSummary(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.generateSummary(request));
    }

    /**
     * 2. Generate work experience bullet points
     * 
     * @param request the AI request with context for bullet generation
     * @return a response containing optimized bullet points
     */
    @PostMapping("/generate-bullets")
    @Operation(summary = "Generate work experience bullet points")
    public ResponseEntity<?> generateBullets(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.generateBullets(request));
    }

    /**
     * 3. ATS compatibility check
     * 
     * @param request the AI request containing resume and job description
     * @return a JSON report with ATS score and suggestions
     */
    @PostMapping("/check-ats")
    @Operation(summary = "Check ATS compatibility and score")
    public ResponseEntity<?> checkAts(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.checkAtsCompatibility(request));
    }

    /**
     * 4. AI skill suggestions
     * 
     * @param resumeId the ID of the resume
     * @param jobTitle the target job title for skill extraction
     * @return a list of suggested skills
     */
    @GetMapping("/suggest-skills/{resumeId}")
    @Operation(summary = "Suggest relevant skills for a job title")
    public ResponseEntity<?> suggestSkills(@PathVariable Long resumeId, @RequestParam String jobTitle) {
        return ResponseEntity.ok(aiService.suggestSkills(resumeId, jobTitle));
    }

    /**
     * 5. Get remaining quota
     * 
     * @param userId the ID of the user
     * @return the remaining AI call quotas
     */
    @GetMapping("/quota/{userId}")
    @Operation(summary = "Get remaining AI quotas for a user")
    public ResponseEntity<?> getQuota(@PathVariable String userId) {
        return ResponseEntity.ok(aiService.getUserQuota(currentUserService.requireUserIdAsString()));
    }

    /**
     * 6. Generate personalised cover letter (Premium)
     */
    @PostMapping("/generate-cover-letter")
    @Operation(summary = "Generate personalised cover letter (Premium)")
    public ResponseEntity<?> generateCoverLetter(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.generateCoverLetter(request));
    }

    /**
     * 7. Rewrite section (Premium)
     */
    @PostMapping("/improve-section")
    @Operation(summary = "Rewrite/Improve a resume section (Premium)")
    public ResponseEntity<?> improveSection(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.improveSection(request));
    }

    /**
     * 8. Fully tailor entire resume (Premium)
     */
    @PostMapping("/tailor-resume")
    @Operation(summary = "Fully tailor entire resume to a job description (Premium)")
    public ResponseEntity<?> tailorResume(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.tailorResume(request));
    }

    /**
     * 9. Translate full resume (Premium)
     */
    @PostMapping("/translate-resume")
    @Operation(summary = "Translate full resume to a target language (Premium)")
    public ResponseEntity<?> translateResume(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.translateResume(request));
    }

    /**
     * 10. View AI history (Premium)
     */
    @GetMapping("/history/{userId}")
    @Operation(summary = "View AI chronological history (Premium)")
    public ResponseEntity<?> getHistory(@PathVariable String userId) {
        return ResponseEntity.ok(aiService.getUserHistory(currentUserService.requireUserIdAsString()));
    }

    /**
     * 11. Compute resume-to-job match score (Internal)
     */
    @PostMapping("/internal/analyze-job-fit")
    @Operation(summary = "Analyze job fit match score (Internal)")
    public ResponseEntity<?> analyzeJobFit(@RequestBody AiRequest request) {
        request.setUserId(currentUserService.requireUserIdAsString());
        return ResponseEntity.ok(aiService.analyzeJobFit(request));
    }

    /**
     * 13. Extract template from PDF
     */
    @PostMapping("/templates/extract-from-pdf")
    @Operation(summary = "Extract HTML template and thumbnail from PDF (Admin)")
    public ResponseEntity<?> extractTemplateFromPdf(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(aiService.extractTemplateFromPdf(file));
    }

    /**
     * 14. Admin AI Usage Statistics
     */
    @GetMapping("/admin/stats")
    @Operation(summary = "Get global AI usage statistics (Admin only)")
    public ResponseEntity<?> getAdminStats() {
        return ResponseEntity.ok(aiService.getUsageStats());
    }

    /**
     * 12. Token usage stats (Admin)
     */
    @GetMapping("/admin/usage-stats")
    @Operation(summary = "Get platform-wide AI token usage statistics (Admin only)")
    public ResponseEntity<?> getUsageStats() {
        return ResponseEntity.ok(aiService.getUsageStats());
    }

    /**
     * 13. Token cost breakdown (Admin)
     */
    @GetMapping("/admin/cost-by-user")
    @Operation(summary = "Get AI cost breakdown by user (Admin only)")
    public ResponseEntity<?> getCostByUser() {
        return ResponseEntity.ok(aiService.getCostByUser());
    }
}
