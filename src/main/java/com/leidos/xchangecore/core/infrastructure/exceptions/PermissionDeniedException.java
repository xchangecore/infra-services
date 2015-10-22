package com.leidos.xchangecore.core.infrastructure.exceptions;

import javax.xml.soap.SOAPException;

public class PermissionDeniedException
    extends SOAPException {

    /**
     * 
     */
    private static final long serialVersionUID = 3140830666593806208L;

    public PermissionDeniedException() {

        super("Permission Denied");
    }
}
