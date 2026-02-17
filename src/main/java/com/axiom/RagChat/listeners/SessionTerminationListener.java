package com.axiom.RagChat.listeners;

import com.axiom.RagChat.events.SessionTerminationEvent;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.service.SessionFinalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionTerminationListener {

    private final SessionFinalizationService finalizationService;

    @EventListener
    @Async
    public void handleSessionTermination(SessionTerminationEvent event) {
        ChatSession session = event.getSession();
        
        log.info("Session termination event received for session {}", session.getId());
        
        // Trigger finalization process
        finalizationService.finalizeSession(session);
    }
}
