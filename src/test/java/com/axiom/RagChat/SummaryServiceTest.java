package com.axiom.RagChat;

import com.axiom.RagChat.ai.GeminiService;
import com.axiom.RagChat.ai.PromptTemplateLibrary;
import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.repository.MessageRepository;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.service.SessionService;
import com.axiom.RagChat.summary.service.SummaryService;
import com.axiom.RagChat.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private GeminiService geminiService;

    @Mock
    private PromptTemplateLibrary promptTemplateLibrary;

    @InjectMocks
    private SummaryService summaryService;

    private ChatSession testSession;
    private List<ChatMessage> testMessages;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .build();

        testSession = ChatSession.builder()
            .id(1L)
            .user(testUser)
            .title("Test Session")
            .build();

        ChatMessage msg1 = ChatMessage.builder()
            .id(1L)
            .session(testSession)
            .role("USER")
            .content("Hello")
            .build();

        ChatMessage msg2 = ChatMessage.builder()
            .id(2L)
            .session(testSession)
            .role("ASSISTANT")
            .content("Hi there!")
            .build();

        testMessages = Arrays.asList(msg1, msg2);
    }

    @Test
    void testGenerateRollingSummary() {
        when(sessionService.getSession(1L)).thenReturn(testSession);
        when(messageRepository.findMessagesSince(anyLong(), any())).thenReturn(testMessages);
        when(promptTemplateLibrary.getRollingSummaryPrompt(any(), any())).thenReturn("Test prompt");
        when(geminiService.generateText(anyString())).thenReturn("Generated summary");

        String summary = summaryService.generateRollingSummary(1L);

        assertNotNull(summary);
        assertEquals("Generated summary", summary);
        verify(sessionService, times(1)).updateRollingSummary(eq(1L), anyString());
    }
}
