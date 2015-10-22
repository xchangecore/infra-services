package com.leidos.xchangecore.core.infrastructure.exceptions;

import javax.xml.soap.SOAPException;

@SuppressWarnings("serial")
public class SOAPServiceException
    extends SOAPException {

    public SOAPServiceException(String message) {

        super(message);
    }

}
