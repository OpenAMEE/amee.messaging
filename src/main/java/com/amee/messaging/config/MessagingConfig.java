package com.amee.messaging.config;

import java.io.Serializable;

/**
 * A bean for configuring a {@link com.amee.messaging.MessageConsumer}.
 *
 * @see /conf/applicationContext-messaging.xml
 * @see com.amee.messaging.MessageConsumer
 * @see com.amee.messaging.TopicMessageConsumer
 * @see com.rabbitmq.client.QueueingConsumer
 */
public class MessagingConfig implements Serializable {

    /**
     * Delay in milliseconds before starting consumer threads at application start-up.
     */
    private int runSleep = 5000;

    /**
     * Timeout in milliseconds when waiting for the next message delivery.
     */
    private int deliveryTimeout = 1000;

    public MessagingConfig() {
        super();
    }

    public int getRunSleep() {
        return runSleep;
    }

    public void setRunSleep(int runSleep) {
        this.runSleep = runSleep;
    }

    public int getDeliveryTimeout() {
        return deliveryTimeout;
    }

    public void setDeliveryTimeout(int deliveryTimeout) {
        this.deliveryTimeout = deliveryTimeout;
    }
}
