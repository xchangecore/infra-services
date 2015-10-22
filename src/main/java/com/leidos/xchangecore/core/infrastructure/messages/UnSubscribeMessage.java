package com.leidos.xchangecore.core.infrastructure.messages;

public class UnSubscribeMessage {

    private String coreJID;
    private String interestGroupID;

    public UnSubscribeMessage(String coreJID, String interestGroupID) {

        super();
        this.coreJID = coreJID;
        this.interestGroupID = interestGroupID;
    }

    public String getCoreJID() {

        return coreJID;
    }

    public String getInterestGroupID() {

        return interestGroupID;
    }

    public void setCoreJID(String coreJID) {

        this.coreJID = coreJID;
    }

    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

}
