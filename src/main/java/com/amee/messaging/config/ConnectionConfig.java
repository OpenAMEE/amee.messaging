package com.amee.messaging.config;

import com.rabbitmq.client.Address;

import java.io.Serializable;

public class ConnectionConfig implements Serializable {

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
