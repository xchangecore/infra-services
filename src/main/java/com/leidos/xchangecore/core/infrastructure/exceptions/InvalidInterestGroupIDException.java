package com.leidos.xchangecore.core.infrastructure.exceptions;

@SuppressWarnings("serial")
public class InvalidInterestGroupIDException
    extends UICDSException {

    private String interestGroupID;

    public InvalidInterestGroupIDException(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    public String getMessage() {

        return ("Invalid interest group ID: " + interestGroupID);
    }
}
