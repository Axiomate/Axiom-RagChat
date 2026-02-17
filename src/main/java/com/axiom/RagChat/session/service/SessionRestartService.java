package com.axiom.RagChat.session.service;

import com.axiom.RagChat.exception.ApiException;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.user.entity.User;
import com.axiom.RagChat.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionRestartService {

    private final SessionService sessionService;
    private final SessionSnapshotService snapshotService;
    private final UserService userService;

    @Transactional
    public ChatSession restartFromSnapshot(Long sessionId) {
        // Verify the session exists and user has access
        ChatSession originalSession = sessionService.getSession(sessionId);
        
        if (originalSession.getSessionSnapshot() == null) {
            throw new ApiException("No snapshot available for this session", HttpStatus.BAD_REQUEST);
        }

        // Load snapshot
        Map<String, Object> snapshot = snapshotService.loadSnapshot(sessionId);
        
        User user = userService.getCurrentUser();

        // Create new session with context from snapshot
        ChatSession newSession = ChatSession.builder()
            .user(user)
            .title(snapshot.get("title") + " (Restarted)")
            .rollingSummary((String) snapshot.get("rollingSummary"))
            .status(ChatSession.SessionStatus.ACTIVE)
            .lastActivityAt(LocalDateTime.now())
            .build();

        // Note: We don't copy messages, just the summary context
        log.info("Restarted session {} as new session", sessionId);
        
        return sessionService.createSession(newSession.getTitle());
    }
}
