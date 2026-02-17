package com.axiom.RagChat.user.service;

import com.axiom.RagChat.ai.GeminiService;
import com.axiom.RagChat.ai.PromptTemplateLibrary;
import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.repository.SessionRepository;
import com.axiom.RagChat.user.entity.User;
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
public class UserPreferenceSummaryService {

    private final UserService userService;
    private final SessionRepository sessionRepository;
    private final GeminiService geminiService;
    private final PromptTemplateLibrary promptTemplateLibrary;

    @Async
    @Transactional
    public void aggregateUserPreferences(Long userId) {
        try {
            User user = userService.getUserById(userId);
            List<ChatSession> sessions = sessionRepository.findByUserIdAndFinalSummaryIsNotNull(userId);

            if (sessions.isEmpty()) {
                log.info("No finalized sessions found for user: {}", userId);
                return;
            }

            String sessionSummaries = sessions.stream()
                .map(session -> String.format("Session %d: %s", session.getId(), session.getFinalSummary()))
                .collect(Collectors.joining("\n\n"));

            String prompt = promptTemplateLibrary.getUserPreferenceAggregationPrompt(
                user.getName(),
                sessionSummaries
            );

            String preferenceSummary = geminiService.generateText(prompt);
            userService.updatePreferenceSummary(userId, preferenceSummary);

            log.info("User preference summary updated for user: {}", userId);
        } catch (Exception e) {
            log.error("Error aggregating user preferences for user: {}", userId, e);
        }
    }
}
