package com.amee.messaging.config;

import java.io.Serializable;
import java.util.Map;

/**
 * A bean for configuring a RabbitMQ exchange.
 * <p/>
 * JavaDoc for the properties below is lifted directly from relevant RabbitMQ methods.
 *
 * @see /conf/applicationContext-messaging.xml
 * @see com.amee.messaging.MessageService
 * @see com.rabbitmq.client.Channel
 */
public class ExchangeConfig implements Serializable {

    /**
     * The name of the exchange.
     */
    private String name = "platform";

    /**
     * The exchange type.
     */
    private String type = "direct";

    /**
     * True if we are passively declaring a exchange (asserting the exchange already exists).
     */
    private boolean passive = false;

    /**
     * True if we are declaring a durable exchange (the exchange will survive a server restart).
     */
    private boolean durable = false;

    /**
     * True if the server should delete the exchange when it is no longer in use.
     */
    private boolean autoDelete = false;

    /**
     * Other properties (construction arguments) for the exchange.
     */
    private Map<String, Object> arguments = null;

    public ExchangeConfig() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

