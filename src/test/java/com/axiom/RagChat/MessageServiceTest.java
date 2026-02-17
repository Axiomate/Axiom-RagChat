package com.axiom.RagChat;

import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.repository.MessageRepository;
import com.axiom.RagChat.message.service.MessageService;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.service.SessionService;
import com.axiom.RagChat.user.entity.User;
import com.axiom.RagChat.util.TokenCounter;
import com.axiom.RagChat.vector.EmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TokenCounter tokenCounter;

    @InjectMocks
    private MessageService messageService;

    private ChatSession testSession;

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
    }

    @Test
    void testSaveMessage() {
        ChatMessage savedMessage = ChatMessage.builder()
            .id(1L)
            .session(testSession)
            .role("USER")
            .content("Test message")
            .tokenCount(10)
            .build();

        when(sessionService.getSession(1L)).thenReturn(testSession);
        when(tokenCounter.countTokens(anyString())).thenReturn(10);
        when(messageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        ChatMessage result = messageService.saveMessage(1L, "USER", "Test message");

        assertNotNull(result);
        assertEquals("Test message", result.getContent());
        assertEquals(10, result.getTokenCount());
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }
}