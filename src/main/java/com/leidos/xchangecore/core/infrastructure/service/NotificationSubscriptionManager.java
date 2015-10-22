package com.leidos.xchangecore.core.infrastructure.service;

public interface NotificationSubscriptionManager {

    // passing the message along with the interest group
    public boolean push(String IGID, String message);

    // add the permission for user/jid to access an interest group
    public boolean addUser(String IGID, String jid);

    // remove the permission for user/jid to access the interest group 
    public boolean RemoveUser(String IGID, String jid);

    // to return whether the user/jid can access the interest group or not
    public boolean getPermission(String IGID, String jid);
}
