package com.amee.messaging;

import com.rabbitmq.client.RpcServer;
import com.rabbitmq.client.StringRpcServer;

import java.io.IOException;

/**
 * An abstract class which sub-classes {@link RpcMessageConsumer} to handle String RPC calls from
 * a {@link StringRpcServer}.
 */
public abstract class StringRpcMessageConsumer extends RpcMessageConsumer {

    /**
     * Return a new {@link StringRpcServer} for this consumer.
     *
     * @return a new {@link StringRpcServer} for this consumer
     * @throws IOException thrown by RabbitMQ
     */
    protected RpcServer getRpcServer() throws IOException {
        return new StringRpcServer(channel, getQueueConfig().getName()) {
            public String handleStringCall(String request) {
                return handle(request);
            }
        };
    }

    /**
     * Handle the String form of the incoming RPC message.
     *
     * @param request in String form
     * @return message response in String form
     */
    protected abstract String handle(String request);
}