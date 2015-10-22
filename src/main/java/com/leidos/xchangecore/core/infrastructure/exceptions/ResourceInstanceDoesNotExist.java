package com.leidos.xchangecore.core.infrastructure.exceptions;

@SuppressWarnings("serial")
public class ResourceInstanceDoesNotExist
    extends UICDSException {

    public ResourceInstanceDoesNotExist() {

        super(ResourceInstanceDoesNotExist.class.getName());
    }

    public ResourceInstanceDoesNotExist(String message) {

        super(message);
    }
}
