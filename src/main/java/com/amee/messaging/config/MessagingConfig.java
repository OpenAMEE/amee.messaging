package com.amee.messaging.config;

import java.io.Serializable;

public class MessagingConfig implements Serializable {

    private int runSleep = 5000;
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
