package com.leidos.xchangecore.core.infrastructure.messages;

import java.util.ArrayList;
import java.util.List;

public class JoinedInterestGroupNotificationMessage {

    public String interestGroupID;
    public String interestGroupType;
    public String owner;
    public String ownerProperties;
    public String interestGroupInfo;
    public List<String> joinedWPTypes = new ArrayList<String>();

    public String getInterestGroupID() {

        return interestGroupID;
    }

    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    public String getInterestGroupType() {

        return interestGroupType;
    }

    public String getOwner() {

        return owner;
    }

    public String getInterestGroupInfo() {

        return interestGroupInfo;
    }

    public void setInterestGroupType(String interestGroupType) {

        this.interestGroupType = interestGroupType;
    }

    public void setOwner(String owner) {

        this.owner = owner;
    }

    public String getOwnerProperties() {

        return ownerProperties;
    }

    public void setOwnerProperties(String ownerProperties) {

        this.ownerProperties = ownerProperties;
    }

    public void setInterestGroupInfo(String interestGroupInfo) {

        this.interestGroupInfo = interestGroupInfo;
    }

    public List<String> getJoinedWPTypes() {

        return joinedWPTypes;
    }

    public void setJoinedWPTypes(List<String> joinedWPTypes) {

        this.joinedWPTypes = joinedWPTypes;
    }

}
