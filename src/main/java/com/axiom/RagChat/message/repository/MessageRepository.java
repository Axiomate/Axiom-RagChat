package com.axiom.RagChat.message.repository;

import com.axiom.RagChat.message.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId, Pageable pageable);
    
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    
    List<ChatMessage> findTop20BySessionIdOrderByCreatedAtDesc(Long sessionId);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :sessionId AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<ChatMessage> findMessagesSince(
        @Param("sessionId") Long sessionId,
        @Param("since") LocalDateTime since
    );
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.session.id = :sessionId")
    long countBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT SUM(m.tokenCount) FROM ChatMessage m WHERE m.session.id = :sessionId")
    Long sumTokensBySessionId(@Param("sessionId") Long sessionId);
}
