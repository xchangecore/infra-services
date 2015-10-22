package com.leidos.xchangecore.core.infrastructure.exceptions;

@SuppressWarnings("serial")
public class RemoteCoreUnavailableException
    extends UICDSException {

    public String coreName;

    public RemoteCoreUnavailableException(String coreName) {

        this.coreName = coreName;
    }

    public String getCoreName() {

        return coreName;
    }

    public void setCoreName(String coreName) {

        this.coreName = coreName;
    }

    public String getMessage() {

        return ("Remote core " + coreName + " is unavailable");
    }
}
