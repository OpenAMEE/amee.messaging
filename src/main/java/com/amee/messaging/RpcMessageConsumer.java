package com.amee.messaging;

import com.rabbitmq.client.StringRpcServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public abstract class RpcMessageConsumer extends MessageConsumer {

    private final Log log = LogFactory.getLog(getClass());

    protected void consume() throws IOException, InterruptedException {
        log.debug("consume()");
        // If we have a channel we're safe to configure the StringRpcServer.
        if (channel != null) {
            StringRpcServer server = new StringRpcServer(channel, getQueueConfig().getName()) {
                public String handleStringCall(String request) {
                    return handle(request);
                }
            };
            throw server.mainloop();
        }
    }

    protected abstract String handle(String request);
}
