package com.axiom.RagChat.ai;

import org.springframework.stereotype.Component;

@Component
public class PromptTemplateLibrary {

    public String getRollingSummaryPrompt(String existingSummary, String newContent) {
        return String.format("""
            You are a conversation summarizer. Update the existing summary with new information.
            
            Existing Summary:
            %s
            
            New Messages:
            %s
            
            Instructions:
            1. Integrate new information into the existing summary
            2. Maintain chronological flow
            3. Keep the summary concise (max 500 words)
            4. Focus on key topics, decisions, and action items
            5. Preserve important context
            
            Provide the updated summary:
            """, 
            existingSummary != null ? existingSummary : "No previous summary.",
            newContent
        );
    }

    public String getFinalSummaryPrompt(String conversationHistory, String rollingSummary) {
        return String.format("""
            You are creating a comprehensive final summary of a chat session.
            
            Rolling Summary:
            %s
            
            Full Conversation:
            %s
            
            Instructions:
            1. Create a detailed summary of the entire conversation
            2. Include:
               - Main topics discussed
               - Key decisions made
               - Important insights
               - Action items identified
               - User preferences revealed
            3. Structure the summary with clear sections
            4. Maximum 1000 words
            
            Provide the final summary:
            """,
            rollingSummary != null ? rollingSummary : "No rolling summary available.",
            conversationHistory
        );
    }

    public String getUserPreferenceAggregationPrompt(String userName, String sessionSummaries) {
        return String.format("""
            You are analyzing multiple chat sessions to identify user preferences and patterns.
            
            User: %s
            
            Session Summaries:
            %s
            
            Instructions:
            1. Identify consistent preferences across sessions
            2. Note communication style preferences
            3. Detect topic interests
            4. Highlight recurring goals or needs
            5. Summarize in 300 words or less
            
            Provide aggregated user preferences:
            """,
            userName,
            sessionSummaries
        );
    }

    public String getContextualResponsePrompt(String userMessage, String sessionContext, String userPreferences) {
        return String.format("""
            You are an AI assistant continuing a conversation with context awareness.
            
            Session Context:
            %s
            
            User Preferences:
            %s
            
            User Message:
            %s
            
            Instructions:
            1. Respond naturally to the user's message
            2. Use session context to maintain conversation flow
            3. Respect user preferences in your tone and style
            4. Be helpful, accurate, and concise
            
            Your response:
            """,
            sessionContext != null ? sessionContext : "New conversation.",
            userPreferences != null ? userPreferences : "No known preferences.",
            userMessage
        );
    }
}