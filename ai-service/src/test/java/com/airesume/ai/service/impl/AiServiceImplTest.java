package com.airesume.ai.service.impl;

import com.airesume.ai.dto.AiJobMessage;
import com.airesume.ai.dto.AiRequest;
import com.airesume.ai.entity.AiHistory;
import com.airesume.ai.entity.UserQuota;
import com.airesume.ai.repository.AiHistoryRepository;
import com.airesume.ai.repository.UserQuotaRepository;
import com.airesume.ai.security.CurrentUserService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AiServiceImpl.
 * Covers AI content generation, quota management, RabbitMQ background processing,
 * and PDF template extraction logic.
 */
@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private AiHistoryRepository aiHistoryRepository;

    @Mock
    private UserQuotaRepository userQuotaRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "defaultFreeLimit", 10);
        ReflectionTestUtils.setField(aiService, "defaultAtsLimit", 3);
        lenient().when(currentUserService.isPremium()).thenReturn(false);
    }

    // ── BASIC AI TESTS ────────────────────────────────────────────────────────

    /**
     * Tests successful summary generation and quota decrement.
     */
    @Test
    void testGenerateSummary_Success() {
        AiRequest request = AiRequest.builder().userId("user1").build();
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").remainingSummaryCount(5).build()));
        
        Generation gen = new Generation(new AssistantMessage("Summary content"));
        ChatResponse resp = new ChatResponse(List.of(gen));
        when(chatModel.call(any(Prompt.class))).thenReturn(resp);

        Map<String, Object> result = aiService.generateSummary(request);
        assertEquals("Summary content", result.get("content"));
        verify(userQuotaRepository).save(any());
    }

    /**
     * Verifies that the AI can successfully generate resume experience bullets.
     */
    @Test
    void testGenerateBullets_Success() {
        AiRequest request = AiRequest.builder().userId("user1").build();
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").remainingSummaryCount(5).build()));
        
        Generation gen = new Generation(new AssistantMessage("Bullets"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));

        Map<String, Object> result = aiService.generateBullets(request);
        assertNotNull(result.get("content"));
    }

    /**
     * Verifies the ATS compatibility check logic and JSON parsing.
     */
    @Test
    void testCheckAtsCompatibility_Success() {
        AiRequest request = AiRequest.builder().userId("user1").build();
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").remainingAtsCount(3).build()));
        
        String json = "{\"score\": 85, \"suggestions\": [\"Add skills\"], \"missingKeywords\": [\"Java\"]}";
        Generation gen = new Generation(new AssistantMessage(json));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));

        Map<String, Object> result = aiService.checkAtsCompatibility(request);
        assertEquals(85, result.get("score"));
    }

    /**
     * Verifies that the AI can suggest relevant skills based on job context.
     */
    @Test
    void testSuggestSkills_Success() {
        Generation gen = new Generation(new AssistantMessage("Java, Python, AWS"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));

        List<String> skills = aiService.suggestSkills(1L, "Dev");
        assertEquals(3, skills.size());
        assertTrue(skills.contains("Java"));
    }

    /**
     * Verifies the quota retrieval logic for premium users.
     */
    @Test
    void testGetUserQuota_Premium() {
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").build()));
        when(currentUserService.isPremium()).thenReturn(true);
        
        Map<String, Object> quota = aiService.getUserQuota("user1");
        assertTrue((Boolean) quota.get("isPremium"));
    }

    /**
     * Verifies that the AI can successfully generate a cover letter.
     */
    @Test
    void testGenerateCoverLetter_Success() {
        AiRequest request = AiRequest.builder().userId("user1").build();
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").isPremium(true).build()));
        when(currentUserService.isPremium()).thenReturn(true);
        
        Generation gen = new Generation(new AssistantMessage("Letter"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));

        Map<String, Object> result = aiService.generateCoverLetter(request);
        assertEquals("Letter", result.get("content"));
    }

    /**
     * Verifies the AI's ability to improve specific resume sections.
     */
    @Test
    void testImproveSection_Success() {
        AiRequest request = AiRequest.builder().userId("user1").build();
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").isPremium(true).build()));
        when(currentUserService.isPremium()).thenReturn(true);
        
        Generation gen = new Generation(new AssistantMessage("Improved"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));

        Map<String, Object> result = aiService.improveSection(request);
        assertEquals("Improved", result.get("content"));
    }

    // ── RABBITMQ & BACKGROUND JOBS ──────────────────────────────────────────

    /**
     * Verifies that resume tailoring is correctly queued for asynchronous processing.
     */
    @Test
    void testTailorResume_Queued() {
        AiRequest request = AiRequest.builder().userId("user1").build();
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").isPremium(true).build()));
        when(currentUserService.isPremium()).thenReturn(true);

        Map<String, Object> result = aiService.tailorResume(request);
        assertEquals("QUEUED", result.get("status"));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AiJobMessage.class));
    }

    /**
     * Verifies that the system falls back to synchronous AI processing if RabbitMQ queueing fails.
     */
    @Test
    void testTailorResume_FallbackToSync() {
        AiRequest request = AiRequest.builder().userId("user1").build();
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").isPremium(true).build()));
        when(currentUserService.isPremium()).thenReturn(true);
        
        doThrow(new RuntimeException("Rabbit error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AiJobMessage.class));
        
        Generation gen = new Generation(new AssistantMessage("Sync response"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));

        Map<String, Object> result = aiService.tailorResume(request);
        assertEquals("Sync response", result.get("content"));
    }

    /**
     * Verifies that resume translation requests are successfully queued.
     */
    @Test
    void testTranslateResume_Queued() {
        AiRequest request = AiRequest.builder().userId("user1").targetLanguage("Spanish").build();
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").isPremium(true).build()));
        when(currentUserService.isPremium()).thenReturn(true);

        Map<String, Object> result = aiService.translateResume(request);
        assertEquals("QUEUED", result.get("status"));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AiJobMessage.class));
    }

    /**
     * Verifies successful execution of a background AI task and history persistence.
     */
    @Test
    void testProcessBackgroundAiJob_Success() {
        Generation gen = new Generation(new AssistantMessage("Bg result"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));

        aiService.processBackgroundAiJob("Prompt", "user1", "TASK");
        verify(aiHistoryRepository).save(any(AiHistory.class));
    }

    /**
     * Verifies synchronous fallback for translation requests when the message broker is unavailable.
     */
    @Test
    void testTranslateResume_FallbackToSync() {
        AiRequest request = AiRequest.builder().userId("user1").targetLanguage("French").build();
        when(userQuotaRepository.findById("user1")).thenReturn(Optional.of(UserQuota.builder().userId("user1").isPremium(true).build()));
        when(currentUserService.isPremium()).thenReturn(true);
        
        doThrow(new RuntimeException("Queue error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AiJobMessage.class));
        
        Generation gen = new Generation(new AssistantMessage("French content"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
        
        Map<String, Object> result = aiService.translateResume(request);
        assertEquals("French content", result.get("content"));
    }

    // ── PDF EXTRACTION TESTS ────────────────────────────────────────────────

    /**
     * Tests the extraction of HTML/CSS layouts from a mock PDF file.
     * Uses static mocks for PDFBox and ImageIO.
     */
    @Test
    void testExtractTemplateFromPdf_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy data".getBytes());

        try (MockedStatic<Loader> loaderMock = mockStatic(Loader.class);
             MockedStatic<javax.imageio.ImageIO> imageIoMock = mockStatic(javax.imageio.ImageIO.class);
             MockedConstruction<PDFTextStripper> stripperMock = mockConstruction(PDFTextStripper.class, (mock, context) -> {
                 when(mock.getText(any())).thenReturn("Raw PDF Text");
             });
             MockedConstruction<PDFRenderer> rendererMock = mockConstruction(PDFRenderer.class, (mock, context) -> {
                 BufferedImage dummyImg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
                 when(mock.renderImageWithDPI(anyInt(), anyInt())).thenReturn(dummyImg);
             })) {

            PDDocument mockDoc = mock(PDDocument.class);
            loaderMock.when(() -> Loader.loadPDF(any(byte[].class))).thenReturn(mockDoc);
            imageIoMock.when(() -> javax.imageio.ImageIO.write(any(java.awt.image.RenderedImage.class), anyString(), any(java.io.OutputStream.class))).thenReturn(true);

            String aiJson = "{\"html\": \"<div>Html</div>\", \"css\": \".css {}\"}";
            Generation gen = new Generation(new AssistantMessage(aiJson));
            when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));

            var response = aiService.extractTemplateFromPdf(file);

            assertNotNull(response);
            assertEquals("<div>Html</div>", response.getHtmlLayout());
            assertEquals(".css {}", response.getCssStyles());
        }
    }

    /**
     * Verifies that malformed JSON responses from the AI are handled gracefully during PDF extraction.
     */
    @Test
    void testExtractTemplateFromPdf_AiJsonMalformedFallback() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy data".getBytes());

        try (MockedStatic<Loader> loaderMock = mockStatic(Loader.class);
             MockedStatic<javax.imageio.ImageIO> imageIoMock = mockStatic(javax.imageio.ImageIO.class);
             MockedConstruction<PDFTextStripper> stripperMock = mockConstruction(PDFTextStripper.class, (mock, context) -> {
                 when(mock.getText(any())).thenReturn("Raw PDF Text");
             });
             MockedConstruction<PDFRenderer> rendererMock = mockConstruction(PDFRenderer.class, (mock, context) -> {
                 BufferedImage dummyImg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
                 when(mock.renderImageWithDPI(anyInt(), anyInt())).thenReturn(dummyImg);
             })) {

            PDDocument mockDoc = mock(PDDocument.class);
            loaderMock.when(() -> Loader.loadPDF(any(byte[].class))).thenReturn(mockDoc);
            imageIoMock.when(() -> javax.imageio.ImageIO.write(any(java.awt.image.RenderedImage.class), anyString(), any(java.io.OutputStream.class))).thenReturn(true);

            // AI returns invalid JSON
            String aiResponse = "Here is your template: { html: ... missing quotes }";
            Generation gen = new Generation(new AssistantMessage(aiResponse));
            when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));

            var response = aiService.extractTemplateFromPdf(file);

            assertNotNull(response);
            assertTrue(response.getHtmlLayout().contains("missing quotes"));
            assertTrue(response.getCssStyles().contains("AI returned raw content or malformed JSON"));
        }
    }

    /**
     * Verifies that AI responses using backticks (JS-style template literals) are correctly parsed.
     */
    @Test
    void testExtractTemplateFromPdf_BackticksRegex() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        try (MockedStatic<Loader> loaderMock = mockStatic(Loader.class);
             MockedStatic<javax.imageio.ImageIO> imageIoMock = mockStatic(javax.imageio.ImageIO.class);
             MockedConstruction<PDFTextStripper> stripperMock = mockConstruction(PDFTextStripper.class, (mock, context) -> {
                 when(mock.getText(any())).thenReturn("Text");
             });
             MockedConstruction<PDFRenderer> rendererMock = mockConstruction(PDFRenderer.class, (mock, context) -> {
                 when(mock.renderImageWithDPI(anyInt(), anyInt())).thenReturn(new BufferedImage(1,1,1));
             })) {
            
            PDDocument doc = mock(PDDocument.class);
            loaderMock.when(() -> Loader.loadPDF(any(byte[].class))).thenReturn(doc);
            
            // AI returns JSON with backticks
            String aiResponse = "{\"html\": `<div class='backtick'>Content</div>`, \"css\": `.test { color: red; }`}";
            Generation gen = new Generation(new AssistantMessage(aiResponse));
            when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
            
            var resp = aiService.extractTemplateFromPdf(file);
            assertEquals("<div class='backtick'>Content</div>", resp.getHtmlLayout());
            assertEquals(".test { color: red; }", resp.getCssStyles());
        }
    }

    /**
     * Verifies that AI responses using single quotes for JSON keys/values are correctly handled.
     */
    @Test
    void testExtractTemplateFromPdf_SingleQuotesRegex() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        try (MockedStatic<Loader> loaderMock = mockStatic(Loader.class);
             MockedStatic<javax.imageio.ImageIO> imageIoMock = mockStatic(javax.imageio.ImageIO.class);
             MockedConstruction<PDFTextStripper> stripperMock = mockConstruction(PDFTextStripper.class, (mock, context) -> {
                 when(mock.getText(any())).thenReturn("Text");
             });
             MockedConstruction<PDFRenderer> rendererMock = mockConstruction(PDFRenderer.class, (mock, context) -> {
                 when(mock.renderImageWithDPI(anyInt(), anyInt())).thenReturn(new BufferedImage(1,1,1));
             })) {
            
            PDDocument doc = mock(PDDocument.class);
            loaderMock.when(() -> Loader.loadPDF(any(byte[].class))).thenReturn(doc);
            
            // AI returns JSON with single quotes
            String aiResponse = "{'html': '<div>Single</div>', 'css': '.single {}'}";
            Generation gen = new Generation(new AssistantMessage(aiResponse));
            when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
            
            var resp = aiService.extractTemplateFromPdf(file);
            assertEquals("<div>Single</div>", resp.getHtmlLayout());
            assertEquals(".single {}", resp.getCssStyles());
        }
    }

    /**
     * Verifies that PDF extraction failures (e.g., corrupt files) result in a RuntimeException.
     */
    @Test
    void testExtractTemplateFromPdf_Failure() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "bad data".getBytes());
        // No mocks for Loader, it will fail
        assertThrows(RuntimeException.class, () -> aiService.extractTemplateFromPdf(file));
    }

    // ── STATS & HISTORY ─────────────────────────────────────────────────────

    @Test
    void testGetUsageStats() {
        AiHistory h1 = AiHistory.builder().userId("u1").modelUsed("Groq").build();
        AiHistory h2 = AiHistory.builder().userId("u2").modelUsed("Groq").build();
        when(aiHistoryRepository.findAll()).thenReturn(List.of(h1, h2));

        Map<String, Object> stats = aiService.getUsageStats();
        assertEquals(2L, stats.get("totalAiCalls"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topUsers = (List<Map<String, Object>>) stats.get("topUsersByUsage");
        assertEquals(2, topUsers.size());
    }

    @Test
    void testGetUserHistory_Success() {
        when(aiHistoryRepository.findByUserIdOrderByCreatedAtDesc("user1")).thenReturn(List.of(
                AiHistory.builder().id(1L).userId("user1").actionType("TASK").tokensUsed(100).build()
        ));
        
        var result = aiService.getUserHistory("user1");
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).get("tokensUsed"));
    }

    @Test
    void testGetHistory_NullUserId() {
        assertTrue(aiService.getUserHistory(null).isEmpty());
    }

    // ── EDGE CASES & PRIVATE METHODS (via Reflection) ───────────────────────

    @Test
    void testParseScore_Variations() {
        assertEquals(75, (int) ReflectionTestUtils.invokeMethod(aiService, "parseScore", "Score is 75%"));
        assertEquals(100, (int) ReflectionTestUtils.invokeMethod(aiService, "parseScore", 120));
        assertEquals(0, (int) ReflectionTestUtils.invokeMethod(aiService, "parseScore", -10));
        assertEquals(0, (int) ReflectionTestUtils.invokeMethod(aiService, "parseScore", "no number"));
        assertEquals(0, (int) ReflectionTestUtils.invokeMethod(aiService, "parseScore", (Object) null));
    }

    @Test
    void testToStringList_Variations() {
        List<String> input = List.of(" a ", "", "b");
        List<String> result = ReflectionTestUtils.invokeMethod(aiService, "toStringList", input);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));

        assertTrue(((List<?>)ReflectionTestUtils.invokeMethod(aiService, "toStringList", "not a list")).isEmpty());
    }

    @Test
    void testFirstNonBlank() {
        String res = ReflectionTestUtils.invokeMethod(aiService, "firstNonBlank", (Object) new String[]{null, " ", "found", "ignored"});
        assertEquals("found", res);
        
        assertEquals("", ReflectionTestUtils.invokeMethod(aiService, "firstNonBlank", (Object) new String[]{null, ""}));
    }

    @Test
    void testValidateQuota_Exceeded() {
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(UserQuota.builder().remainingSummaryCount(0).build()));
        AiRequest req = AiRequest.builder().userId("u1").build();
        assertThrows(RuntimeException.class, () -> aiService.generateSummary(req));
    }

    @Test
    void testValidatePremium_Locked() {
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(UserQuota.builder().isPremium(false).build()));
        AiRequest req = AiRequest.builder().userId("u1").build();
        assertThrows(RuntimeException.class, () -> aiService.improveSection(req));
    }

    @Test
    void testValidateQuota_AtsExceeded() {
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(UserQuota.builder().remainingAtsCount(0).build()));
        AiRequest req = AiRequest.builder().userId("u1").build();
        assertThrows(RuntimeException.class, () -> aiService.checkAtsCompatibility(req));
    }

    @Test
    void testConsumeQuota_Summary() {
        UserQuota quota = spy(UserQuota.builder().userId("u1").remainingSummaryCount(5).build());
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(quota));
        
        // Use a private method invocation or just call a method that triggers consumeQuota
        AiRequest req = AiRequest.builder().userId("u1").build();
        // Setup AI mock
        Generation gen = new Generation(new AssistantMessage("Content"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
        
        aiService.generateSummary(req);
        
        verify(quota).setRemainingSummaryCount(4);
        verify(userQuotaRepository).save(quota);
    }

    @Test
    void testConsumeQuota_Ats() {
        UserQuota quota = spy(UserQuota.builder().userId("u1").remainingAtsCount(3).build());
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(quota));
        
        AiRequest req = AiRequest.builder().userId("u1").build();
        String json = "{\"score\": 80}";
        Generation gen = new Generation(new AssistantMessage(json));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
        
        aiService.checkAtsCompatibility(req);
        
        verify(quota).setRemainingAtsCount(2);
        verify(userQuotaRepository).save(quota);
    }

    @Test
    void testGetOrCreateQuota_NullUserId() {
        when(userQuotaRepository.findById("anonymous")).thenReturn(Optional.empty());
        when(userQuotaRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        
        // This is private, but validateQuota calls it
        ReflectionTestUtils.invokeMethod(aiService, "getOrCreateQuota", (Object) null);
        verify(userQuotaRepository).findById("anonymous");
        verify(userQuotaRepository).save(argThat(q -> q.getUserId().equals("anonymous")));
    }

    // ── AI COMMUNICATION FAILURES ───────────────────────────────────────────

    @Test
    void testCallAi_EmptyResponse() {
        Generation gen = new Generation(new AssistantMessage(""));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
        
        AiRequest req = AiRequest.builder().userId("u1").build();
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(UserQuota.builder().userId("u1").remainingSummaryCount(5).isPremium(false).build()));
        
        assertThrows(RuntimeException.class, () -> aiService.generateSummary(req));
    }

    @Test
    void testCallAi_UsageMetadataNull() {
        AssistantMessage msg = new AssistantMessage("Success");
        Generation gen = new Generation(msg);
        ChatResponse resp = new ChatResponse(List.of(gen)); 
        when(chatModel.call(any(Prompt.class))).thenReturn(resp);
        
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(UserQuota.builder().userId("u1").remainingSummaryCount(5).build()));
        
        var result = aiService.generateSummary(AiRequest.builder().userId("u1").build());
        assertEquals(0, result.get("tokensUsed"));
    }

    @Test
    void testCallAi_GenericException() {
        // Use doAnswer to throw a checked exception and trigger the friendly error message
        doAnswer(invocation -> {
            throw new Exception("API error");
        }).when(chatModel).call(any(Prompt.class));
        
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(UserQuota.builder().userId("u1").remainingSummaryCount(5).build()));
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> aiService.generateSummary(AiRequest.builder().userId("u1").build()));
        assertTrue(ex.getMessage().contains("temporarily unavailable"));
    }

    @Test
    void testCallAi_SystemUser_NoHistorySaved() {
        Generation gen = new Generation(new AssistantMessage("Skills"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
        
        aiService.suggestSkills(1L, "Java");
        
        verify(aiHistoryRepository, never()).save(any());
    }

    @Test
    void testCallAi_NullUserId_NoHistorySaved() {
        Generation gen = new Generation(new AssistantMessage("Content"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
        
        // Pass null userId to trigger the branch in finally block
        ReflectionTestUtils.invokeMethod(aiService, "callAiAndSaveHistory", "prompt", (String) null, "TASK");
        
        verify(aiHistoryRepository, never()).save(any());
    }

    @Test
    void testCallAi_HistorySaveFailure_DoesNotCrash() {
        Generation gen = new Generation(new AssistantMessage("Content"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(UserQuota.builder().userId("u1").remainingSummaryCount(5).build()));
        
        doThrow(new RuntimeException("DB Down")).when(aiHistoryRepository).save(any());
        
        assertDoesNotThrow(() -> aiService.generateSummary(AiRequest.builder().userId("u1").build()));
    }

    // ── PARSING VARIATIONS ──────────────────────────────────────────────────

    @Test
    void testParseAtsReport_MarkdownBlocks() {
        // Logic expects string to START with ```
        String jsonWithMarkdown = "```json\n{\"score\": 90, \"suggestions\": [\"Great\"]}\n```";
        Map<String, Object> raw = new HashMap<>();
        raw.put("content", jsonWithMarkdown);
        
        Map<String, Object> result = ReflectionTestUtils.invokeMethod(aiService, "parseAtsReport", raw);
        assertEquals(90, result.get("score"));
        @SuppressWarnings("unchecked")
        List<String> suggs = (List<String>) result.get("suggestions");
        assertEquals("Great", suggs.get(0));
    }

    @Test
    void testParseAtsReport_InvalidJsonFallback() {
        String invalidJson = "This is not json at all.\nLine 1\nLine 2";
        Map<String, Object> raw = new HashMap<>();
        raw.put("content", invalidJson);
        
        Map<String, Object> result = ReflectionTestUtils.invokeMethod(aiService, "parseAtsReport", raw);
        assertEquals(0, result.get("score"));
        @SuppressWarnings("unchecked")
        List<String> suggs = (List<String>) result.get("suggestions");
        assertTrue(suggs.size() >= 2);
    }

    @Test
    void testParseAtsReport_NotAString() {
        Map<String, Object> raw = new HashMap<>();
        raw.put("content", 12345); // Integer instead of String
        
        Map<String, Object> result = ReflectionTestUtils.invokeMethod(aiService, "parseAtsReport", raw);
        assertEquals(0, result.get("score"));
        assertTrue(((List<?>)result.get("suggestions")).get(0).toString().contains("did not return a valid"));
    }

    @Test
    void testUsageStats_NullFields() {
        AiHistory h = new AiHistory(); // All fields null
        when(aiHistoryRepository.findAll()).thenReturn(List.of(h));
        
        Map<String, Object> stats = aiService.getUsageStats();
        assertNotNull(stats);
        assertEquals(1L, stats.get("totalAiCalls"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topUsers = (List<Map<String, Object>>) stats.get("topUsersByUsage");
        assertEquals("anonymous", topUsers.get(0).get("userId"));
    }

    @Test
    void testExtractTemplateFromPdf_NoJsonMarkers() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        try (MockedStatic<Loader> loaderMock = mockStatic(Loader.class);
             MockedStatic<javax.imageio.ImageIO> imageIoMock = mockStatic(javax.imageio.ImageIO.class);
             MockedConstruction<PDFTextStripper> stripperMock = mockConstruction(PDFTextStripper.class, (mock, context) -> {
                 when(mock.getText(any())).thenReturn("Text");
             });
             MockedConstruction<PDFRenderer> rendererMock = mockConstruction(PDFRenderer.class, (mock, context) -> {
                 when(mock.renderImageWithDPI(anyInt(), anyInt())).thenReturn(new BufferedImage(1,1,1));
             })) {
            
            PDDocument doc = mock(PDDocument.class);
            loaderMock.when(() -> Loader.loadPDF(any(byte[].class))).thenReturn(doc);
            
            // AI returns text without { } markers
            Generation gen = new Generation(new AssistantMessage("Plain text response"));
            when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
            
            var resp = aiService.extractTemplateFromPdf(file);
            assertEquals("Plain text response", resp.getHtmlLayout());
        }
    }

    @Test
    void testExtractTemplateFromPdf_IncompleteJsonMarkers() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        try (MockedStatic<Loader> loaderMock = mockStatic(Loader.class);
             MockedStatic<javax.imageio.ImageIO> imageIoMock = mockStatic(javax.imageio.ImageIO.class);
             MockedConstruction<PDFTextStripper> stripperMock = mockConstruction(PDFTextStripper.class, (mock, context) -> {
                 when(mock.getText(any())).thenReturn("Text");
             });
             MockedConstruction<PDFRenderer> rendererMock = mockConstruction(PDFRenderer.class, (mock, context) -> {
                 when(mock.renderImageWithDPI(anyInt(), anyInt())).thenReturn(new BufferedImage(1,1,1));
             })) {
            
            PDDocument doc = mock(PDDocument.class);
            loaderMock.when(() -> Loader.loadPDF(any(byte[].class))).thenReturn(doc);
            
            // AI returns only { without }
            Generation gen = new Generation(new AssistantMessage("Only { bracket"));
            when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
            
            var resp = aiService.extractTemplateFromPdf(file);
            assertEquals("Only { bracket", resp.getHtmlLayout());
        }
    }

    /**
     * Verifies that completely empty AI responses result in a friendly error template.
     */
    @Test
    void testExtractTemplateFromPdf_EmptyAiResponse() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        try (MockedStatic<Loader> loaderMock = mockStatic(Loader.class);
             MockedStatic<javax.imageio.ImageIO> imageIoMock = mockStatic(javax.imageio.ImageIO.class);
             MockedConstruction<PDFTextStripper> stripperMock = mockConstruction(PDFTextStripper.class, (mock, context) -> {
                 when(mock.getText(any())).thenReturn("Text");
             });
             MockedConstruction<PDFRenderer> rendererMock = mockConstruction(PDFRenderer.class, (mock, context) -> {
                 when(mock.renderImageWithDPI(anyInt(), anyInt())).thenReturn(new BufferedImage(1,1,1));
             })) {
            
            PDDocument doc = mock(PDDocument.class);
            loaderMock.when(() -> Loader.loadPDF(any(byte[].class))).thenReturn(doc);
            
            // AI returns empty response
            Generation gen = new Generation(new AssistantMessage(""));
            when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(gen)));
            
            var resp = aiService.extractTemplateFromPdf(file);
            assertTrue(resp.getHtmlLayout().contains("Template generation failed"));
        }
    }

    @Test
    void testConsumeQuota_NoOpIfZero() {
        // Summary quota already 0
        UserQuota quota = spy(UserQuota.builder().userId("u1").remainingSummaryCount(0).build());
        when(userQuotaRepository.findById("u1")).thenReturn(Optional.of(quota));
        
        ReflectionTestUtils.invokeMethod(aiService, "consumeQuota", "u1", "SUMMARY");
        
        verify(quota, never()).setRemainingSummaryCount(anyInt());
        verify(userQuotaRepository, never()).save(any());
        
        // ATS quota already 0
        ReflectionTestUtils.invokeMethod(aiService, "consumeQuota", "u1", "ATS");
        verify(quota, never()).setRemainingAtsCount(anyInt());
    }
}
