package com.amee.messaging.config;

import java.io.Serializable;

public class ConsumeConfig implements Serializable {

    private boolean noAck = false;
    private String consumerTag = "";
    private boolean noLocal = false;
    private boolean exclusive = false;

    public ConsumeConfig() {
        super();
    }

    public boolean isNoAck() {
        return noAck;
    }

    public void setNoAck(boolean noAck) {
        this.noAck = noAck;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    public boolean isNoLocal() {
        return noLocal;
    }

    public void setNoLocal(boolean noLocal) {
        this.noLocal = noLocal;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }
}