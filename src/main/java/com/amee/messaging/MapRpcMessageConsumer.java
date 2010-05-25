package com.amee.messaging;

import com.rabbitmq.client.MapRpcServer;
import com.rabbitmq.client.RpcServer;

import java.io.IOException;
import java.util.Map;

public abstract class MapRpcMessageConsumer extends RpcMessageConsumer {

    protected RpcServer getRpcServer() throws IOException {
        return new MapRpcServer(channel, getQueueConfig().getName()) {
            public Map<String, Object> handleMapCall(Map<String, Object> request) {
                return handle(request);
            }
        };
    }

    protected abstract Map<String, Object> handle(Map<String, Object> request);
}