package com.amee.messaging;

import org.springframework.context.ApplicationEvent;

/**
 * An abstract base class encapsulating a message to be sent via RabbitMQ. This extends {@link ApplicationEvent}
 * so instances can also be published on the Spring event bus. Concrete sub-classes must implement getMessage
 * and setMessage to in order to define the message body to be sent.
 */
public abstract class Message extends ApplicationEvent {

    /**
     * Indicates if this message was created locally to the current JVM.
     */
    private boolean local;

    /**
     * A constructor for locally sourced messages. Will set the local property to true.
     *
     * @param source the component that published the event
     */
    public Message(Object source) {
        super(source);
        setLocal(true);
    }

    /**
     * A constructor for remotely sourced messages. Will set the local property to false.
     *
     * @param source  the component that published the event
     * @param message the remote message to be parsed
     */
    public Message(Object source, String message) {
        super(source);
        setLocal(false);
        setMessage(message);
    }

    /**
     * Get the message as a String that is ready to be published. The message String is typicall
     * a deserialized object produced by the implementing sub-class.
     *
     * @return the message
     */
    public abstract String getMessage();

    /**
     * Set the message as a String. The message String is typically a serialized object that will
     * be parsed by the implementing sub-class.
     *
     * @param message the message to be parsed
     */
    public abstract void setMessage(String message);

    /**
     * Return true if this message was created locally to the current JVM.
     *
     * @return true if this message was created locally to the current JVM
     */
    public boolean isLocal() {
        return local;
    }

    /**
     * Set the local property, indicating if this message was created locally to the current JVM.
     *
     * @param local true if this message was created locally to the current JVM
     */
    public void setLocal(boolean local) {
        this.local = local;
    }
}
