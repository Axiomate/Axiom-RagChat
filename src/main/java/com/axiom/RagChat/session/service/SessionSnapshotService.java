package com.axiom.RagChat.session.service;

import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.repository.MessageRepository;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.repository.SessionRepository;
import com.axiom.RagChat.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionSnapshotService {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public void createSnapshot(ChatSession session) {
        try {
            // Get last N messages for context
            List<ChatMessage> recentMessages = messageRepository
                .findTop20BySessionIdOrderByCreatedAtDesc(session.getId());

            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("sessionId", session.getId());
            snapshot.put("title", session.getTitle());
            snapshot.put("rollingSummary", session.getRollingSummary());
            snapshot.put("finalSummary", session.getFinalSummary());
            snapshot.put("messageCount", session.getMessageCount());
            snapshot.put("recentMessages", recentMessages.stream()
                .map(msg -> Map.of(
                    "role", msg.getRole(),
                    "content", msg.getContent(),
                    "timestamp", msg.getCreatedAt().toString()
                ))
                .collect(Collectors.toList()));

            String snapshotJson = JsonUtils.toJson(snapshot);
            session.setSessionSnapshot(snapshotJson);
            sessionRepository.save(session);

            log.info("Snapshot created for session: {}", session.getId());
        } catch (Exception e) {
            log.error("Error creating snapshot for session: {}", session.getId(), e);
        }
    }

    public Map<String, Object> loadSnapshot(Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getSessionSnapshot() == null) {
            return null;
        }

        return JsonUtils.fromJson(session.getSessionSnapshot(), Map.class);
    }
}
