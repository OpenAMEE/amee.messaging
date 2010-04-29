package com.amee.messaging.resource;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceHandler;
import com.amee.messaging.RpcMessageConsumer;
import com.amee.messaging.config.ExchangeConfig;
import com.amee.messaging.config.QueueConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class RequestWrapperMessageConsumer extends RpcMessageConsumer {

    private final Log log = LogFactory.getLog(getClass());

    private final static XMLOutputter XML_OUTPUTTER = new XMLOutputter();

    @Autowired
    @Qualifier("requestWrapperExchange")
    private ExchangeConfig exchangeConfig;

    @Autowired
    @Qualifier("requestWrapperQueue")
    private QueueConfig queueConfig;

    protected String handle(String message) {
        try {
            // Obtain RequestWrapper.
            RequestWrapper requestWrapper = new RequestWrapper(new JSONObject(message));
            // Lookup target bean.
            if (applicationContext.containsBean(requestWrapper.getTarget())) {
                // Target bean found, send request there and get result object.
                Object target = applicationContext.getBean(requestWrapper.getTarget());
                // Only ResourceHandler derived implementations are supported.
                if (ResourceHandler.class.isAssignableFrom(target.getClass())) {
                    return handle(requestWrapper, (ResourceHandler) target);
                } else {
                    // Target bean type not supported.
                    log.warn("handle() Target bean type not supported: " + target.getClass());
                    return "{\"error\": \"Could not find target.\"}";
                }
            } else {
                // Target bean not found.
                log.warn("handle() Target bean not found:  " + requestWrapper.getTarget());
                return "{\"error\": \"Could not find target.\"}";
            }
        } catch (JSONException e) {
            log.warn("handle() Caught JSONException: " + e.getMessage());
            return "{\"error\": \"Could not parse JSON.\"}";
        }
    }

    protected String handle(RequestWrapper requestWrapper, ResourceHandler handler) {
        Object result = handler.handle(requestWrapper);
        // Handle result object.
        if (JSONObject.class.isAssignableFrom(result.getClass())) {
            return result.toString();
        } else if (Document.class.isAssignableFrom(result.getClass())) {
            return XML_OUTPUTTER.outputString((Document) result);
        } else {
            // Result object Class not supported
            log.warn("handle() Result object Class not supported: " + result.getClass().getName());
            return "{\"error\": \"Result object Class not supported.\"}";
        }
    }

    @Override
    public ExchangeConfig getExchangeConfig() {
        return exchangeConfig;
    }

    @Override
    public QueueConfig getQueueConfig() {
        return queueConfig;
    }

    @Override
    public String getBindingKey() {
        return getQueueConfig().getName();
    }
}