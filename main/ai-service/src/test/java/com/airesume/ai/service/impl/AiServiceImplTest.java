package com.airesume.ai.service.impl;

import com.airesume.ai.dto.AiRequest;
import com.airesume.ai.entity.UserQuota;
import com.airesume.ai.repository.AiHistoryRepository;
import com.airesume.ai.repository.UserQuotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.test.util.ReflectionTestUtils;
import com.airesume.ai.security.CurrentUserService;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private ChatModel geminiModel;

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

    @Test
    void testGenerateSummary_Success_FreeUser() {
        // Arrange
        String userId = "testUser";
        AiRequest request = new AiRequest();
        request.setUserId(userId);
        request.setTargetJobTitle("Software Engineer");
        request.setExistingContent("Worked with Java");
        request.setTone("Professional");

        UserQuota quota = new UserQuota();
        quota.setUserId(userId);
        quota.setRemainingSummaryCount(5);
        quota.setPremium(false);

        when(userQuotaRepository.findById(userId)).thenReturn(Optional.of(quota));
        
        // Mock AI Response
        Generation generation = new Generation(new org.springframework.ai.chat.messages.AssistantMessage("Generated Professional Summary"));
        ChatResponse chatResponse = new ChatResponse(java.util.List.of(generation));
        when(geminiModel.call(any(org.springframework.ai.chat.prompt.Prompt.class))).thenReturn(chatResponse);

        // Act
        Map<String, Object> result = aiService.generateSummary(request);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals("Generated Professional Summary", result.get("content"));
        
        // Verify quota was decremented
        assertEquals(4, quota.getRemainingSummaryCount());
        verify(userQuotaRepository, times(1)).save(quota);
        verify(aiHistoryRepository, times(1)).save(any());
    }

    @Test
    void testGenerateSummary_Fail_QuotaExceeded() {
        // Arrange
        String userId = "testUser";
        AiRequest request = new AiRequest();
        request.setUserId(userId);

        UserQuota quota = new UserQuota();
        quota.setUserId(userId);
        quota.setRemainingSummaryCount(0); // Quota empty
        quota.setPremium(false);

        when(userQuotaRepository.findById(userId)).thenReturn(Optional.of(quota));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> aiService.generateSummary(request));
        assertEquals("Summary quota exceeded. Please upgrade to Premium.", exception.getMessage());
        
        // Verify AI was never called
        verify(geminiModel, never()).call(any(org.springframework.ai.chat.prompt.Prompt.class));
    }

    @Test
    void testGenerateCoverLetter_Success_PremiumUser() {
        // Arrange
        String userId = "premiumUser";
        AiRequest request = new AiRequest();
        request.setUserId(userId);

        UserQuota quota = new UserQuota();
        quota.setUserId(userId);
        quota.setPremium(true); // Premium user

        when(userQuotaRepository.findById(userId)).thenReturn(Optional.of(quota));
        when(currentUserService.isPremium()).thenReturn(true);
        
        // Mock AI Response
        Generation generation = new Generation(new org.springframework.ai.chat.messages.AssistantMessage("Generated Cover Letter"));
        ChatResponse chatResponse = new ChatResponse(java.util.List.of(generation));
        when(geminiModel.call(any(org.springframework.ai.chat.prompt.Prompt.class))).thenReturn(chatResponse);

        // Act
        Map<String, Object> result = aiService.generateCoverLetter(request);

        // Assert
        assertNotNull(result);
        assertEquals("Generated Cover Letter", result.get("content"));
        verify(aiHistoryRepository, times(1)).save(any());
    }

    @Test
    void testGenerateCoverLetter_Fail_FreeUser() {
        // Arrange
        String userId = "freeUser";
        AiRequest request = new AiRequest();
        request.setUserId(userId);

        UserQuota quota = new UserQuota();
        quota.setUserId(userId);
        quota.setPremium(false); // Free user

        when(userQuotaRepository.findById(userId)).thenReturn(Optional.of(quota));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> aiService.generateCoverLetter(request));
        assertEquals("Premium feature locked. Please upgrade to Premium to use this feature.", exception.getMessage());
    }
}
