package com.axiom.RagChat.session.controller;

import com.axiom.RagChat.session.entity.ChatSession;
import com.axiom.RagChat.session.service.SessionRestartService;
import com.axiom.RagChat.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Chat session management")
public class SessionController {

    private final SessionService sessionService;
    private final SessionRestartService restartService;

    @PostMapping
    @Operation(summary = "Create session", description = "Create a new chat session")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        ChatSession session = sessionService.createSession(request.getTitle());
        return ResponseEntity.ok(mapToResponse(session));
    }

    @GetMapping
    @Operation(summary = "List sessions", description = "Get paginated list of user sessions")
    public ResponseEntity<Page<SessionResponse>> getSessions(Pageable pageable) {
        Page<ChatSession> sessions = sessionService.getUserSessions(pageable);
        return ResponseEntity.ok(sessions.map(this::mapToResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session", description = "Get session details with summary")
    public ResponseEntity<SessionResponse> getSession(@PathVariable Long id) {
        ChatSession session = sessionService.getSession(id);
        return ResponseEntity.ok(mapToResponse(session));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "End session", description = "End session and trigger finalization")
    public ResponseEntity<Void> endSession(@PathVariable Long id) {
        sessionService.endSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restart")
    @Operation(summary = "Restart session", description = "Create new session from snapshot")
    public ResponseEntity<SessionResponse> restartSession(@PathVariable Long id) {
        ChatSession newSession = restartService.restartFromSnapshot(id);
        return ResponseEntity.ok(mapToResponse(newSession));
    }

    private SessionResponse mapToResponse(ChatSession session) {
        return SessionResponse.builder()
            .id(session.getId())
            .title(session.getTitle())
            .status(session.getStatus().name())
            .messageCount(session.getMessageCount())
            .rollingSummary(session.getRollingSummary())
            .finalSummary(session.getFinalSummary())
            .totalTokens(session.getTotalTokens())
            .lastActivityAt(session.getLastActivityAt())
            .createdAt(session.getCreatedAt())
            .endedAt(session.getEndedAt())
            .build();
    }
}

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class CreateSessionRequest {
    private String title;
}

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class SessionResponse {
    private Long id;
    private String title;
    private String status;
    private Integer messageCount;
    private String rollingSummary;
    private String finalSummary;
    private Long totalTokens;
    private java.time.LocalDateTime lastActivityAt;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime endedAt;
}
