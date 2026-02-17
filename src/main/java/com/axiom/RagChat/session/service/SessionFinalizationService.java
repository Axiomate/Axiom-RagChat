package com.axiom.RagChat.session.service;

import com.axiom.RagChat.ai.GeminiService;
import com.axiom.RagChat.ai.PromptTemplateLibrary;
import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.repository.MessageRepository;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.user.service.UserPreferenceSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionFinalizationService {

    private final MessageRepository messageRepository;
    private final SessionService sessionService;
    private final SessionSnapshotService snapshotService;
    private final GeminiService geminiService;
    private final PromptTemplateLibrary promptTemplateLibrary;
    private final UserPreferenceSummaryService userPreferenceSummaryService;

    @Async
    @Transactional
    public void finalizeSession(ChatSession session) {
        try {
            log.info("Finalizing session: {}", session.getId());

            // Generate comprehensive final summary
            String finalSummary = generateFinalSummary(session);
            sessionService.updateFinalSummary(session.getId(), finalSummary);

            // Create session snapshot for restart capability
            snapshotService.createSnapshot(session);

            // Trigger user preference aggregation
            userPreferenceSummaryService.aggregateUserPreferences(session.getUser().getId());

            log.info("Session finalization completed: {}", session.getId());
        } catch (Exception e) {
            log.error("Error finalizing session: {}", session.getId(), e);
        }
    }

    private String generateFinalSummary(ChatSession session) {
        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());

        String conversationHistory = messages.stream()
            .map(msg -> String.format("%s: %s", msg.getRole(), msg.getContent()))
            .collect(Collectors.joining("\n"));

        String prompt = promptTemplateLibrary.getFinalSummaryPrompt(
            conversationHistory,
            session.getRollingSummary()
        );

        return geminiService.generateText(prompt);
    }
}
