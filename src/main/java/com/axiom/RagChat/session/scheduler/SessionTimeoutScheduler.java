package com.axiom.RagChat.session.scheduler;

import com.axiom.RagChat.events.SessionTerminationEvent;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionTimeoutScheduler {

    private final SessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${ragchat.session.timeout-minutes:30}")
    private int timeoutMinutes;

    @Scheduled(fixedDelayString = "${ragchat.session.inactivity-check-minutes:5}000")
    @Transactional
    public void checkInactiveSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        
        List<ChatSession> inactiveSessions = sessionRepository.findInactiveSessions(
            ChatSession.SessionStatus.ACTIVE,
            cutoffTime
        );

        log.info("Found {} inactive sessions to finalize", inactiveSessions.size());

        for (ChatSession session : inactiveSessions) {
            try {
                session.setStatus(ChatSession.SessionStatus.FINALIZED);
                session.setEndedAt(LocalDateTime.now());
                sessionRepository.save(session);

                eventPublisher.publishEvent(new SessionTerminationEvent(this, session));
                
                log.info("Auto-finalized inactive session: {}", session.getId());
            } catch (Exception e) {
                log.error("Error finalizing session: {}", session.getId(), e);
            }
        }
    }
}