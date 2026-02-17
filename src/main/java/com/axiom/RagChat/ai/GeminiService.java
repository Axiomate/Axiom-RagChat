package com.axiom.RagChat.ai;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final VertexAI vertexAI;
    private final String geminiModel;

    public String generateText(String prompt) {
        try {
            GenerativeModel model = new GenerativeModel(geminiModel, vertexAI);
            
            GenerateContentResponse response = model.generateContent(
                ContentMaker.fromMultiModalData(prompt)
            );
            
            return ResponseHandler.getText(response);
        } catch (Exception e) {
            log.error("Error generating text with Gemini", e);
            throw new RuntimeException("Failed to generate text", e);
        }
    }

    public float[] generateEmbedding(String text) {
        try {
            // Using text-embedding-004 model for embeddings
            GenerativeModel embeddingModel = new GenerativeModel("text-embedding-004", vertexAI);
            
            GenerateContentResponse response = embeddingModel.generateContent(
                ContentMaker.fromMultiModalData(text)
            );
            
            // Extract embedding from response
            // Note: This is a simplified implementation
            // In production, you'd use the proper embedding API
            return extractEmbedding(response);
        } catch (Exception e) {
            log.error("Error generating embedding", e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    private float[] extractEmbedding(GenerateContentResponse response) {
        // Placeholder implementation
        // In production, extract actual embedding values from response
        float[] embedding = new float[768];
        Arrays.fill(embedding, 0.0f);
        return embedding;
    }
}
