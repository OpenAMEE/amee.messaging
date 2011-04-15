package com.amee.messaging;

import com.amee.messaging.config.ConsumeConfig;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * An abstract implementation of {@link MessageConsumer} for RabbitMQ topics. Sub-classes are required to
 * implement the abstract getConsumeConfig and handle methods.
 */
public abstract class TopicMessageConsumer extends MessageConsumer {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * Starts a topic consumption loop which will finish when the application ends or an unhandled exception is
     * thrown.
     *
     * @throws IOException          thrown by RabbitMQ
     * @throws InterruptedException thrown by RabbitMQ
     */
    protected void consume() throws IOException, InterruptedException {
        log.debug("consume()");
        // If we have a channel we're safe to configure the QueueingConsumer.
        if (channel != null) {
            // Create QueueingConsumer and start.
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(
                    getQueueConfig().getName(),
                    getConsumeConfig().isNoAck(),
                    getConsumeConfig().getConsumerTag(),
                    getConsumeConfig().isNoLocal(),
                    getConsumeConfig().isExclusive(),
                    consumer);
            // Handle deliveries until stopped.
            while (!stopping) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(messagingConfig.getDeliveryTimeout());
                if (delivery != null) {
                    try {
                        handle(delivery);
                    } catch (MessagingException e) {
                        log.warn("consume() Caught MessagingException: " + e.getMessage());
                    }
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            }
            closeAndClear();
        }
    }

    /**
     * Get the {@link ConsumeConfig} for this consumer.
     *
     * @return the {@link ConsumeConfig}
     */
    public abstract ConsumeConfig getConsumeConfig();

    /**
     * Handle the incoming message represented by {@link QueueingConsumer.Delivery}.
     *
     * @param delivery a {@link QueueingConsumer.Delivery} representing the incoming message
     */
    protected abstract void handle(QueueingConsumer.Delivery delivery);
}