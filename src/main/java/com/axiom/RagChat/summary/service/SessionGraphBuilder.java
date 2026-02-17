package com.axiom.RagChat.summary.service;

import com.axiom.RagChat.message.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SessionGraphBuilder {

    /**
     * Builds a conversation graph showing message flow and topics
     */
    public Map<String, Object> buildConversationGraph(List<ChatMessage> messages) {
        Map<String, Object> graph = new HashMap<>();
        
        List<Map<String, Object>> nodes = messages.stream()
            .map(msg -> {
                Map<String, Object> node = new HashMap<>();
                node.put("id", msg.getId());
                node.put("role", msg.getRole());
                node.put("contentPreview", truncate(msg.getContent(), 100));
                node.put("timestamp", msg.getCreatedAt());
                node.put("tokenCount", msg.getTokenCount());
                return node;
            })
            .collect(Collectors.toList());

        graph.put("nodes", nodes);
        graph.put("totalMessages", messages.size());
        graph.put("totalTokens", messages.stream()
            .mapToInt(m -> m.getTokenCount() != null ? m.getTokenCount() : 0)
            .sum());

        return graph;
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}