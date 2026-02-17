package com.axiom.RagChat.summary.service;

import com.axiom.RagChat.session.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RollingCompressionService {

    private final SummaryService summaryService;

    @Value("${ragchat.summary.trigger-message-count:20}")
    private int triggerMessageCount;

    @Async
    public void checkAndCompress(ChatSession session) {
        if (session.getMessageCount() % triggerMessageCount == 0) {
            log.info("Triggering rolling compression for session {}", session.getId());
            summaryService.generateRollingSummary(session.getId());
        }
    }
}