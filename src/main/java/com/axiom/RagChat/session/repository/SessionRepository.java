package com.axiom.RagChat.session.repository;

import com.axiom.RagChat.session.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<ChatSession, Long> {

    // Uses direct userId field on entity — works with derived query
    Page<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    List<ChatSession> findByUserIdAndStatus(Long userId, ChatSession.SessionStatus status);

    List<ChatSession> findByUserIdAndFinalSummaryIsNotNull(Long userId);

    @Query("""
        SELECT s FROM ChatSession s
        WHERE s.status = :status
        AND s.lastActivityAt < :cutoffTime
        """)
    List<ChatSession> findInactiveSessions(
        @Param("status") ChatSession.SessionStatus status,
        @Param("cutoffTime") LocalDateTime cutoffTime
    );

    // Uses s.user.id — correct JPQL path through the @ManyToOne relationship
    @Query("""
        SELECT COUNT(s) FROM ChatSession s
        WHERE s.user.id = :userId
        AND s.status = 'ACTIVE'
        """)
    long countActiveSessionsByUserId(@Param("userId") Long userId);
}