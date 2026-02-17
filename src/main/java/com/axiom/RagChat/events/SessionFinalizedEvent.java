package com.axiom.RagChat.events;

import com.axiom.RagChat.session.entity.ChatSession;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SessionFinalizedEvent extends ApplicationEvent {
    
    private final ChatSession session;

    public SessionFinalizedEvent(Object source, ChatSession session) {
        super(source);
        this.session = session;
    }
}