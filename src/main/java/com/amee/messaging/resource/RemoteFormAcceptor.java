package com.amee.messaging.resource;

import com.amee.base.resource.ResourceAcceptor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class RemoteFormAcceptor extends RemoteResourceHandler implements ResourceAcceptor {
}