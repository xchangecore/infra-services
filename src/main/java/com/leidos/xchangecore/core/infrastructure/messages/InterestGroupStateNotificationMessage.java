/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.messages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author roger
 * 
 */
public class InterestGroupStateNotificationMessage {

    public static enum State {
        NEW, SHARE, UPDATE, JOIN, RESIGN, DELETE, RESTORE,
    };

    public static enum SharingStatus {
        None, Shared, Joined,
    };

    private String interestGroupID;
    private String interestGroupType;
    private State state;
    private String owningCore;
    // private String coreToShareWith; // only used if and only if state=SHARE/RESIGN
    private String interestGroupInfo;
    private String sharingStatus;
    // remoteCoreMap contains:
    // if state is SHARE, then name of core with which to share the interest group
    // if state is RESIGN, then name of core from which the interest group is resigned
    // if state is RESTORE, then
    // if sharingStatus = SHARED, then name of one or more cores with which the incident is shared
    // if sharingStatus = JOINED, then name and properties of the core to which the interest group
    // is joined

    // data for state=Share
    private Set<String> sharedCoreList = new HashSet<String>();
    private List<String> workProductTypesToShare = new ArrayList<String>();
    private Set extendedMetadata = new HashSet();

    // data for state=JOINED
    private String owmnerProperties;
    private List<String> joinedWPTypes = new ArrayList<String>();

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

    public State getState() {

        return state;
    }

    public void setState(State state) {

        this.state = state;
    }

    public String getOwningCore() {

        return owningCore;
    }

    public void setOwningCore(String owningCore) {

        this.owningCore = owningCore;
    }

    // public String getCoreToShareWith() {
    // return coreToShareWith;
    // }

    // public void setCoreToShareWith(String coreToShareWith) {
    // this.coreToShareWith = coreToShareWith;
    // }

    public String getInterestGroupInfo() {

        return interestGroupInfo;
    }

    public void setInterestGroupInfo(String interestGroupInfo) {

        this.interestGroupInfo = interestGroupInfo;
    }

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

    public List<String> getWorkProductTypesToShare() {

        return workProductTypesToShare;
    }

    public void setWorkProductTypesToShare(List<String> workProductTypesToShare) {

        this.workProductTypesToShare = workProductTypesToShare;
    }

    public String getOwmnerProperties() {

        return owmnerProperties;
    }

    public void setOwmnerProperties(String owmnerProperties) {

        this.owmnerProperties = owmnerProperties;
    }

    public List<String> getJoinedWPTypes() {

        return joinedWPTypes;
    }

    public void setJoinedWPTypes(List<String> joinedWPTypes) {

        this.joinedWPTypes = joinedWPTypes;
    }

    public Set getExtendedMetadata() {

        return extendedMetadata;
    }

    public void setExtendedMetadata(Set extendedMetadata) {

        this.extendedMetadata = extendedMetadata;
    }

}
