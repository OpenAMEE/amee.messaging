package com.amee.messaging.config;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;

import java.io.Serializable;

/**
 * A bean for RabbitMQ publish configuration.
 * <p/>
 * JavaDoc for the properties below is lifted directly from relevant RabbitMQ methods.
 *
 * @see /conf/applicationContext-messaging.xml
 * @see com.amee.messaging.MessageService
 * @see com.rabbitmq.client.Channel
 */
public class PublishConfig implements Serializable {

    /**
     * True if we are requesting a mandatory publish.
     */
    private boolean mandatory = false;

    /**
     * Immediate true if we are requesting an immediate publish.
     */
    private boolean immediate = false;

    /**
     * Other properties for the message - routing headers etc.
     */
    private AMQP.BasicProperties properties = MessageProperties.PERSISTENT_TEXT_PLAIN;

    /**
     * The name of the AMEE environment or 'scope' for messaging. Defaults to 'live' but could also be 'stage',
     * 'qa', 'dev', 'science' or another AMEE environment.
     */
    private String scope = "live";

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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}