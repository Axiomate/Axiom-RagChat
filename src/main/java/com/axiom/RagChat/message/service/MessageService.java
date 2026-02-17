package com.axiom.RagChat.message.service;

import com.axiom.RagChat.events.MessageSavedEvent;
import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.repository.MessageRepository;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.service.SessionService;
import com.axiom.RagChat.util.TokenCounter;
import com.axiom.RagChat.vector.EmbeddingOrchestrator;
import com.axiom.RagChat.vector.EmbeddingService;
import com.axiom.RagChat.vector.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final SessionService sessionService;
    private final EmbeddingOrchestrator embeddingOrchestrator;
    private final EmbeddingService embeddingService;    // no cycle — EmbeddingService no longer injects MessageService
    private final VectorStoreService vectorStoreService; // no cycle — VectorStoreService no longer injects EmbeddingService
    private final ApplicationEventPublisher eventPublisher;
    private final TokenCounter tokenCounter;

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    @Transactional
    public ChatMessage saveMessage(Long sessionId, String role, String content) {
        ChatSession session = sessionService.getSession(sessionId);

        int tokens = tokenCounter.countTokens(content);

        ChatMessage message = ChatMessage.builder()
            .session(session)
            .role(role)
            .content(content)
            .tokenCount(tokens)
            .build();

        message = messageRepository.save(message);

        sessionService.incrementMessageCount(sessionId);
        sessionService.addTokenUsage(sessionId, tokens);
        sessionService.updateActivity(sessionId);

        // Async: embed → store in Qdrant → persist embeddingId
        embeddingOrchestrator.generateAndStoreEmbedding(message);

        // Publish event for rolling summary
        eventPublisher.publishEvent(new MessageSavedEvent(this, message));

        log.info("Saved message {} for session {}", message.getId(), sessionId);
        return message;
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    public List<ChatMessage> getSessionMessages(Long sessionId) {
        sessionService.getSession(sessionId);
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public List<ChatMessage> getRecentMessages(Long sessionId, int limit) {
        sessionService.getSession(sessionId);
        return messageRepository.findTop20BySessionIdOrderByCreatedAtDesc(sessionId);
    }

    public ChatMessage getMessage(Long messageId) {
        return messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    /**
     * Semantic search: converts the query string to an embedding, then delegates
     * the vector search to VectorStoreService. The controller never touches either
     * EmbeddingService or VectorStoreService directly.
     */
    public List<ChatMessage> searchSimilarMessages(Long sessionId, String query, int limit) {
        sessionService.getSession(sessionId); // verify session access
        float[] queryEmbedding = embeddingService.generateEmbedding(query);
        return vectorStoreService.searchSimilarMessages(sessionId, queryEmbedding, limit);
    }
}