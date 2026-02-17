package com.axiom.RagChat.config;

import com.google.cloud.vertexai.VertexAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.project-id}")
    private String projectId;

    @Value("${gemini.location:us-central1}")
    private String location;

    @Value("${gemini.model:gemini-2.0-flash-exp}")
    private String model;

    @Bean
    public VertexAI vertexAI() {
        return new VertexAI(projectId, location);
    }

    @Bean
    public String geminiModel() {
        return model;
    }
}
