package com.leidos.xchangecore.core.infrastructure.messages;

public class ProductPublicationStatusNotificationMessage {

    String owningCore;
    String userID;
    String status;

    public String getOwningCore() {

        return owningCore;
    }

    public void setOwningCore(String owningCore) {

        this.owningCore = owningCore;
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
