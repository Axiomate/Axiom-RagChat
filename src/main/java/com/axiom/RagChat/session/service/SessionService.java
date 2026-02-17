package com.axiom.RagChat.session.service;

import com.axiom.RagChat.cache.SessionCacheService;
import com.axiom.RagChat.cache.SessionCacheService.SessionCacheDto;
import com.axiom.RagChat.events.SessionTerminationEvent;
import com.axiom.RagChat.exception.ApiException;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.repository.SessionRepository;
import com.axiom.RagChat.user.entity.User;
import com.axiom.RagChat.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final SessionCacheService cacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatSession createSession(String title) {
        User user = userService.getCurrentUser();

        ChatSession session = ChatSession.builder()
            .user(user)
            .title(title != null ? title : "New Chat")
            .status(ChatSession.SessionStatus.ACTIVE)
            .lastActivityAt(LocalDateTime.now())
            .build();

        session = sessionRepository.save(session);
        userService.incrementSessionCount(user.getId());

        cacheService.cacheSession(session);

        log.info("Created session {} for user {}", session.getId(), user.getId());
        return session;
    }

    public ChatSession getSession(Long sessionId) {
        // Check cache first — avoids DB hit on hot sessions
        SessionCacheDto cached = cacheService.getCachedSession(sessionId);
        if (cached != null) {
            // Still verify ownership against cached userId
            User currentUser = userService.getCurrentUser();
            if (!cached.getUserId().equals(currentUser.getId())) {
                throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
            }
            // Fetch full entity from DB (cache only holds metadata)
            return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Session not found", HttpStatus.NOT_FOUND));
        }

        // Cache miss — fetch from DB
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ApiException("Session not found", HttpStatus.NOT_FOUND));

        // Verify ownership
        User currentUser = userService.getCurrentUser();
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        cacheService.cacheSession(session);
        return session;
    }

    public Page<ChatSession> getUserSessions(Pageable pageable) {
        User user = userService.getCurrentUser();
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId(), pageable);
    }

    @Transactional
    public void updateActivity(Long sessionId) {
        ChatSession session = getSession(sessionId);
        session.updateActivity();
        sessionRepository.save(session);
        cacheService.cacheSession(session);
    }

    @Transactional
    public void endSession(Long sessionId) {
        ChatSession session = getSession(sessionId);

        if (session.getStatus() == ChatSession.SessionStatus.FINALIZED) {
            throw new ApiException("Session already finalized", HttpStatus.BAD_REQUEST);
        }

        session.setStatus(ChatSession.SessionStatus.FINALIZED);
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);

        eventPublisher.publishEvent(new SessionTerminationEvent(this, session));

        cacheService.evictSession(sessionId);

        log.info("Session {} ended", sessionId);
    }

    @Transactional
    public void updateRollingSummary(Long sessionId, String summary) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ApiException("Session not found", HttpStatus.NOT_FOUND));

        session.setRollingSummary(summary);
        session.setLastSummaryAt(LocalDateTime.now());
        sessionRepository.save(session);

        cacheService.cacheSession(session);
    }

    @Transactional
    public void updateFinalSummary(Long sessionId, String summary) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ApiException("Session not found", HttpStatus.NOT_FOUND));

        session.setFinalSummary(summary);
        sessionRepository.save(session);
    }

    @Transactional
    public void incrementMessageCount(Long sessionId) {
        ChatSession session = getSession(sessionId);
        session.incrementMessageCount();
        sessionRepository.save(session);
        cacheService.cacheSession(session);
    }

    @Transactional
    public void addTokenUsage(Long sessionId, long tokens) {
        ChatSession session = getSession(sessionId);
        session.addTokens(tokens);
        sessionRepository.save(session);
    }
}