package com.amee.messaging;

/**
 * A custom {@link RuntimeException} implementation for this messaging library.
 */
public class MessagingException extends RuntimeException {

    public MessagingException() {
        super();
    }

    public MessagingException(String message) {
        super(message);
    }

    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagingException(Throwable cause) {
        super(cause);
    }
}
