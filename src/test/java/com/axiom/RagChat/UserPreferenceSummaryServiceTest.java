package com.axiom.RagChat;

import com.axiom.RagChat.ai.GeminiService;
import com.axiom.RagChat.ai.PromptTemplateLibrary;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.repository.SessionRepository;
import com.axiom.RagChat.user.entity.User;
import com.axiom.RagChat.user.service.UserPreferenceSummaryService;
import com.axiom.RagChat.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferenceSummaryServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private GeminiService geminiService;

    @Mock
    private PromptTemplateLibrary promptTemplateLibrary;

    @InjectMocks
    private UserPreferenceSummaryService preferenceSummaryService;

    private User testUser;
    private List<ChatSession> testSessions;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .build();

        ChatSession session1 = ChatSession.builder()
            .id(1L)
            .user(testUser)
            .finalSummary("Summary 1")
            .build();

        ChatSession session2 = ChatSession.builder()
            .id(2L)
            .user(testUser)
            .finalSummary("Summary 2")
            .build();

        testSessions = Arrays.asList(session1, session2);
    }

    @Test
    void testAggregateUserPreferences() {
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(sessionRepository.findByUserIdAndFinalSummaryIsNotNull(1L)).thenReturn(testSessions);
        when(promptTemplateLibrary.getUserPreferenceAggregationPrompt(anyString(), anyString()))
            .thenReturn("Test prompt");
        when(geminiService.generateText(anyString())).thenReturn("Aggregated preferences");

        preferenceSummaryService.aggregateUserPreferences(1L);

        verify(userService, times(1)).updatePreferenceSummary(eq(1L), eq("Aggregated preferences"));
    }
}