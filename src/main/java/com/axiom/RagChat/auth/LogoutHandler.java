package com.axiom.RagChat.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutHandler implements org.springframework.security.web.authentication.logout.LogoutHandler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        
        final String jwt = authHeader.substring(7);
        
        try {
            // Blacklist token in Redis
            String key = "blacklist:token:" + jwt;
            long expirationMs = jwtService.getExpirationTime();
            redisTemplate.opsForValue().set(key, "true", expirationMs, TimeUnit.MILLISECONDS);
            
            log.info("Token blacklisted successfully");
        } catch (Exception e) {
            log.error("Error blacklisting token", e);
        }
    }
}
