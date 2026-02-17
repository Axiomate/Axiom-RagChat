package com.axiom.RagChat.message.service;

import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.repository.MessageRepository;
import com.axiom.RagChat.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessagePaginationService {

    private final MessageRepository messageRepository;
    private final SessionService sessionService;

    public Page<ChatMessage> getMessagesPaginated(Long sessionId, Pageable pageable) {
        sessionService.getSession(sessionId); // Verify access
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, pageable);
    }
}
