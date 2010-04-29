package com.amee.messaging.resource;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.messaging.MessageService;
import com.amee.messaging.config.ExchangeConfig;
import com.rabbitmq.client.RpcClient;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;

@Service
@Scope("prototype")
public class RemoteDOMBuilder implements ResourceBuilder {

    @Autowired
    private MessageService messageService;

    @Autowired
    @Qualifier("requestWrapperExchange")
    private ExchangeConfig exchangeConfig;

    private String target;

    public Document handle(RequestWrapper requestWrapper) {
        Document document = null;
        try {
            requestWrapper.setTarget(getTarget());
            RpcClient rpcClient = new RpcClient(
                    messageService.getChannel(exchangeConfig),
                    exchangeConfig.getName(),
                    exchangeConfig.getName());
            // Get remote result.
            String result = rpcClient.stringCall(requestWrapper.toJSONObject().toString());
            // Convert to StringReader.
            StringReader reader = new StringReader(result);
            // Return the Document.
            SAXBuilder saxBuilder = new SAXBuilder();
            document = saxBuilder.build(reader);
        } catch (IOException e) {
            // TODO.
        } catch (JDOMException e) {
            // TODO.
        }
        return document;
    }

    public String getMediaType() {
        return "application/xml";
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}