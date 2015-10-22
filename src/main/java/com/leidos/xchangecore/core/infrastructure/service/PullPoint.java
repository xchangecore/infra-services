package com.leidos.xchangecore.core.infrastructure.service;

import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

/**
 * WS-Notification v1.3 Interface
 * 
 * @ssdd
 */

public interface PullPoint {

    /**
     * @param num =the number of messages to get
     * @return Notification messages
     * @ssdd
     */
    public NotificationMessageHolderType[] getMessages(String entityID, int num);

    /**
     * Create a new pullpoint to retrieve notification via explicit client "pull".
     * 
     * @return EPR of the new pullpoint
     * @ssdd
     */
    public EndpointReferenceType createPullPoint(String entityID);

    /**
     * Destroys a pullpoint and all the waiting notifications on that pullpoint
     * 
     * @return true if found and destroyed, false if not found
     * @ssdd
     */
    public boolean destroyPullPoint(String entityID);

    /**
     * Gets the pull point.
     * 
     * @param entityID the entity id
     * 
     * @return the pull point
     * @ssdd
     */
    public EndpointReferenceType getPullPoint(String entityID);
}
