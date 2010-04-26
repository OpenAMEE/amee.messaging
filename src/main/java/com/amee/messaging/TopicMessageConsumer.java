package com.amee.messaging;

import com.amee.messaging.config.ConsumeConfig;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public abstract class TopicMessageConsumer extends MessageConsumer {

    private final Log log = LogFactory.getLog(getClass());

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

    public abstract ConsumeConfig getConsumeConfig();

    protected abstract void handle(QueueingConsumer.Delivery delivery);
}