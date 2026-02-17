package com.axiom.RagChat.listeners;

import com.axiom.RagChat.events.MessageSavedEvent;
import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.service.SessionService;
import com.axiom.RagChat.summary.service.RollingCompressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageSavedListener {

    private final RollingCompressionService rollingCompressionService;
    private final SessionService sessionService;

    @EventListener
    @Async
    public void handleMessageSaved(MessageSavedEvent event) {
        ChatMessage message = event.getMessage();
        ChatSession session = sessionService.getSession(message.getSession().getId());
        
        log.info("Message saved event received for session {}", session.getId());
        
        // Check if rolling compression should be triggered
        rollingCompressionService.checkAndCompress(session);
    }
}
