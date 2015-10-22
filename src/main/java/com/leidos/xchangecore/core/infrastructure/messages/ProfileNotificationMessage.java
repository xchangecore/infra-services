package com.leidos.xchangecore.core.infrastructure.messages;

public class ProfileNotificationMessage {

    public static enum NotificationState {
        CREATE, UPDATE, DELETE, RESTORE,
    }

    public static final String NAME = "ProfileNotificationMessage";;

    private NotificationState state;
    private String userID;
    private String userName;

    public NotificationState getState() {

        return state;
    }

    public String getUserID() {

        return userID;
    }

    public String getUserName() {

        return userName;
    }

    public void setState(NotificationState state) {

        this.state = state;
    }

    public void setUserID(String userID) {

        this.userID = userID;
    }

    public void setUserName(String userName) {

        this.userName = userName;
    }

    public String toString() {

        return userID;
    }

}
