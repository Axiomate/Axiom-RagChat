package com.axiom.RagChat.vector;

import com.axiom.RagChat.ai.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final GeminiService geminiService;
    // No MessageService — avoids EmbeddingService ↔ MessageService cycle
    // No VectorStoreService — avoids EmbeddingService ↔ VectorStoreService cycle

    public float[] generateEmbedding(String text) {
        return geminiService.generateEmbedding(text);
    }
}