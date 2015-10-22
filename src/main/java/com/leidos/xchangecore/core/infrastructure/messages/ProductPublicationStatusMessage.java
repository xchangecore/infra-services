package com.leidos.xchangecore.core.infrastructure.messages;

public class ProductPublicationStatusMessage {

    String requestingCore;
    String userID;
    String status;

    public String getRequestingCore() {

        return requestingCore;
    }

    public void setRequestingCore(String requestingCore) {

        this.requestingCore = requestingCore;
    }

    public String getUserID() {

        return userID;
    }

    public void setUserID(String userID) {

        this.userID = userID;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

}
