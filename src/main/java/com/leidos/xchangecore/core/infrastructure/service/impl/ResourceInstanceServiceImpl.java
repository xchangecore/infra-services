package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.util.Iterator;
import java.util.List;

import org.oasisOpen.docs.wsn.b2.FilterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.directoryServiceData.WorkProductTypeListType;
import org.uicds.resourceInstanceService.ResourceInstance;
import org.uicds.resourceInstanceService.ResourceInstanceListType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import com.leidos.xchangecore.core.infrastructure.dao.ResourceInstanceDAO;
import com.leidos.xchangecore.core.infrastructure.exceptions.EmptySubscriberNameException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductIDException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductTypeException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NullSubscriberException;
import com.leidos.xchangecore.core.infrastructure.exceptions.ResourceInstanceDoesNotExist;
import com.leidos.xchangecore.core.infrastructure.exceptions.ResourceProfileDoesNotExist;
import com.leidos.xchangecore.core.infrastructure.model.InterestElement;
import com.leidos.xchangecore.core.infrastructure.model.ResourceInstanceModel;
import com.leidos.xchangecore.core.infrastructure.model.ResourceProfileModel;
import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.service.NotificationService;
import com.leidos.xchangecore.core.infrastructure.service.ResourceInstanceService;
import com.leidos.xchangecore.core.infrastructure.service.ResourceProfileService;
import com.leidos.xchangecore.core.infrastructure.util.NotificationUtils;
import com.leidos.xchangecore.core.infrastructure.util.ResourceInstanceUtil;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;
import com.saic.precis.x2009.x06.base.IdentifierType;

/**
 * A XchangeCore resource instance represents a resource that is capable of receiving notifications.
 * Resource instances are maintained in a hibernate model.
 *
 * @author Andre Bonner
 * @see com.leidos.xchangecore.core.infrastructure.model.InterestElement InterestElement Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.ResourceInstanceModel ResourceInstance Data
 *      Model
 * @see com.leidos.xchangecore.core.infrastructure.model.ResourceProfileModel ResourceProfile Data
 *      Model
 * @ssdd
 */
public class ResourceInstanceServiceImpl
    implements ResourceInstanceService, ServiceNamespaces {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private NotificationService notificationService;
    private ResourceProfileService resourceProfileService;
    private ResourceInstanceDAO resourceInstanceDAO;
    private DirectoryService directoryService;
    private ConfigurationService configurationService;

    /**
     * Apply profile : Add the interests from the profile to the endpoint
     *
     * TODO: Currently this method doesn't filter out work product types that this resource instance
     * is already subscribed to so applying a second resource profile may create duplicate
     * subscriptions on the endpoint causing duplicate notification messages.
     *
     * @param resourceModel
     *            the resource model
     * @param resourceProfileID
     *            the resource profile id
     *
     * @return true, if successful
     * @ssdd
     */
    @Override
    public boolean applyProfile(ResourceInstanceModel resourceModel,
                                IdentifierType resourceProfileID)
        throws ResourceProfileDoesNotExist {

        // get the profile from the ResourceProfile service
        final ResourceProfileModel profileModel = resourceProfileService.getProfile(resourceProfileID);

        if (profileModel == null)
            throw new ResourceProfileDoesNotExist(resourceProfileID.getStringValue() +
                                                  " does not exist");
        else if (resourceModel.getEndpoints().size() > 0 &&
                 !resourceModel.getProfiles().contains(resourceProfileID.getStringValue())) {

            // get the resource's endpoint
            final Iterator<String> i = resourceModel.getEndpoints().iterator();
            final EndpointReferenceType endpoint = EndpointReferenceType.Factory.newInstance();
            endpoint.addNewAddress().setStringValue(i.next());

            // TODO: don't subscribe to interests that this endpoint is already subscribed to
            for (final InterestElement interest : profileModel.getInterests()) {
                FilterType filter = null;
                filter = NotificationUtils.getFilterFromInterest(interest);
                try {
                    notificationService.subscribe(endpoint, filter);
                } catch (final InvalidProductTypeException e) {
                    logger.error("invalid work product type in subscription request");
                } catch (final NullSubscriberException e) {
                    logger.error("null subscriber in subscription request");
                } catch (final EmptySubscriberNameException e) {
                    logger.error("empty subscriber name in subscription requeet");
                } catch (final InvalidProductIDException e) {
                    logger.error("invalid product id in subscription request");
                }
            }

            resourceModel.getProfiles().add(resourceProfileID.getStringValue());

            if (profileModel.getDescription() != null)
                resourceModel.setDescription(profileModel.getDescription());
            else
                resourceModel.setDescription(resourceModel.getIdentifier() + "-" +
                                             profileModel.getIdentifier());

            return true;
        }

        return false;
    }

    /**
     * Applications can check in with or without a profile.
     *
     * @param id
     *            the id
     * @param localResourceID
     *            the local resource id
     * @param resourceProfileID
     *            the resource profile id
     *
     * @return the resource instance model
     * @ssdd
     */
    @Override
    public ResourceInstanceModel checkin(IdentifierType id,
                                         IdentifierType localResourceID,
                                         IdentifierType resourceProfileID) {

        // Applications can check in with or without a profile. Checking in with a profile shows how
        // an application checks into XchangeCore to enable it to start receiving notifications. This
        // diagram represents an application that is designed to monitor the core. It is probably
        // going to have interests in all new incidents and alerts. This application's profile is
        // created by an administrative GUI delivered with the core. All Resource Profiles will be
        // created through the administrative interface. Applications will not be able to create a
        // profile.
        //
        // The important part about an application checking into the core is that it authenticates
        // with the LDAP server and therefore gets a notification endpoint created with its
        // profile's interests and an XMPP server connection created. This allows the application to
        // start receiving notifications and advertise that it is online through its presence. Also,
        // the JID is added to the Notification Service's roster so that it can receive unavailable
        // notifications for the service if the application takes over the the XMPP connection. If
        // an application does not take over its XMPP connection then it should check out with the
        // Notification Service to set its XMPP status to unavailable.
        //
        // This diagram also shows how an application should start up. After checking in it should
        // get it's profile and for each interest in the profile it should call GetCurrentMessage on
        // the Notification Service to get the most current notification message for each of it's
        // interests. After this it can start a periodic polling for notifications with the
        // GetMessage operation on the Notification Service.
        //
        // When an application checks out it can specify if it wants the endpoint to be deleted or
        // not. If the endpoint is deleted then the next time the application checks in there will
        // be no messages when it calls GetMessages. It will have to start with GetCurrentMessages
        // for its interests. If it does not delete the endpoint then the Notification Service will
        // continue to deliver notifications to the endpoint that will be stored on the server. One
        // issue here is that we may need set a limit on the notification queue size.

        ResourceInstanceModel resourceModel = resourceInstanceDAO.findByIdentifier(id.getStringValue());
        if (resourceModel == null) {
            resourceModel = createResourceInstanceModel();
            if (localResourceID != null && localResourceID.getStringValue() != null)
                resourceModel.setLocalResourceID(localResourceID.getStringValue());
        }

        // Persist the resource instance
        resourceInstanceDAO.makePersistent(resourceModel);

        return resourceModel;
    }

    /**
     * Checkout.
     *
     * @param ris
     *            the resource instance
     *
     * @return the resource instance
     * @ssdd
     */
    @Override
    public ResourceInstance checkout(ResourceInstance ris) {

        // TODO Auto-generated method stub
        return ResourceInstance.Factory.newInstance();
    }

    /**
     * Creates the resource instance with notification endpoint.
     *
     * @param id
     *            the id
     *
     * @return the resource instance model
     * @ssdd
     */
    @Override
    public ResourceInstanceModel createResourceInstance(IdentifierType id) {

        // create resposne

        // Check the database to see if we already have a resource by this name
        ResourceInstanceModel r = null;

        // If the resource does not exist then create a new one
        if (r == null) {
            r = createResourceInstanceModel();

            // Create a new endpoint
            final EndpointReferenceType endpoint = notificationService.createPullPoint(id.getStringValue());
            r.getEndpoints().add(endpoint.getAddress().getStringValue());

            // fli added on 11/30/2011
            // r.setNotMsgCount(notificationService.findMsgCountByEntityId(id.getStringValue()));

            // create a unique identifier
            r.setIdentifier(id.getStringValue());

            // populate the model
            r.setResourceID(id.getStringValue());
            if (id.getLabel() != null)
                r.setLabel(id.getLabel());
            else
                r.setLabel("ID");

            // Set the owning core
            r.setOwningCore(configurationService.getFullyQualifiedHostName());

        }
        return r;
    }

    private ResourceInstanceModel createResourceInstanceModel() {

        // Create ResoureInstance/ResourceInstanceID
        final ResourceInstanceModel model = new ResourceInstanceModel();
        return model;
    }

    public ConfigurationService getConfigurationService() {

        return configurationService;
    }

    public NotificationService getNotificationService() {

        return notificationService;
    }

    /**
     * Gets the resource instance.
     *
     * @param id
     *            the id
     *
     * @return the resource instance
     * @ssdd
     */
    @Override
    public ResourceInstance getResourceInstance(IdentifierType id) {

        final ResourceInstanceModel resourceModel = resourceInstanceDAO.findByIdentifier(id.getStringValue());

        if (resourceModel != null)
            // resourceModel.setNotMsgCount(notificationService.findMsgCountByEntityId(id.getStringValue()));
            return ResourceInstanceUtil.copyProperties(resourceModel,
                notificationService.findMsgCountByEntityId(id.getStringValue()));
        else
            return null;

    }

    /**
     * Gets the resource instance list.
     *
     * @param queryString
     *            the query string
     *
     * @return the resource instance list
     * @ssdd
     */
    @Override
    public ResourceInstanceListType getResourceInstanceList(String queryString) {

        final ResourceInstanceListType list = ResourceInstanceListType.Factory.newInstance();

        final List<ResourceInstanceModel> resourceInstanceList = resourceInstanceDAO.findAll();
        // if (resourceInstanceList != null) {
        // log.info("Found " + resourceInstanceList.size() + " resource instances");
        // } else {
        // log.info("No resource instances found");
        // }

        if (resourceInstanceList != null && resourceInstanceList.size() > 0) {
            final ResourceInstance[] resources = new ResourceInstance[resourceInstanceList.size()];
            int i = 0;
            for (final ResourceInstanceModel resourceInstance : resourceInstanceList) {
                // resourceInstance.setNotMsgCount(notificationService.findMsgCountByEntityId(resourceInstance.getIdentifier()));
                final ResourceInstance resource = ResourceInstanceUtil.copyProperties(
                    resourceInstance,
                    notificationService.findMsgCountByEntityId(resourceInstance.getIdentifier()));
                if (resource != null)
                    resources[i++] = resource;
            }
            list.setResourceInstanceArray(resources);
        }
        return list;
    }

    public ResourceProfileService getResourceProfileService() {

        return resourceProfileService;
    }

    private void init() {

        final List<ResourceInstanceModel> instances = resourceInstanceDAO.findAll();
        if (instances != null && instances.size() > 0) {
            // for (ResourceInstanceModel instnce : instances) {
            // ResourceProfile theProfile = ResourceProfileUtil.copyProperties(profile);
            //
            // log.debug("init: send [ " + profile.getId() + " ] to directory service");
            // }
        }
    }

    /**
     * Checks if is checked in.
     *
     * @param ris
     *            the resource instance
     *
     * @return true, if is checked in
     * @ssdd
     */
    @Override
    public boolean isCheckedIn(ResourceInstance ris) {

        // TODO add code to check if this resource has already checked in
        return false;
    }

    /**
     * Offer profile.
     *
     * @param ris
     *            the resource instance
     * @param resourceProfileID
     *            the resource profile id
     * @ssdd
     */
    @Override
    public void offerProfile(ResourceInstance ris, IdentifierType resourceProfileID) {

        // TODO send a notification to this resource that it has been offered a resource profile

    }

    /**
     * Register a resource profile instance. Applications must have a Resource Profile created for
     * them by an administrator and must register with the core to begin to receive notifications.
     * The important part about an application registering with the core is that a notification
     * endpoint created with its profile's interests is created.
     *
     * @param id
     *            the id
     * @param localResourceID
     *            the local resource id
     * @param resourceProfileID
     *            the resource profile id
     *
     * @return the resource instance model
     *
     * @throws ResourceProfileDoesNotExist
     *             the resource profile does not exist
     * @ssdd
     */
    @Override
    public ResourceInstanceModel register(IdentifierType id,
                                          IdentifierType localResourceID,
                                          IdentifierType resourceProfileID)
        throws ResourceProfileDoesNotExist {

        // Applications must have a Resource Profile created for them by an administrator and must
        // register with the core to begin to receive notifications. The following diagrams shows
        // how an application registers with the core to enable it to start receiving notifications.
        // This diagram represents an application that is designed to monitor the core and not
        // necessarily represent any particular user. All Resource Profiles can only be created and
        // updated through the administrative interface. Applications will not be able to create or
        // update a profile.
        //
        // The important part about an application registering with the core is that
        // a notification endpoint created with its profile's interests is created

        // check if a resource instance already exists
        ResourceInstanceModel resourceModel = resourceInstanceDAO.findByIdentifier(id.getStringValue());
        if (resourceModel == null) {
            resourceModel = createResourceInstance(id);
            if (localResourceID != null && localResourceID.getStringValue() != null)
                resourceModel.setLocalResourceID(localResourceID.getStringValue());
        }

        // Apply the profile based on the requested resourceProfileID
        if (applyProfile(resourceModel, resourceProfileID))
            // Persist the resource instance
            resourceModel = resourceInstanceDAO.makePersistent(resourceModel);

        // Return ResourceInstance
        return resourceModel;
    }

    public void setConfigurationService(ConfigurationService configurationService) {

        this.configurationService = configurationService;
    }

    @Override
    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    public void setNotificationService(NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    public void setResourceInstanceDAO(ResourceInstanceDAO p) {

        resourceInstanceDAO = p;
    }

    public void setResourceProfileService(ResourceProfileService resourceProfileService) {

        this.resourceProfileService = resourceProfileService;
    }

    @Override
    public void systemInitializedHandler(String messgae) {

        logger.debug("systemInitializedHandler: ... start ...");
        final WorkProductTypeListType publishedProducts = WorkProductTypeListType.Factory.newInstance();
        final WorkProductTypeListType subscribedProducts = WorkProductTypeListType.Factory.newInstance();
        directoryService.registerUICDSService(NS_ResourceInstanceService,
            RESOURCEINSTANCE_SERVICE_NAME, publishedProducts, subscribedProducts);
        init();
        logger.debug("systemInitializedHandler: ... done ...");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.leidos.xchangecore.core.infrastructure.service.ResourceInstanceService#unregister(com
     * .saic.precis.x2009.x06.base.IdentifierType)
     */
    @Override
    public IdentifierType unregister(IdentifierType id) throws ResourceInstanceDoesNotExist {

        final ResourceInstanceModel resourceModel = resourceInstanceDAO.findByIdentifier(id.getStringValue());
        if (resourceModel != null) {
            notificationService.destroyPullPoint(resourceModel.getResourceID());
            resourceInstanceDAO.makeTransient(resourceModel);
            return id;
        } else
            throw new ResourceInstanceDoesNotExist(id.getStringValue() +
                                                   " resource instance does not exist");
    }

    /**
     * call notificationService to update its endpoint
     *
     * @param id
     *            the id
     * @param endpoint
     *            the endpoint
     * @param isWebService
     *            the is web service
     *
     * @return true, if successful
     * @ssdd
     */
    @Override
    public boolean updateEndpoint(IdentifierType id, String endpoint, boolean isWebService) {

        boolean updated = false;

        final ResourceInstanceModel resourceModel = resourceInstanceDAO.findByIdentifier(id.getStringValue());

        if (resourceModel != null) {

            // call notificationService to update its endpoint
            notificationService.updateEndpoint(id.getStringValue(), endpoint, isWebService);

            // Set the endpoint in the model
            resourceModel.getEndpoints().clear();
            resourceModel.getEndpoints().add(endpoint);

            updated = true;

            // do we need to persist profile?
        }
        return updated;
    }

}
