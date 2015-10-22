package com.leidos.xchangecore.core.infrastructure.exceptions;

@SuppressWarnings("serial")
public class NoShareRuleInAgreementException
    extends UICDSException {

    private String localCore;
    private String remoteCore;
    private String interestGroupType;
    private String interestGroupSubtype;

    public NoShareRuleInAgreementException(String localCore, String remoteCore,
                                           String interestGroupType, String interestGroupSubtype) {

        setLocalCore(localCore);
        setRemoteore(remoteCore);
        setInterestGroupType(interestGroupType);
        setInterestGroupSubtype(interestGroupSubtype);
    }

    public String getLocalCore() {

        return localCore;
    }

    private void setLocalCore(String localCore) {

        this.localCore = localCore;
    }

    public String getRemoteCore() {

        return remoteCore;
    }

    private void setRemoteore(String remoteCore) {

        this.remoteCore = remoteCore;
    }

    public String getInterestGroupType() {

        return interestGroupType;
    }

    private void setInterestGroupType(String interestGroupType) {

        this.interestGroupType = interestGroupType;
    }

    public String getInterestGroupSubtype() {

        return interestGroupSubtype;
    }

    private void setInterestGroupSubtype(String interestGroupSubtype) {

        this.interestGroupSubtype = interestGroupSubtype;
    }

    public String getMessage() {

        String message;
        message = super.getMessage() + " => Share rules are either undefined or disabled";
        if ((interestGroupType != null) && (interestGroupSubtype != null)) {
            message += " for interestGroup type: " + interestGroupType + " subtype: " +
                       interestGroupSubtype + " in the agreement  between " + localCore + " and " +
                       remoteCore;
        }
        return message;
    }

}
