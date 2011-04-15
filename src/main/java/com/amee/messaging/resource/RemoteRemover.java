package com.amee.messaging.resource;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceRemover;
import com.amee.messaging.MessageService;
import com.amee.messaging.TimeoutRpcClient;
import com.amee.messaging.config.ExchangeConfig;
import com.rabbitmq.client.RpcClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * An implementation of {@link ResourceRemover} which will send the request to a remote ResourceRemover via
 * RabbitMQ RPC.
 *
 * @see ResourceRemover
 */
@Service
@Scope("prototype")
public class RemoteRemover implements ResourceRemover {

    @Autowired
    private MessageService messageService;

    @Autowired
    @Qualifier("requestWrapperExchange")
    private ExchangeConfig exchangeConfig;

    /**
     * The target bean in the remote instance which will handle the {@link RequestWrapper}.
     */
    private String target;

    /**
     * Handle a request, embodied in a {@link RequestWrapper}, and return a representation object. This will
     * send the RequestWrapper to a remote instance of ResourceRemover via RabbitMQ RPC.
     *
     * @param requestWrapper RequestWrapper for this request
     * @return the output representation object
     */
    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            requestWrapper.setTarget(getTarget());
            RpcClient rpcClient = new TimeoutRpcClient(
                    messageService.getChannel(exchangeConfig),
                    exchangeConfig.getName(),
                    exchangeConfig.getName());
            return new JSONObject(rpcClient.stringCall(requestWrapper.toJSONObject().toString()));
        } catch (IOException e) {
            throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}