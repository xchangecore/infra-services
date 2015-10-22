package com.leidos.xchangecore.core.infrastructure.exceptions;

@SuppressWarnings("serial")
public class ResourceProfileDoesNotExist
    extends UICDSException {

    public ResourceProfileDoesNotExist() {

        super(ResourceProfileDoesNotExist.class.getName());
    }

    public ResourceProfileDoesNotExist(String message) {

        super(message);
    }
}
