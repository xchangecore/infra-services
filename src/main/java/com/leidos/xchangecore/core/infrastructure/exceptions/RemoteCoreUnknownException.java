package com.leidos.xchangecore.core.infrastructure.exceptions;

@SuppressWarnings("serial")
public class RemoteCoreUnknownException
    extends UICDSException {

    public String coreName;

    public RemoteCoreUnknownException(String coreName) {

        this.coreName = coreName;
    }

    public String getCoreName() {

        return coreName;
    }

    public void setCoreName(String coreName) {

        this.coreName = coreName;
    }

    public String getMessage() {

        return ("Unknown remote core " + coreName);
    }
}
