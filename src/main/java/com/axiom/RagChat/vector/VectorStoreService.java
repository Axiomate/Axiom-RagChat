package com.axiom.RagChat.vector;

import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.repository.MessageRepository;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.FieldCondition;
import io.qdrant.client.grpc.Points.Match;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.JsonWithInt.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final QdrantClient qdrantClient;
    private final String qdrantCollectionName;
    private final MessageRepository messageRepository;
    // EmbeddingService removed — this class only speaks in raw float[] vectors

    private static final int VECTOR_DIMENSIONS = 768;
    private static final float SIMILARITY_THRESHOLD = 0.7f;

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @PostConstruct
    public void initializeCollection() {
        try {
            List<String> existingCollections = qdrantClient.listCollectionsAsync().get();
            boolean exists = existingCollections.contains(qdrantCollectionName);

            if (!exists) {
                log.info("Creating Qdrant collection: {}", qdrantCollectionName);
                qdrantClient.createCollectionAsync(qdrantCollectionName,
                        VectorParams.newBuilder()
                                .setSize(VECTOR_DIMENSIONS)
                                .setDistance(Distance.Cosine)
                                .build())
                        .get();
                log.info("Qdrant collection '{}' created successfully", qdrantCollectionName);
            } else {
                log.info("Qdrant collection '{}' already exists", qdrantCollectionName);
            }
        } catch (Exception e) {
            log.error("Error initialising Qdrant collection '{}'", qdrantCollectionName, e);
        }
    }

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    public void storeEmbedding(String embeddingId, float[] embedding, Long sessionId, Long messageId, String content) {
        try {
            Map<String, Value> payload = new HashMap<>();
            payload.put("session_id", Value.newBuilder().setIntegerValue(sessionId).build());
            payload.put("message_id", Value.newBuilder().setIntegerValue(messageId).build());
            payload.put("content", Value.newBuilder().setStringValue(content).build());

            List<Float> vectorList = toFloatList(embedding);

            io.qdrant.client.grpc.Points.Vectors vectors = io.qdrant.client.grpc.Points.Vectors.newBuilder()
                    .setVector(io.qdrant.client.grpc.Points.Vector.newBuilder()
                            .addAllData(vectorList)
                            .build())
                    .build();

            PointStruct point = PointStruct.newBuilder()
                    .setId(PointId.newBuilder().setUuid(embeddingId).build())
                    .setVectors(vectors)
                    .putAllPayload(payload)
                    .build();

            qdrantClient.upsertAsync(qdrantCollectionName, Collections.singletonList(point)).get();
            log.info("Stored embedding '{}' for message {} in session {}", embeddingId, messageId, sessionId);
        } catch (Exception e) {
            log.error("Error storing embedding '{}' in Qdrant", embeddingId, e);
            throw new RuntimeException("Failed to store embedding", e);
        }
    }

    // -------------------------------------------------------------------------
    // Read / Search
    // -------------------------------------------------------------------------

    /**
     * Searches for similar messages using a pre-computed query embedding.
     * Callers are responsible for generating the embedding via EmbeddingService
     * before calling this method — keeping the two services decoupled.
     *
     * @param sessionId     the session to scope the search to
     * @param queryEmbedding pre-computed embedding vector for the query
     * @param limit         max number of results to return
     * @return list of matching ChatMessages ordered by similarity
     */
    public List<ChatMessage> searchSimilarMessages(Long sessionId, float[] queryEmbedding, int limit) {
        try {
            Filter sessionFilter = Filter.newBuilder()
                    .addMust(io.qdrant.client.grpc.Points.Condition.newBuilder()
                            .setField(FieldCondition.newBuilder()
                                    .setKey("session_id")
                                    .setMatch(Match.newBuilder().setInteger(sessionId).build())
                                    .build())
                            .build())
                    .build();

            SearchPoints searchRequest = SearchPoints.newBuilder()
                    .setCollectionName(qdrantCollectionName)
                    .addAllVector(toFloatList(queryEmbedding))
                    .setFilter(sessionFilter)
                    .setLimit(limit)
                    .setScoreThreshold(SIMILARITY_THRESHOLD)
                    .setWithPayload(io.qdrant.client.grpc.Points.WithPayloadSelector.newBuilder()
                            .setEnable(true)
                            .build())
                    .build();

            List<ScoredPoint> results = qdrantClient.searchAsync(searchRequest).get();

            if (results.isEmpty()) {
                log.info("No similar messages found for query in session {}", sessionId);
                return Collections.emptyList();
            }

            List<Long> messageIds = results.stream()
                    .map(scored -> {
                        Value val = scored.getPayloadMap().get("message_id");
                        return val != null ? val.getIntegerValue() : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("Found {} similar messages in session {}", messageIds.size(), sessionId);
            return messageRepository.findAllById(messageIds);

        } catch (Exception e) {
            log.error("Error searching similar messages in session {}", sessionId, e);
            return Collections.emptyList();
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    public void deleteSessionEmbeddings(Long sessionId) {
        try {
            Filter sessionFilter = Filter.newBuilder()
                    .addMust(io.qdrant.client.grpc.Points.Condition.newBuilder()
                            .setField(FieldCondition.newBuilder()
                                    .setKey("session_id")
                                    .setMatch(Match.newBuilder().setInteger(sessionId).build())
                                    .build())
                            .build())
                    .build();

            qdrantClient.deleteAsync(qdrantCollectionName, sessionFilter).get();
            log.info("Deleted all embeddings for session {}", sessionId);
        } catch (Exception e) {
            log.error("Error deleting embeddings for session {}", sessionId, e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<Float> toFloatList(float[] array) {
        List<Float> list = new ArrayList<>(array.length);
        for (float v : array) {
            list.add(v);
        }
        return list;
    }
}