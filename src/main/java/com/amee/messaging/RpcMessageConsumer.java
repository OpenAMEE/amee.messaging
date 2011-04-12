package com.amee.messaging;

import com.rabbitmq.client.RpcServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * An abstract base class {@link MessageConsumer} to implement an {@link RpcServer} consumer. Sub-classes must implement the
 * getRpcServer method along with consume, getExchangeConfig, getQueueConfig and getBindingKey
 * from {@link MessageConsumer}.
 *
 * @see RpcServer
 */
public abstract class RpcMessageConsumer extends MessageConsumer {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * Consume messages from the queue with the {@link RpcServer}.
     *
     * @throws IOException          thrown by RabbitMQ
     * @throws InterruptedException thrown by RabbitMQ
     */
    protected void consume() throws IOException, InterruptedException {
        log.debug("consume()");
        if (channel != null) {
            throw getRpcServer().mainloop();
        }
    }

    /**
     * Return a new {@link RpcServer} for this consumer.
     *
     * @return a new {@link RpcServer} for this consumer
     * @throws IOException thrown by RabbitMQ
     */
    protected abstract RpcServer getRpcServer() throws IOException;
}
