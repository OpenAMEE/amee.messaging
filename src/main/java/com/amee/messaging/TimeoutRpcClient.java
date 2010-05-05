package com.amee.messaging;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClient;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A subclass of RpcClient that wraps primitiveCall such that a timeout is taken into the account.
 */
public class TimeoutRpcClient extends RpcClient {

    private final Log log = LogFactory.getLog(getClass());

    private int timeout = 30;

    public TimeoutRpcClient(Channel channel, String exchange, String routingKey) throws IOException {
        super(channel, exchange, routingKey);
    }

    public TimeoutRpcClient(Channel channel, String exchange, String routingKey, int timeout) throws IOException {
        super(channel, exchange, routingKey);
        setTimeout(timeout);
    }

    @Override
    public byte[] primitiveCall(final AMQP.BasicProperties props, final byte[] message)
            throws IOException, ShutdownSignalException {
        byte[] result = new byte[0];
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
            public Object call() throws IOException, ShutdownSignalException {
                return wrappedPrimitiveCall(props, message);
            }
        };
        log.debug("primitiveCall() Submitting the task.");
        Future<Object> future = executor.submit(task);
        try {
            result = (byte[]) future.get(getTimeout(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("primitiveCall() Caught TimeoutException (aborting).");
            getChannel().abort();
        } catch (InterruptedException e) {
            log.warn("primitiveCall() Caught InterruptedException (aborting): " + e.getMessage());
            getChannel().abort();
        } catch (ExecutionException e) {
            log.warn("primitiveCall() Caught ExecutionException (aborting): " + e.getMessage());
            getChannel().abort();
        } finally {
            log.debug("primitiveCall() Canceling the task via its Future.");
            future.cancel(true);
        }
        return result;
    }

    public byte[] wrappedPrimitiveCall(AMQP.BasicProperties props, byte[] message)
            throws IOException, ShutdownSignalException {
        return super.primitiveCall(props, message);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
