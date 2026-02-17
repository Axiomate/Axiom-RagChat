package com.axiom.RagChat;

import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.repository.SessionRepository;
import com.axiom.RagChat.session.service.SessionService;
import com.axiom.RagChat.user.entity.User;
import com.axiom.RagChat.user.service.UserService;
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
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SessionService sessionService;

    private User testUser;
    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .build();

        testSession = ChatSession.builder()
            .id(1L)
            .user(testUser)
            .title("Test Session")
            .status(ChatSession.SessionStatus.ACTIVE)
            .build();
    }

    @Test
    void testCreateSession() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        ChatSession created = sessionService.createSession("Test Session");

        assertNotNull(created);
        assertEquals("Test Session", created.getTitle());
        verify(sessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    void testEndSession() {
        when(sessionRepository.findById(1L)).thenReturn(java.util.Optional.of(testSession));
        when(userService.getCurrentUser()).thenReturn(testUser);

        sessionService.endSession(1L);

        assertEquals(ChatSession.SessionStatus.FINALIZED, testSession.getStatus());
        assertNotNull(testSession.getEndedAt());
        verify(eventPublisher, times(1)).publishEvent(any());
    }
}
