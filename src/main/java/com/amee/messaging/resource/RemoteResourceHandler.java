package com.amee.messaging.resource;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceHandler;
import com.amee.messaging.MessageService;
import com.amee.messaging.TimeoutRpcClient;
import com.amee.messaging.config.ExchangeConfig;
import com.rabbitmq.client.RpcClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 * An implementation of {@link ResourceHandler} which will send the request to a remote ResourceHandler via
 * RabbitMQ RPC.
 */
public class RemoteResourceHandler implements ResourceHandler {

    private final Log log = LogFactory.getLog(getClass());

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
     * Handle a request, embodied in a {@link RequestWrapper}, and return a representation object. This implementation
     * packages the RequestWrapper up in a RabbitMQ message and sends it to a remote implementation
     * of {@link ResourceHandler}.
     *
     * @param requestWrapper RequestWrapper for this request
     * @return the output representation object
     */
    public Object handle(RequestWrapper requestWrapper) {
        Object result = null;
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
                        result = new JSONObject(response);
                    } catch (JSONException e) {
                        throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
                    }
                } else if (mediaType.endsWith("application/xml")) {
                    try {
                        StringReader reader = new StringReader(response);
                        SAXBuilder saxBuilder = new SAXBuilder();
                        result = saxBuilder.build(reader);
                    } catch (JDOMException e) {
                        throw new RuntimeException("Caught JDOMException: " + e.getMessage(), e);
                    }
                } else {
                    log.error("accept() Response media type is not supported.");
                }
            } else {
                log.error("accept() Response or media type was null.");
            }
        } catch (IOException e) {
            log.error("accept() Caught IOException: " + e.getMessage(), e);
        }
        return result;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}