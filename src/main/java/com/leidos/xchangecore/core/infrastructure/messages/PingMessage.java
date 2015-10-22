package com.leidos.xchangecore.core.infrastructure.messages;

public class PingMessage {

    private String remoteJID;
    private boolean isConnected;

    public PingMessage(String remoteJID, boolean isConnected) {

        super();
        this.remoteJID = remoteJID;
        this.isConnected = isConnected;
    }

    public String getRemoteJID() {

        return remoteJID;
    }

    public boolean isConnected() {

        return isConnected;
    }

    public void setConnected(boolean isConnected) {

        this.isConnected = isConnected;
    }

    public void setRemoteJID(String remoteJID) {

        this.remoteJID = remoteJID;
    }
}
