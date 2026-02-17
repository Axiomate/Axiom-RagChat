package com.axiom.RagChat.cache;

import com.axiom.RagChat.session.entity.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SESSION_CACHE_PREFIX = "session:";
    private static final long CACHE_TTL_HOURS = 24;

    public void cacheSession(ChatSession session) {
        try {
            String key = SESSION_CACHE_PREFIX + session.getId();
            SessionCacheDto dto = SessionCacheDto.from(session);
            redisTemplate.opsForValue().set(key, dto, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cached session {}", session.getId());
        } catch (Exception e) {
            log.error("Error caching session {}", session.getId(), e);
        }
    }

    public SessionCacheDto getCachedSession(Long sessionId) {
        try {
            String key = SESSION_CACHE_PREFIX + sessionId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof SessionCacheDto dto) {
                log.debug("Cache hit for session {}", sessionId);
                return dto;
            }
        } catch (Exception e) {
            log.error("Error retrieving session {} from cache", sessionId, e);
        }
        return null;
    }

    public void evictSession(Long sessionId) {
        try {
            String key = SESSION_CACHE_PREFIX + sessionId;
            redisTemplate.delete(key);
            log.debug("Evicted session {} from cache", sessionId);
        } catch (Exception e) {
            log.error("Error evicting session {} from cache", sessionId, e);
        }
    }

    // ── DTO ──────────────────────────────────────────────────────────────────
    // Plain serializable object — no JPA proxies, no Hibernate types

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionCacheDto implements Serializable {

        private Long id;
        private Long userId;
        private String title;
        private String rollingSummary;
        private String finalSummary;
        private Integer messageCount;
        private Long totalTokens;
        private String status;
        private LocalDateTime lastActivityAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime endedAt;

        public static SessionCacheDto from(ChatSession session) {
            return SessionCacheDto.builder()
                .id(session.getId())
                .userId(session.getUser() != null ? session.getUser().getId() : null)
                .title(session.getTitle())
                .rollingSummary(session.getRollingSummary())
                .finalSummary(session.getFinalSummary())
                .messageCount(session.getMessageCount())
                .totalTokens(session.getTotalTokens())
                .status(session.getStatus() != null ? session.getStatus().name() : null)
                .lastActivityAt(session.getLastActivityAt())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .endedAt(session.getEndedAt())
                .build();
        }
    }
}