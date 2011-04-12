package com.amee.messaging.config;

import java.io.Serializable;

/**
 * A bean for configuring a message consumer.
 * <p/>
 * JavaDoc for the properties below is lifted directly from relevant RabbitMQ methods.
 * <p/>
 *
 * @see /conf/applicationContext-messaging.xml
 * @see com.amee.messaging.TopicMessageConsumer
 * @see com.rabbitmq.client.Channel
 */
public class ConsumeConfig implements Serializable {

    /**
     * True if no handshake is required.
     */
    private boolean noAck = false;

    /**
     * A client-generated consumer tag to establish context.
     */
    private String consumerTag = "";

    /**
     * Flag set to true unless server local buffering is required.
     */
    private boolean noLocal = false;

    /**
     * True if this is an exclusive consumer
     */
    private boolean exclusive = false;

    /**
     * The name of the AMEE environment or 'scope' for messaging. Defaults to 'live' but could also be 'stage',
     * 'qa', 'dev', 'science' or another AMEE environment.
     */
    private String scope = "live";

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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}