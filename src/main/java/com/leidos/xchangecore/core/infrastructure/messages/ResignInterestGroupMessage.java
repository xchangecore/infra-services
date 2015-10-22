package com.leidos.xchangecore.core.infrastructure.messages;

public class ResignInterestGroupMessage {

    private String interestGroupID;

    public String getIncidentID() {

        return interestGroupID;
    }

    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    public ResignInterestGroupMessage(String interestGroupID) {

        setInterestGroupID(interestGroupID);
    }

}
