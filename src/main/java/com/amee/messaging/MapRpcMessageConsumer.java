package com.amee.messaging;

import com.rabbitmq.client.MapRpcServer;
import com.rabbitmq.client.RpcServer;

import java.io.IOException;
import java.util.Map;

/**
 * An abstract class which sub-classes {@link RpcMessageConsumer} to handle Map RPC calls from
 * a {@link MapRpcServer}.
 */
public abstract class MapRpcMessageConsumer extends RpcMessageConsumer {

    /**
     * Return a new {@link MapRpcServer} for this consumer.
     *
     * @return a new {@link MapRpcServer} for this consumer
     * @throws IOException thrown by RabbitMQ
     */
    protected RpcServer getRpcServer() throws IOException {
        return new MapRpcServer(channel, getQueueConfig().getName()) {
            public Map<String, Object> handleMapCall(Map<String, Object> request) {
                return handle(request);
            }
        };
    }

    /**
     * Handle the Map form of the incoming RPC message.
     *
     * @param request in Map form
     * @return message response in Map form
     */
    protected abstract Map<String, Object> handle(Map<String, Object> request);
}