package com.axiom.RagChat.session.entity;

import com.axiom.RagChat.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions", indexes = {
    @Index(name = "idx_session_user",          columnList = "user_id"),
    @Index(name = "idx_session_status",        columnList = "status"),
    @Index(name = "idx_session_updated",       columnList = "updated_at"),
    @Index(name = "idx_session_last_activity", columnList = "last_activity_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ← Expose userId directly so derived queries like findByUserIdOrderByUpdatedAtDesc work
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "rolling_summary", columnDefinition = "TEXT")
    private String rollingSummary;

    @Column(name = "final_summary", columnDefinition = "TEXT")
    private String finalSummary;

    @Column(name = "session_snapshot", columnDefinition = "TEXT")
    private String sessionSnapshot;

    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    @Column(name = "last_summary_at")
    private LocalDateTime lastSummaryAt;

    @Column(name = "total_tokens")
    @Builder.Default
    private Long totalTokens = 0L;

    @Column(name = "prompt_tokens")
    @Builder.Default
    private Long promptTokens = 0L;

    @Column(name = "completion_tokens")
    @Builder.Default
    private Long completionTokens = 0L;

    @Column(name = "embedding_tokens")
    @Builder.Default
    private Long embeddingTokens = 0L;

    @Column(name = "snapshot_created_at")
    private LocalDateTime snapshotCreatedAt;

    @Column(name = "snapshot_version")
    @Builder.Default
    private Integer snapshotVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public enum SessionStatus {
        ACTIVE,
        INACTIVE,
        FINALIZED,
        ARCHIVED
    }

    @PrePersist
    public void prePersist() {
        if (lastActivityAt == null) {
            lastActivityAt = LocalDateTime.now();
        }
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void incrementMessageCount() {
        this.messageCount++;
    }

    public void addTokens(long tokens) {
        this.totalTokens += tokens;
    }
}