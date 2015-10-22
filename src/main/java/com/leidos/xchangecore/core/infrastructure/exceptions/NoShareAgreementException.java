package com.leidos.xchangecore.core.infrastructure.exceptions;

@SuppressWarnings("serial")
public class NoShareAgreementException
    extends UICDSException {

    private String localCore;
    private String remoteCore;
    private String message;

    public NoShareAgreementException(String localCore, String remoteCore) {

        setLocalCore(localCore);
        setRemoteore(remoteCore);
        setMessage();
    }

    public String getLocalCore() {

        return localCore;
    }

    public void setLocalCore(String localCore) {

        this.localCore = localCore;
        setMessage();
    }

    public String getRemoteCore() {

        return remoteCore;
    }

    public void setRemoteore(String remoteCore) {

        this.remoteCore = remoteCore;
        setMessage();
    }

    public String getMessage() {

        return message;
    }

    private void setMessage() {

        message = super.getMessage() + " => No Share Agreement between " + localCore + " and " +
                  remoteCore;
    }
}
