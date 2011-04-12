package com.amee.messaging;

import com.amee.messaging.config.ExchangeConfig;
import com.amee.messaging.config.MessagingConfig;
import com.amee.messaging.config.QueueConfig;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;

/**
 * An abstract base class for Spring bean responsible for managing the consumption of RabbitMQ messages. Most of
 * the logic here is to do with exception handling, thread management and orchestration of RabbitMQ {@link Channel}s
 * and {@link com.rabbitmq.client.AMQP.Queue}s.
 * <p/>
 * Sub-classes must implement the abstract methods (consume, getExchangeConfig, getQueueConfig and
 * getBindingKey) to handle the messages.
 */
public abstract class MessageConsumer implements Runnable, SmartLifecycle, ApplicationContextAware {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    protected MessageService messageService;

    @Autowired
    protected MessagingConfig messagingConfig;

    protected Channel channel;
    protected Thread thread;
    protected boolean stopping = false;
    protected ApplicationContext applicationContext;

    /**
     * Spring Lifecycle callback to start this bean on application startup. Will create a new {@link Thread} for
     * message consumption and start it.
     */
    @Override
    public synchronized void start() {
        log.info("start()");
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Spring Lifecycle callback to stop this bean on application shutdown. Will shutdown (interrupt)
     * the {@link Thread}.
     */
    @Override
    public synchronized void stop() {
        log.info("stop()");
        stopping = true;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * Returns true if there is a {@link Thread} present and it is alive.
     *
     * @return true if the thread is present and alive
     */
    @Override
    public boolean isRunning() {
        return (thread != null) && (thread.isAlive());
    }

    /**
     * Return whether this Lifecycle component should be started automatically
     * by the container when the ApplicationContext is refreshed. A value of
     * "false" indicates that the component is intended to be started manually.
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * Spring Lifecycle callback to stop this bean on application shutdown.
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    /**
     * Return the Spring lifecycle phase value of this object. We use {@link Integer.MAX_VALUE} to indicate that
     * this bean should start as late as possible.
     */
    @Override
    public int getPhase() {
        // Start as late as possible.
        return Integer.MAX_VALUE;
    }

    /**
     * Implementation of run from {@link Runnable}. This attempts to reliably consume messages from queues
     * until the application stops. Will catch {@link IOException}, {@link ShutdownSignalException},
     * {@link InterruptedException}, {@link Exception} and {@link Throwable}. The thread will only
     * stop handling a queue if the current thread has not been interrupted or a {@link InterruptedException} has
     * not been thrown.
     */
    @Override
    public void run() {
        log.info("run()");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                log.debug("run() Waiting.");
                // Wait before first-run and subsequent retries.
                Thread.sleep(messagingConfig.getRunSleep());
                log.debug("run() Starting.");
                // Start the Consumer and handle the deliveries.
                configureChannel();
                consume();
                // We got here if there is no channel or this was stopped.
                log.debug("run() No channel or stopped.");
            } catch (IOException e) {
                log.warn("run() Caught IOException. We'll try restarting the consumer. Message was: " +
                        ((e.getCause() != null) ? e.getCause().getMessage() : e.getMessage()));
            } catch (ShutdownSignalException e) {
                log.warn("run() Caught ShutdownSignalException. We'll try restarting the consumer. Message was: " +
                        e.getMessage());
            } catch (InterruptedException e) {
                log.info("run() Interrupted.");
                closeAndClear();
                return;
            } catch (Exception e) {
                log.error("run() Caught Exception: " + e.getMessage(), e);
            } catch (Throwable t) {
                log.error("run() Caught Throwable: " + t.getMessage(), t);
            }
        }
    }

    /**
     * Configures a RabbitMQ {@link Channel} for the current {@link ExchangeConfig}, {@link QueueConfig} and
     * binding key.
     *
     * @throws IOException thrown by RabbitMQ
     */
    protected void configureChannel() throws IOException {
        // Ensure the channel is closed & consumer is cleared before starting.
        closeAndClear();
        // Try to get a channel for our configuration.
        channel = messageService.getChannelAndBind(
                getExchangeConfig(),
                getQueueConfig(),
                getBindingKey());
    }

    /**
     * An abstract method to be implemented by sub-classes to consume messages from a queue.
     *
     * @throws IOException          thrown by RabbitMQ
     * @throws InterruptedException thrown by RabbitMQ
     */
    protected abstract void consume() throws IOException, InterruptedException;

    /**
     * Will close and null the current RabbitMQ {@link Channel}.
     */
    protected synchronized void closeAndClear() {
        log.debug("closeAndClear()");
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                // Swallow.
            } catch (ShutdownSignalException e) {
                // Swallow.
            }
            channel = null;
        }
    }

    /**
     * Get the {@link ExchangeConfig} applicable for this {@link MessageConsumer} instance.
     *
     * @return the {@link ExchangeConfig} applicable for this {@link MessageConsumer} instance
     */
    public abstract ExchangeConfig getExchangeConfig();

    /**
     * Get the {@link QueueConfig} applicable for this {@link MessageConsumer} instance.
     *
     * @return the {@link QueueConfig} applicable for this {@link MessageConsumer} instance
     */
    public abstract QueueConfig getQueueConfig();

    /**
     * Get the routing / binding key to use for RabbitMQ queue binding.
     *
     * @return the routing / binding key
     */
    public abstract String getBindingKey();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
