package com.leidos.xchangecore.core.infrastructure.messages;

public class CoreStatusUpdateMessage {

    public static final String Status_Available = "available";
    public static final String Status_UnAvailable = "unavailable";
    public static final String Status_UnSubscribed = "unsubscribed";
    public static final String Status_Subscribed = "subscribed";

    String coreName;
    String coreStatus;
    String coreLatitude;
    String coreLongitude;

    public CoreStatusUpdateMessage(String coreName, String coreStatus, String latitude,
                                   String longitude) {

        setCoreName(coreName);
        setCoreStatus(coreStatus);
        setCoreLatitude(latitude);
        setCoreLongitude(longitude);
    }

    public String getCoreLatitude() {

        return coreLatitude;
    }

    public String getCoreLongitude() {

        return coreLongitude;
    }

    public String getCoreName() {

        return coreName;
    }

    public String getCoreStatus() {

        return coreStatus;
    }

    public void setCoreLatitude(String coreLatitude) {

        this.coreLatitude = coreLatitude;
    }

    public void setCoreLongitude(String coreLongitude) {

        this.coreLongitude = coreLongitude;
    }

    public void setCoreName(String coreName) {

        this.coreName = coreName;
    }

    //    public CoreStatusUpdateMessage(String coreName, String coreStatus) {
    //        setCoreName(coreName);
    //        setCoreStatus(coreStatus);
    //        setCoreLatitude("");
    //        setCoreLongitude("");
    //    }

    public void setCoreStatus(String coreStatus) {

        this.coreStatus = coreStatus;
    }

}
