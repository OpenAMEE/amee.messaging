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

@Service
@Scope("prototype")
public class RemoteRemover implements ResourceRemover {

    @Autowired
    private MessageService messageService;

    @Autowired
    @Qualifier("requestWrapperExchange")
    private ExchangeConfig exchangeConfig;

    private String target;

    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            requestWrapper.setTarget(getTarget());
            RpcClient rpcClient = new TimeoutRpcClient(
                    messageService.getChannel(exchangeConfig),
                    exchangeConfig.getName(),
                    exchangeConfig.getName());
            return new JSONObject(rpcClient.stringCall(requestWrapper.toJSONObject().toString()));
        } catch (IOException e) {
            throw new RuntimeException("Caught IOException: " + e.getMessage());
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage());
        }
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}