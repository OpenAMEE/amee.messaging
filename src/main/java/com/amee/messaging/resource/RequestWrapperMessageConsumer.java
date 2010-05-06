package com.amee.messaging.resource;

import com.amee.base.domain.VersionBeanFinder;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceHandler;
import com.amee.base.validation.ValidationException;
import com.amee.messaging.RpcMessageConsumer;
import com.amee.messaging.config.ExchangeConfig;
import com.amee.messaging.config.QueueConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class RequestWrapperMessageConsumer extends RpcMessageConsumer {

    private final Log log = LogFactory.getLog(getClass());

    private final static XMLOutputter XML_OUTPUTTER = new XMLOutputter();

    @Autowired
    private VersionBeanFinder versionBeanFinder;

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
            Object target = versionBeanFinder.getBeanForVersion(requestWrapper.getTarget(), requestWrapper.getVersion());
            // Lookup target bean.
            if (target != null) {
                // Target bean found, send request there and get result object.
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

    /**
     * TODO: Make this content / accept type sensitive.
     *
     * @param requestWrapper which encapsulates the request
     * @param handler        which will handle the requestWrapper
     * @return the result serialized as a String
     */
    protected String handle(RequestWrapper requestWrapper, ResourceHandler handler) {
        try {
            Object result = null;
            // Handle the requestWrapper, and deal with any validation exceptions.
            try {
                result = handler.handle(requestWrapper);
            } catch (ValidationException e) {
                result = e.getJSONObject();
            }
            // Handle the result object.
            if (JSONObject.class.isAssignableFrom(result.getClass())) {
                JSONObject o = (JSONObject) result;
                o.put("version", requestWrapper.getVersion().toString());
                return result.toString();
            } else if (Document.class.isAssignableFrom(result.getClass())) {
                Document doc = (Document) result;
                doc.getRootElement().addContent(new Element("Version").setText(requestWrapper.getVersion().toString()));
                return XML_OUTPUTTER.outputString(doc);
            } else {
                // Result object Class not supported
                log.warn("handle() Result object Class not supported: " + result.getClass().getName());
                return "{\"error\": \"Result object Class not supported.\"}";
            }
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
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