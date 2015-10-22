package com.leidos.xchangecore.core.infrastructure.messages;

public class DeleteInterestGroupForRemoteCoreMessage {

    public String remoteCoreName;
    public String interestGroupID = null;

    public DeleteInterestGroupForRemoteCoreMessage(String remoteCoreName) {

        super();
        this.remoteCoreName = remoteCoreName;
    }

    public DeleteInterestGroupForRemoteCoreMessage(String remoteCoreName, String interestGroupID) {

        super();
        this.remoteCoreName = remoteCoreName;
        this.interestGroupID = interestGroupID;
    }

    public String getInterestGroupID() {

        return interestGroupID;
    }

    public String getRemoteCoreName() {

        return remoteCoreName;
    }

    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    public void setRemoteCoreName(String remoteCoreName) {

        this.remoteCoreName = remoteCoreName;
    }
}
