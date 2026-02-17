package com.axiom.RagChat.events;

import com.axiom.RagChat.message.entity.ChatMessage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MessageSavedEvent extends ApplicationEvent {
    
    private final ChatMessage message;

    public MessageSavedEvent(Object source, ChatMessage message) {
        super(source);
        this.message = message;
    }
}
