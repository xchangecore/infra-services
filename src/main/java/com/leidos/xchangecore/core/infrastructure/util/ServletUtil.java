package com.leidos.xchangecore.core.infrastructure.util;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

public class ServletUtil {

    public static String getPrincipalName() {

        // get the TransportContext for this call
        TransportContext transportContext = TransportContextHolder.getTransportContext();
        if (transportContext == null) {
            return null;
        }
        // Get the service connection from the context
        WebServiceConnection webServiceConnection = transportContext.getConnection();
        // check to see if it is the request is HTTP
        if (!(webServiceConnection instanceof HttpServletConnection)) {
            return null;
        }
        // Cast it
        HttpServletConnection httpServletConnection = (HttpServletConnection) webServiceConnection;
        // Get the request object
        HttpServletRequest request = httpServletConnection.getHttpServletRequest();

        // call a method and return
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return null;
        }
        return principal.getName();
    }
}
