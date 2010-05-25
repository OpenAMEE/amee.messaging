package com.amee.messaging.resource;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceHandler;
import com.amee.messaging.MessageService;
import com.amee.messaging.TimeoutRpcClient;
import com.amee.messaging.config.ExchangeConfig;
import com.rabbitmq.client.RpcClient;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class RemoteResource implements ResourceHandler {

    @Autowired
    private MessageService messageService;

    @Autowired
    @Qualifier("requestWrapperExchange")
    private ExchangeConfig exchangeConfig;

    private String target;

    public Object handle(RequestWrapper requestWrapper) {
        try {
            // Remote end requires a named target.
            requestWrapper.setTarget(getTarget());
            // Prepare the message.
            Map<String, Object> message = new HashMap<String, Object>();
            message.put("requestWrapper", requestWrapper.toJSONObject().toString());
            // Prepare the RpcClient.
            RpcClient rpcClient = new TimeoutRpcClient(
                    messageService.getChannel(exchangeConfig),
                    exchangeConfig.getName(),
                    exchangeConfig.getName());
            // Send the message and get the reply.
            Map<String, Object> reply = rpcClient.mapCall(message);
            // Handle the reply.
            String mediaType = reply.containsKey("mediaType") ? reply.get("mediaType").toString() : null;
            String response = reply.containsKey("response") ? reply.get("response").toString() : null;
            if ((mediaType != null) && (response != null)) {
                if (mediaType.endsWith("application/json")) {
                    try {
                        return new JSONObject(response);
                    } catch (JSONException e) {
                        // TODO.
                    }
                } else if (mediaType.endsWith("application/xml")) {
                    try {
                        StringReader reader = new StringReader(response);
                        SAXBuilder saxBuilder = new SAXBuilder();
                        return saxBuilder.build(reader);
                    } catch (JDOMException e) {
                        // TODO.
                    }
                } else {
                    // TODO.
                }
            } else {
                // TODO.
            }
        } catch (IOException e) {
            // TODO.
        }
        return null;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}