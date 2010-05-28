package com.amee.messaging.resource;

import com.amee.base.domain.VersionBeanFinder;
import com.amee.base.resource.NotFoundException;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceHandler;
import com.amee.base.transaction.TransactionEvent;
import com.amee.base.transaction.TransactionEventType;
import com.amee.base.utils.ThreadBeanHolder;
import com.amee.base.validation.ValidationException;
import com.amee.messaging.MapRpcMessageConsumer;
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

import java.util.HashMap;
import java.util.Map;

public class RequestWrapperMessageConsumer extends MapRpcMessageConsumer {

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

    protected Map<String, Object> handle(Map<String, Object> message) {
        try {
            // Obtain RequestWrapper.
            RequestWrapper requestWrapper = new RequestWrapper(new JSONObject(message.get("requestWrapper").toString()));
            Object target = versionBeanFinder.getBeanForVersion(requestWrapper.getTarget(), requestWrapper.getVersion());
            // Lookup target bean.
            if (target != null) {
                // Target bean found, send request there and get result object.
                // Only ResourceHandler derived implementations are supported.
                if (ResourceHandler.class.isAssignableFrom(target.getClass())) {
                    return handle(requestWrapper, (ResourceHandler) target);
                } else {
                    // Target bean type not supported.
                    log.error("handle() Target bean type not supported: " + target.getClass());
                    return error(requestWrapper, "Could not find target.");
                }
            } else {
                // Target bean not found.
                log.error("handle() Target bean not found:  " + requestWrapper.getTarget());
                return error(requestWrapper, "Could not find target.");
            }
        } catch (JSONException e) {
            log.error("handle() Caught JSONException: " + e.getMessage());
            return error(null, "Could not parse RequestWrapper");
        }
    }

    /**
     * @param requestWrapper which encapsulates the request
     * @param handler        which will handle the requestWrapper
     * @return the result serialized as a String
     */
    protected Map<String, Object> handle(RequestWrapper requestWrapper, ResourceHandler handler) {
        try {
            Object response;
            // Handle the requestWrapper, and deal with any validation exceptions.
            try {
                // Callback hook.
                onBeforeBegin();
                // Do the work.
                response = handler.handle(requestWrapper);
            } catch (ValidationException e) {
                // TODO: Support XML too.
                response = e.getJSONObject();
            } catch (NotFoundException e) {
                // TODO: Support XML too.
                response = e.getJSONObject();
            } catch (Exception e) {
                log.error("handle() Caught Exception: " + e.getMessage(), e);
                return error(requestWrapper, "Internal error");
            } catch (Throwable t) {
                log.error("handle() Caught Throwable: " + t.getMessage(), t);
                return error(requestWrapper, "Internal error");
            } finally {
                // Callback hook.
                onEnd();
            }
            // Handle the result object.
            if (JSONObject.class.isAssignableFrom(response.getClass())) {
                // JSON.
                JSONObject o = (JSONObject) response;
                o.put("version", requestWrapper.getVersion().toString());
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("mediaType", "application/json");
                result.put("response", response.toString());
                return result;
            } else if (Document.class.isAssignableFrom(response.getClass())) {
                // XML.
                Document doc = (Document) response;
                doc.getRootElement().addContent(new Element("Version").setText(requestWrapper.getVersion().toString()));
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("mediaType", "application/xml");
                result.put("response", XML_OUTPUTTER.outputString(doc));
                return result;
            } else {
                // Result object Class not supported
                log.warn("handle() Result object Class not supported: " + response.getClass().getName());
                return error(requestWrapper, "Result object Class not supported.");
            }
        } catch (JSONException e) {
            return error(requestWrapper, "Internal error");
        }
    }

    public void onBeforeBegin() {
        // Ensure threads are clear.
        ThreadBeanHolder.clear();
        // Publish BEFORE_BEGIN event.
        applicationContext.publishEvent(new TransactionEvent(this, TransactionEventType.BEFORE_BEGIN));
    }

    public void onEnd() {
        // Publish END event.
        applicationContext.publishEvent(new TransactionEvent(this, TransactionEventType.END));
        // Ensure threads are clear.
        ThreadBeanHolder.clear();
    }

    /**
     * TODO: Make this media type sensitive.
     *
     * @param requestWrapper
     * @param message
     * @return
     */
    protected Map<String, Object> error(RequestWrapper requestWrapper, String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("response", "{\"status\": \"ERROR\"}, {\"error\": \"" + message + "\"}");
        result.put("mediaType", "application/json");
        return result;
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