package com.axiom.RagChat.summary.service;

import com.axiom.RagChat.ai.GeminiService;
import com.axiom.RagChat.ai.PromptTemplateLibrary;
import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.repository.MessageRepository;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private final MessageRepository messageRepository;
    private final SessionService sessionService;
    private final GeminiService geminiService;
    private final PromptTemplateLibrary promptTemplateLibrary;

    public String generateRollingSummary(Long sessionId) {
        ChatSession session = sessionService.getSession(sessionId);
        
        // Get messages since last summary
        LocalDateTime since = session.getLastSummaryAt() != null 
            ? session.getLastSummaryAt() 
            : session.getCreatedAt();
        
        List<ChatMessage> newMessages = messageRepository.findMessagesSince(sessionId, since);

        if (newMessages.isEmpty()) {
            log.info("No new messages to summarize for session {}", sessionId);
            return session.getRollingSummary();
        }

        String newContent = newMessages.stream()
            .map(msg -> String.format("%s: %s", msg.getRole(), msg.getContent()))
            .collect(Collectors.joining("\n"));

        String prompt = promptTemplateLibrary.getRollingSummaryPrompt(
            session.getRollingSummary(),
            newContent
        );

        String updatedSummary = geminiService.generateText(prompt);
        
        sessionService.updateRollingSummary(sessionId, updatedSummary);
        
        log.info("Updated rolling summary for session {}", sessionId);
        return updatedSummary;
    }
}
