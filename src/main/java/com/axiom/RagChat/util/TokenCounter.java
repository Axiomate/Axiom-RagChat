package com.axiom.RagChat.util;

import org.springframework.stereotype.Component;

@Component
public class TokenCounter {

    /**
     * Estimates token count based on character count
     * Rough approximation: 1 token ≈ 4 characters
     */
    public int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // Simple approximation
        // In production, use actual tokenizer like tiktoken
        return (int) Math.ceil(text.length() / 4.0);
    }

    /**
     * Estimates token count for multiple texts
     */
    public int countTokens(String... texts) {
        int total = 0;
        for (String text : texts) {
            total += countTokens(text);
        }
        return total;
    }
}