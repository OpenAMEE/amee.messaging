package com.amee.messaging.resource;

import com.amee.base.resource.ResourceBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class RemoteBuilder extends RemoteResourceHandler implements ResourceBuilder {
}