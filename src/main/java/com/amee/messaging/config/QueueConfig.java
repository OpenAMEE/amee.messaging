package com.amee.messaging.config;

import java.io.Serializable;
import java.util.Map;

/**
 * A bean for configuring a RabbitMQ queue.
 * <p/>
 * JavaDoc for the properties below is lifted directly from relevant RabbitMQ methods.
 * <p/>
 *
 * @see /conf/applicationContext-messaging.xml
 * @see com.amee.messaging.MessageService
 * @see com.amee.messaging.MessageConsumer
 * @see com.amee.messaging.TopicMessageConsumer
 * @see com.rabbitmq.client.Channel
 * @see com.rabbitmq.client.MapRpcServer
 */
public class QueueConfig implements Serializable {

    /**
     * The name of the queue.
     */
    private String name = "platform";

    /**
     * True if we are passively declaring a queue (asserting the queue already exists).
     */
    private boolean passive = false;

    /**
     * True if we are declaring a durable queue (the queue will survive a server restart).
     */
    private boolean durable = false;

    /**
     * True if we are declaring an exclusive queue.
     */
    private boolean exclusive = false;

    /**
     * True if we are declaring an autodelete queue (server will delete it when no longer in use).
     */
    private boolean autoDelete = false;

    /**
     * Other properties (construction arguments) for the queue.
     */
    private Map<String, Object> arguments = null;

    public QueueConfig() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(boolean passive) {
        this.passive = passive;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}