package com.westminster.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider 
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    
    //Logs incoming request: HTTP method and request URI.
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format("[REQUEST]  Method=%s  URI=%s",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri().toString())); // logs every inbound request
    }

    
    //Logs outgoing response: HTTP status code.
     
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format("[RESPONSE] Method=%s  URI=%s  Status=%d",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri().toString(),
            responseContext.getStatus())); // logs final HTTP status for every response
    }
}

