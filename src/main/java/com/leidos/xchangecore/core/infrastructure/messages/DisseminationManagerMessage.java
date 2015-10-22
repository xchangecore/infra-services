package com.leidos.xchangecore.core.infrastructure.messages;

import java.util.ArrayList;

public class DisseminationManagerMessage {

    private ArrayList<String> jidsToAdd = new ArrayList<String>();
    private ArrayList<String> jidsToRemove = new ArrayList<String>();

    private ArrayList<String> groupsToAdd = new ArrayList<String>();
    private ArrayList<String> groupsToRemove = new ArrayList<String>();

    public void addJID(String jid) {

        this.jidsToAdd.add(jid);
    }

    public void removeJID(String jid) {

        this.jidsToRemove.add(jid);
    }

    public void addGroup(String group) {

        this.groupsToAdd.add(group);
    }

    public void removeGroup(String group) {

        this.groupsToRemove.add(group);
    }

    public ArrayList<String> getGroupsToAdd() {

        return groupsToAdd;
    }

    public void setGroupsToAdd(ArrayList<String> groupsToAdd) {

        this.groupsToAdd = groupsToAdd;
    }

    public ArrayList<String> getGroupsToRemove() {

        return groupsToRemove;
    }

    public void setGroupsToRemove(ArrayList<String> groupsToRemove) {

        this.groupsToRemove = groupsToRemove;
    }

    private String remoteCore;
    private String interestGroupID;

    public ArrayList<String> getJidsToAdd() {

        return jidsToAdd;
    }

    public void setJidsToAdd(ArrayList<String> jidsToAdd) {

        this.jidsToAdd = jidsToAdd;
    }

    public ArrayList<String> getJidsToRemove() {

        return jidsToRemove;
    }

    public void setJidsToRemove(ArrayList<String> jidsToRemove) {

        this.jidsToRemove = jidsToRemove;
    }

    public String getInterestGroupID() {

        return interestGroupID;
    }

    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    public String getRemoteCore() {

        return remoteCore;
    }

    public void setRemoteCore(String remoteCore) {

        this.remoteCore = remoteCore;
    }

}
