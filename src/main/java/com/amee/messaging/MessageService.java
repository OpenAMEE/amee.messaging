package com.amee.messaging;

import com.amee.messaging.config.ConnectionConfig;
import com.amee.messaging.config.ExchangeConfig;
import com.amee.messaging.config.QueueConfig;
import com.amee.messaging.config.PublishConfig;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConnectionParameters;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Service
public class MessageService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ConnectionConfig connectionConfig;

    @Autowired
    private ConnectionParameters connectionParameters;

    private Connection connection;

    @PreDestroy
    public synchronized void stop() {
        log.info("stop()");
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                // Swallow.
            } catch (ShutdownSignalException e) {
                // Swallow.
            }
            connection = null;
        }
    }

    public void publish(
            ExchangeConfig exchangeConfig,
            PublishConfig publishConfig,
            String routingKey,
            Message message) {
        log.debug("publish() " + routingKey);
        try {
            // Try to get a channel.
            Channel channel = getChannel();
            if (channel != null) {
                // Ensure exchange is declared.
                exchangeDeclare(channel, exchangeConfig);
                // Publish.
                channel.basicPublish(
                        exchangeConfig.getName(),
                        routingKey,
                        publishConfig.isMandatory(),
                        publishConfig.isImmediate(),
                        publishConfig.getProperties(),
                        message.getMessage().getBytes());
                // We're done with the channel.
                channel.close();
            } else {
                log.warn("publish() Unable to get a channel.");
            }
        } catch (IOException e) {
            log.warn("publish() Caught IOException: " + e.getMessage());
        } catch (ShutdownSignalException e) {
            log.warn("publish() Caught ShutdownSignalException: " + e.getMessage());
        } catch (MessagingException e) {
            log.warn("publish() Caught MessagingException: " + e.getMessage());
        }
    }

    public Channel getChannelAndBind(
            ExchangeConfig exchangeConfig,
            QueueConfig queueConfig,
            String bindingKey) throws IOException {
        log.debug("consume() " + bindingKey);
        // Try to get a channel.
        Channel channel = getChannel();
        if (channel != null) {
            // Ensure exchange & queue are declared and queue is bound.
            exchangeDeclare(channel, exchangeConfig);
            queueDeclare(channel, queueConfig);
            queueBind(channel, queueConfig, exchangeConfig, bindingKey);
        }
        return channel;
    }

    public void exchangeDeclare(Channel channel, ExchangeConfig exchangeConfig) throws IOException {
        channel.exchangeDeclare(
                exchangeConfig.getName(),
                exchangeConfig.getType(),
                exchangeConfig.isPassive(),
                exchangeConfig.isDurable(),
                exchangeConfig.isAutoDelete(),
                exchangeConfig.getArguments());
    }

    public void queueDeclare(Channel channel, QueueConfig queueConfig) throws IOException {
        channel.queueDeclare(
                queueConfig.getName(),
                queueConfig.isPassive(),
                queueConfig.isDurable(),
                queueConfig.isExclusive(),
                queueConfig.isAutoDelete(),
                queueConfig.getArguments());
    }

    public void queueBind(Channel channel, QueueConfig queueConfig, ExchangeConfig exchangeConfig, String bindingKey) throws IOException {
        channel.queueBind(
                queueConfig.getName(),
                exchangeConfig.getName(),
                bindingKey,
                null);
    }

    public Channel getChannel() throws IOException {
        try {
            return getConnection().createChannel();
        } catch (ShutdownSignalException e) {
            log.warn("getChannel() Caught ShutdownSignalException. We'll try to ignore once. Message was: " + e.getMessage());
            connection = null;
            return getConnection().createChannel();
        }
    }

    public Connection getConnection() throws IOException {
        if (connection == null) {
            synchronized (this) {
                if (connection == null) {
                    connection = getConnectionFactory().newConnection(connectionConfig.getAddresses());
                }
            }
        }
        return connection;
    }

    public ConnectionFactory getConnectionFactory() {
        return new ConnectionFactory(connectionParameters);
    }
}
