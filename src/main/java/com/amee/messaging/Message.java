package com.amee.messaging;

import org.springframework.context.ApplicationEvent;

public abstract class Message extends ApplicationEvent {

    public Message(Object source) {
        super(source);
    }

    public Message(Object source, String message) {
        super(source);
        setMessage(message);
    }

    public abstract String getMessage();

    public abstract void setMessage(String message);
}
