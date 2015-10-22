package com.leidos.xchangecore.core.infrastructure.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ShareInterestGroupMessage
    implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4314809393136591491L;

    private String interestGroupID;
    private String remoteCore;
    private String interestGroupInfo;
    private List<String> workProductTypesToShare = new ArrayList<String>();

    public String getInterestGroupID() {

        return interestGroupID;
    }

    public String getInterestGroupInfo() {

        return interestGroupInfo;
    }

    public String getRemoteCore() {

        return remoteCore;
    }

    public List<String> getWorkProductTypesToShare() {

        return workProductTypesToShare;
    }

    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    public void setInterestGroupInfo(String interestGroupInfo) {

        this.interestGroupInfo = interestGroupInfo;
    }

    public void setRemoteCore(String remoteCore) {

        this.remoteCore = remoteCore;
    }

    public void setWorkProductTypesToShare(List<String> workProductTypesToShare) {

        this.workProductTypesToShare = workProductTypesToShare;
    }

}
