package com.amee.messaging;

import com.rabbitmq.client.RpcServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public abstract class RpcMessageConsumer extends MessageConsumer {

    private final Log log = LogFactory.getLog(getClass());

    protected void consume() throws IOException, InterruptedException {
        log.debug("consume()");
        if (channel != null) {
            throw getRpcServer().mainloop();
        }
    }

    protected abstract RpcServer getRpcServer() throws IOException;
}
