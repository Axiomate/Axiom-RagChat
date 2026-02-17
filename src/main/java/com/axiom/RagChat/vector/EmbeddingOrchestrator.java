package com.axiom.RagChat.vector;

import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Orchestrates the async embed → store → record flow for a saved message.
 *
 * Intentionally separate from MessageService to:
 *   1. Break the MessageService ↔ EmbeddingService circular dependency.
 *   2. Ensure @Async is honoured (self-calls on the same bean bypass the proxy).
 *
 * Dependency graph (no cycles):
 *   MessageService → EmbeddingOrchestrator → EmbeddingService  → GeminiService
 *                                          → VectorStoreService → QdrantClient
 *                                          → MessageRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingOrchestrator {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final MessageRepository messageRepository;

    @Async
    @Transactional
    public void generateAndStoreEmbedding(ChatMessage message) {
        try {
            log.info("Generating embedding for message {}", message.getId());

            // 1. Text → float[]
            float[] embedding = embeddingService.generateEmbedding(message.getContent());

            // 2. float[] → Qdrant
            String embeddingId = UUID.randomUUID().toString();
            vectorStoreService.storeEmbedding(
                embeddingId,
                embedding,
                message.getSession().getId(),
                message.getId(),
                message.getContent()
            );

            // 3. Persist the embeddingId back to the message record directly —
            //    no MessageService call needed, avoiding the cycle entirely.
            messageRepository.findById(message.getId()).ifPresent(m -> {
                m.setEmbeddingId(embeddingId);
                messageRepository.save(m);
            });

            log.info("Embedding stored for message {}", message.getId());
        } catch (Exception e) {
            log.error("Error generating embedding for message {}", message.getId(), e);
        }
    }
}