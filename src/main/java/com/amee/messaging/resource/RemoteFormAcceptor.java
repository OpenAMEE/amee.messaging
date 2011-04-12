package com.amee.messaging.resource;

import com.amee.base.resource.ResourceAcceptor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * An implementation of {@link ResourceAcceptor} which extends {@link RemoteResourceHandler}.
 *
 * @see RemoteResourceHandler
 * @see ResourceAcceptor
 */
@Service
@Scope("prototype")
public class RemoteFormAcceptor extends RemoteResourceHandler implements ResourceAcceptor {
}