package com.axiom.RagChat.message.controller;

import com.axiom.RagChat.message.entity.ChatMessage;
import com.axiom.RagChat.message.service.MessagePaginationService;
import com.axiom.RagChat.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Chat message operations")
public class MessageController {

    private final MessageService messageService;
    private final MessagePaginationService paginationService;
    // VectorStoreService removed — controllers talk to services, not infrastructure

    @PostMapping
    @Operation(summary = "Send message", description = "Send a message in a chat session")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long sessionId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        ChatMessage message = messageService.saveMessage(sessionId, request.getRole(), request.getContent());
        return ResponseEntity.ok(mapToResponse(message));
    }

    @GetMapping
    @Operation(summary = "Get messages", description = "Get paginated messages for a session")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long sessionId,
            Pageable pageable
    ) {
        Page<ChatMessage> messages = paginationService.getMessagesPaginated(sessionId, pageable);
        return ResponseEntity.ok(messages.map(this::mapToResponse));
    }

    @GetMapping("/search")
    @Operation(summary = "Semantic search", description = "Search messages by semantic similarity")
    public ResponseEntity<List<MessageResponse>> searchMessages(
            @PathVariable Long sessionId,
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<ChatMessage> results = messageService.searchSimilarMessages(sessionId, query, limit);
        return ResponseEntity.ok(results.stream().map(this::mapToResponse).toList());
    }

    private MessageResponse mapToResponse(ChatMessage message) {
        return MessageResponse.builder()
            .id(message.getId())
            .role(message.getRole())
            .content(message.getContent())
            .tokenCount(message.getTokenCount())
            .createdAt(message.getCreatedAt())
            .build();
    }
}

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class SendMessageRequest {
    @jakarta.validation.constraints.NotBlank
    private String role;

    @jakarta.validation.constraints.NotBlank
    private String content;
}

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class MessageResponse {
    private Long id;
    private String role;
    private String content;
    private Integer tokenCount;
    private java.time.LocalDateTime createdAt;
}