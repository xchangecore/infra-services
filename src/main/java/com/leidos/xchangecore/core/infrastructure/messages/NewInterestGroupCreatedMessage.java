package com.leidos.xchangecore.core.infrastructure.messages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewInterestGroupCreatedMessage {

    public String interestGroupID;
    public String interestGroupType;
    public String owningCore;
    public boolean restored;
    public String sharingStatus;
    public Set<String> sharedCoreList = new HashSet<String>();
    public String ownerProperties;
    public List<String> joinedWPTYpes = new ArrayList<String>();

    public String getSharingStatus() {

        return sharingStatus;
    }

    public void setSharingStatus(String sharingStatus) {

        this.sharingStatus = sharingStatus;
    }

    public Set<String> getSharedCoreList() {

        return sharedCoreList;
    }

    public void setSharedCoreList(Set<String> sharedCoreList) {

        this.sharedCoreList = sharedCoreList;
    }

    public String getOwnerProperties() {

        return ownerProperties;
    }

    public void setOwnerProperties(String ownerProperties) {

        this.ownerProperties = ownerProperties;
    }

    public String getInterestGroupID() {

        return interestGroupID;
    }

    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    public String getInterestGroupType() {

        return interestGroupType;
    }

    public void setInterestGroupType(String interestGroupType) {

        this.interestGroupType = interestGroupType;
    }

    public String getOwningCore() {

        return owningCore;
    }

    public void setOwningCore(String owningCore) {

        this.owningCore = owningCore;
    }

    public List<String> getJoinedWPTYpes() {

        return joinedWPTYpes;
    }

    public void setJoinedWPTYpes(List<String> joinedWPTYpes) {

        this.joinedWPTYpes = joinedWPTYpes;
    }

    public boolean isRestored() {

        return restored;
    }

    public void setRestored(boolean restored) {

        this.restored = restored;
    }
}
