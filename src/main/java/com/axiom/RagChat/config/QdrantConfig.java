package com.axiom.RagChat.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class QdrantConfig {

    @Value("${qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${qdrant.port:6334}")
    private int qdrantPort;

    @Value("${qdrant.collection-name:chat_embeddings}")
    private String collectionName;

    @Bean
    public QdrantClient qdrantClient() {
        int maxRetries = 10;
        int delayMs = 3000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Connecting to Qdrant at {}:{} (attempt {}/{})",
                        qdrantHost, qdrantPort, attempt, maxRetries);

                QdrantGrpcClient grpcClient = QdrantGrpcClient
                        .newBuilder(qdrantHost, qdrantPort, false)
                        .build();

                QdrantClient client = new QdrantClient(grpcClient);

                // Test the connection
                client.listCollectionsAsync().get();

                log.info("Successfully connected to Qdrant");
                return client;

            } catch (Exception e) {
                log.warn("Qdrant not ready yet (attempt {}/{}): {}",
                        attempt, maxRetries, e.getMessage());

                if (attempt == maxRetries) {
                    throw new RuntimeException(
                            "Failed to connect to Qdrant after " + maxRetries + " attempts", e);
                }

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for Qdrant", ie);
                }
            }
        }

        throw new RuntimeException("Could not connect to Qdrant");
    }

    @Bean
    public String qdrantCollectionName() {
        return collectionName;
    }
}