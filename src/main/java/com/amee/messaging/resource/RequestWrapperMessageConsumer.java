package com.amee.messaging.resource;

import com.amee.base.domain.VersionBeanFinder;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceException;
import com.amee.base.resource.ResourceHandler;
import com.amee.base.resource.TimedOutException;
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
import java.util.concurrent.*;

public class RequestWrapperMessageConsumer extends MapRpcMessageConsumer {

    private final Log log = LogFactory.getLog(getClass());

    private final static XMLOutputter XML_OUTPUTTER = new XMLOutputter();

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    private VersionBeanFinder versionBeanFinder;

    @Autowired
    @Qualifier("requestWrapperExchange")
    private ExchangeConfig exchangeConfig;

    @Autowired
    @Qualifier("requestWrapperQueue")
    private QueueConfig queueConfig;

    private int timeout = 25;

    protected Map<String, Object> handle(Map<String, Object> message) {
        try {
            // Obtain RequestWrapper from incoming message.
            RequestWrapper requestWrapper = new RequestWrapper(new JSONObject(message.get("requestWrapper").toString()));
            // Lookup target bean.
            Object target = versionBeanFinder.getBeanForVersion(requestWrapper.getTarget(), requestWrapper.getVersion());
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
            log.error("handle() Caught JSONException: " + e.getMessage(), e);
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
                // Do the work.
                response = handleWithTimeout(requestWrapper, handler);
            } catch (ResourceException e) {
                response = e.getResponse(requestWrapper);
            } catch (Exception e) {
                // A catch-all to prevent the thread from dying.
                log.error("handle() Caught Exception: " + e.getMessage(), e);
                return error(requestWrapper, "Internal error.");
            } catch (Throwable t) {
                // A catch-all to prevent the thread from dying.
                log.error("handle() Caught Throwable: " + t.getMessage(), t);
                return error(requestWrapper, "Internal error.");
            }
            // Response should not be null.
            if (response != null) {
                // Handle the response object.
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
                    // Response object class not supported
                    log.error("handle() Response object class not supported: " + response.getClass().getName());
                    return error(requestWrapper, "Internal error.");
                }
            } else {
                // Response object was null.
                log.error("handle() Response object was null.");
                return error(requestWrapper, "Internal error.");
            }
        } catch (JSONException e) {
            // This is unlikely to happen.
            log.error("handle() Caught JSONException: " + e.getMessage(), e);
            return error(requestWrapper, "Internal error.");
        }
    }

    /**
     * TODO: What happens to values in ThreadLocal?
     */
    protected Object handleWithTimeout(final RequestWrapper requestWrapper, final ResourceHandler handler) throws Throwable {
        Object response = null;
        Callable<Object> task = new Callable<Object>() {
            public Object call() throws Exception {
                return handler.handle(requestWrapper);
            }
        };
        log.debug("handleWithTimeout() Submitting the task.");
        Future<Object> future = executor.submit(task);
        try {
            response = future.get(getTimeout(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("handleWithTimeout() Caught TimeoutException (aborting).");
            throw new TimedOutException();
        } catch (InterruptedException e) {
            log.error("handleWithTimeout() Caught InterruptedException (aborting): " + e.getMessage(), e);
        } catch (ExecutionException e) {
            log.debug("handleWithTimeout() Caught ExecutionException: " + e.getMessage());
            throw e.getCause();
        } finally {
            log.debug("handleWithTimeout() Canceling the task via its Future.");
            // TODO: One day we should switch this to true.
            // TODO: This can be true if we trust all tasks to be killed cleanly.
            future.cancel(false);
        }
        return response;
    }

    /**
     * Constructs an error response. Will return application/json if this is requested, otherwise
     * application/xml.
     *
     * @param requestWrapper current RequestWrapper
     * @param message        error message to include in response.
     * @return Error response
     */
    protected Map<String, Object> error(RequestWrapper requestWrapper, String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (requestWrapper.getAcceptedMediaTypes().contains("application/json")) {
            try {
                JSONObject o = new JSONObject();
                o.put("status", "ERROR");
                o.put("error", message);
                o.put("version", requestWrapper.getVersion().toString());
                result.put("response", o.toString());
                result.put("mediaType", "application/json");
            } catch (JSONException e) {
                // This should really never ever happen.
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        } else {
            Element rootElem = new Element("Representation");
            rootElem.addContent(new Element("Status").setText("ERROR"));
            rootElem.addContent(new Element("Error").setText(message));
            rootElem.addContent(new Element("Version").setText(requestWrapper.getVersion().toString()));
            result.put("response", XML_OUTPUTTER.outputString(new Document(rootElem)));
            result.put("mediaType", "application/xml");
        }
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}