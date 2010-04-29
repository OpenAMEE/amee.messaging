package com.amee.messaging.resource;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.messaging.MessageService;
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
public class RemoteJSONBuilder implements ResourceBuilder {

    @Autowired
    private MessageService messageService;

    @Autowired
    @Qualifier("requestWrapperExchange")
    private ExchangeConfig exchangeConfig;

    private String target;

    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            requestWrapper.setTarget(getTarget());
            RpcClient rpcClient = new RpcClient(
                    messageService.getChannel(exchangeConfig),
                    exchangeConfig.getName(),
                    exchangeConfig.getName());
            return new JSONObject(rpcClient.stringCall(requestWrapper.toJSONObject().toString()));
        } catch (IOException e) {
            // TODO.
        } catch (JSONException e) {
            // TODO.
        }
        return null;
    }

    public String getMediaType() {
        return "application/json";
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
