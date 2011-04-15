package com.amee.messaging.config;

import com.rabbitmq.client.Address;

import java.io.Serializable;

/**
 * A bean for configuring a RabbitMQ connection.
 * <p/>
 * JavaDoc for the properties below is lifted directly from relevant RabbitMQ methods.
 * <p/>
 *
 * @see /conf/applicationContext-messaging.xml
 * @see com.amee.messaging.MessageService
 * @see com.rabbitmq.client.ConnectionFactory
 */
public class ConnectionConfig implements Serializable {

    /**
     * An array of known broker addresses (hostname/port pairs) to try in order.
     */
    private Address[] addresses;

    public ConnectionConfig() {
        super();
    }

    public Address[] getAddresses() {
        return addresses;
    }

    public void setAddresses(Address[] addresses) {
        this.addresses = addresses;
    }
}
