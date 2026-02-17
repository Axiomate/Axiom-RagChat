package com.axiom.RagChat.user.controller;

import com.axiom.RagChat.user.entity.User;
import com.axiom.RagChat.user.service.UserService;
import com.axiom.RagChat.user.service.UserPreferenceSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final UserPreferenceSummaryService preferenceSummaryService;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get authenticated user profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(mapToResponse(user));
    }

    @GetMapping("/me/preferences")
    @Operation(summary = "Get user preferences", description = "Get aggregated user preferences across all sessions")
    public ResponseEntity<UserPreferenceResponse> getUserPreferences() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(UserPreferenceResponse.builder()
            .preferenceSummary(user.getPreferenceSummary())
            .totalSessions(user.getTotalSessions())
            .totalMessages(user.getTotalMessages())
            .build());
    }

    @PostMapping("/me/preferences/refresh")
    @Operation(summary = "Refresh preferences", description = "Trigger preference aggregation from all sessions")
    public ResponseEntity<Void> refreshPreferences() {
        User user = userService.getCurrentUser();
        preferenceSummaryService.aggregateUserPreferences(user.getId());
        return ResponseEntity.accepted().build();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .totalSessions(user.getTotalSessions())
            .totalMessages(user.getTotalMessages())
            .createdAt(user.getCreatedAt())
            .build();
    }
}

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class UserResponse {
    private Long id;
    private String email;
    private String name;
    private Integer totalSessions;
    private Long totalMessages;
    private java.time.LocalDateTime createdAt;
}

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class UserPreferenceResponse {
    private String preferenceSummary;
    private Integer totalSessions;
    private Long totalMessages;
}
