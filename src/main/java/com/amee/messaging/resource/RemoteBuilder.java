package com.amee.messaging.resource;

import com.amee.base.resource.ResourceBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * An implementation of {@link ResourceBuilder} which extends {@link RemoteResourceHandler}.
 *
 * @see RemoteResourceHandler
 * @see ResourceBuilder
 */
@Service
@Scope("prototype")
public class RemoteBuilder extends RemoteResourceHandler implements ResourceBuilder {
}