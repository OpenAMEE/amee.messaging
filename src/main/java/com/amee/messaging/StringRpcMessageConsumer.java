package com.amee.messaging;

import com.rabbitmq.client.RpcServer;
import com.rabbitmq.client.StringRpcServer;

import java.io.IOException;

public abstract class StringRpcMessageConsumer extends RpcMessageConsumer {

    protected RpcServer getRpcServer() throws IOException {
        return new StringRpcServer(channel, getQueueConfig().getName()) {
            public String handleStringCall(String request) {
                return handle(request);
            }
        };
    }

    protected abstract String handle(String request);
}