package com.leidos.xchangecore.core.infrastructure.service;

import org.springframework.transaction.annotation.Transactional;
import org.uicds.resourceInstanceService.ResourceInstance;
import org.uicds.resourceInstanceService.ResourceInstanceListType;

import com.leidos.xchangecore.core.infrastructure.exceptions.ResourceInstanceDoesNotExist;
import com.leidos.xchangecore.core.infrastructure.exceptions.ResourceProfileDoesNotExist;
import com.leidos.xchangecore.core.infrastructure.model.ResourceInstanceModel;
import com.saic.precis.x2009.x06.base.IdentifierType;

/**
 * The XchangeCore Resource Instance service provides a means to create, discover, and
 * update information about XchangeCore resource instances.
 * 
 * @author Andre Bonner
 * @since 1.0
 * @ssdd
 * 
 */
@Transactional
public interface ResourceInstanceService {

    public static final String RESOURCEINSTANCE_SERVICE_NAME = "ResourceInstanceService";
    public static final String RESOURCE_ID_PREFIX = "ResourceInstance";

    /**
     * Create a resource instance and use the given resource identifier.
     * @param resourceID (optional, if null or empty a UUID will be generated)
     * @return the resource instance model
     * @ssdd
     */
    public ResourceInstanceModel createResourceInstance(IdentifierType id);

    /**
     * 
     * Checking in to a particular ResourceProfile
     * 
     * @param id UICDS identifier - should be the same name as that used for authentication.
     * @param localResourceID - Local system identifier - identifier used by the local system for this resource
     * @param resourceProfileID - Resource Profile identifier - XchangeCore resource profile this application will represent.
     * @return the resource instance model
     * @ssdd
     */
    public ResourceInstanceModel checkin(IdentifierType id,
                                         IdentifierType localResourceID,
                                         IdentifierType resourceProfileID);

    /**
     * Applications must have a Resource Profile created for them by an administrator and must
     * register with the core to begin to receive notifications.
     * 
     * @param id UICDS identifier - should be the same name as that used for authentication.
     * @param localResourceID - Local system identifier - identifier used by the local system for this resource
     * @param resourceProfileID - Resource Profile identifier - XchangeCore resource profile this application will represent.
     * @return the resource instance model
     * @ssdd
     */
    public ResourceInstanceModel register(IdentifierType id,
                                          IdentifierType localResourceID,
                                          IdentifierType resourceProfileID)
        throws ResourceProfileDoesNotExist;

    /**
     * Remove a Resource Instance.  This will remove the resource instance's endpoint and all of 
     * the notification messages on that endpoint.  This cleans up the resources on the core that
     * was allocated to this resource instance and allows a resource instance to then reregister
     * with a differnt resource profile.
     * 
     * @param id UICDS identifier
     * @return the identifier of the resource instance model that was removed
     * @throws ResourceInstanceDoesNotExist
     */
    public IdentifierType unregister(IdentifierType id) throws ResourceInstanceDoesNotExist;

    /**
     * Checkout, leaving/logging off of the system
     * 
     * @param ris
     * @return the resource instance
     * @ssdd
     */
    public ResourceInstance checkout(ResourceInstance ris);

    /**
     * Returns true if the given resource profile id has checked in.
     * @param resourceProfileID
     * @return true, if is checked in
     * @ssdd
     */
    public boolean isCheckedIn(ResourceInstance ris);

    /**
     * Offers the resource profile to the resource instance.  The resource instance will receive a message
     * on its notification queue requesting that it wants to accept this position.
     * @param ris
     * @param resourceProfileID
     * @ssdd
     */
    public void offerProfile(ResourceInstance ris, IdentifierType resourceProfileID);

    /**
     * Apply the given profile to the given resource instance which means subscribe this resource
     * instance to the interests of the given profile.
     * @param ris
     * @param resourceProfileID
     * @return true if profile was applied, false if profile does not exist
     * @ssdd
     */
    public boolean applyProfile(ResourceInstanceModel ris, IdentifierType resourceProfileID)
        throws ResourceProfileDoesNotExist;

    /**
     * Return a list of resource instances whose IDs that match the input query string.
     * Query string is currently not used
     * @return the resource instance list
     * @ssdd
     */
    public ResourceInstanceListType getResourceInstanceList(String queryString);

    /**
     * Returns the resource instance for the given identifier.
     * 
     * @param id UICDS identifier - should be the same name as that used for authentication.
     * @return the resource instance
     * @ssdd
     */
    public ResourceInstance getResourceInstance(IdentifierType id);

    public void setDirectoryService(DirectoryService directoryService);

    /**
     * SystemIntialized Message Handler
     * 
     * @param message SystemInitialized message
     * @return void
     * @see applicationContext
     * @ssdd
     */
    public void systemInitializedHandler(String messgae);

    /**
     * Update endpoint.
     * 
     * Update the resource instance's endpoint address. Could either be a
     * pullpoint address where the user manually looks for messages to pull from
     * or it could be a web service url where messages will automatically be
     * sent to the specified WS-Notification URL.
     * 
     * @param id UICDS identifier - should be the same name as that used for authentication.
     * @param endpoint
     *            - address of the endpoint desired
     * @param isWebService
     *            - tells the notification service whether the endpoint is a WS.
     * @return true, if successful
     * @ssdd
     */
    public boolean updateEndpoint(IdentifierType id, String endpoint, boolean isWebService);

}
