package com.amee.messaging.config;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;

import java.io.Serializable;

public class PublishConfig implements Serializable {

    private boolean mandatory = false;
    private boolean immediate = false;
    private AMQP.BasicProperties properties = MessageProperties.PERSISTENT_TEXT_PLAIN;

    public PublishConfig() {
        super();
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    public AMQP.BasicProperties getProperties() {
        return properties;
    }

    public void setProperties(AMQP.BasicProperties properties) {
        this.properties = properties;
    }
}