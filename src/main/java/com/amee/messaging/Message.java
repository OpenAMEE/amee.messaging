package com.amee.messaging;

import org.springframework.context.ApplicationEvent;

public abstract class Message extends ApplicationEvent {

    private boolean local = false;

    public Message(Object source) {
        super(source);
        setLocal(true);
    }

    public Message(Object source, String message) {
        super(source);
        setMessage(message);
    }

    public abstract String getMessage();

    public abstract void setMessage(String message);

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }
}
