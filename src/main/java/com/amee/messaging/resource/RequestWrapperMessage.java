package com.amee.messaging.resource;

import com.amee.base.resource.RequestWrapper;
import com.amee.messaging.Message;
import com.amee.messaging.MessagingException;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestWrapperMessage extends Message {

    private RequestWrapper requestWrapper;

    public RequestWrapperMessage(Object source) {
        super(source);
    }

    public RequestWrapperMessage(Object source, RequestWrapper requestWrapper) {
        this(source);
        this.setRequestWrapper(requestWrapper);
    }

    public RequestWrapperMessage(Object source, String message) {
        super(source, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || !RequestWrapperMessage.class.isAssignableFrom(o.getClass())) return false;
        RequestWrapperMessage message = (RequestWrapperMessage) o;
        return message.getRequestWrapper().equals(getRequestWrapper());
    }

    @Override
    public String getMessage() {
        return getRequestWrapper().toJSONObject().toString();
    }

    @Override
    public void setMessage(String message) {
        try {
            setRequestWrapper(new RequestWrapper(new JSONObject(message)));
        } catch (JSONException e) {
            throw new MessagingException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    public RequestWrapper getRequestWrapper() {
        return requestWrapper;
    }

    public void setRequestWrapper(RequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }
}
