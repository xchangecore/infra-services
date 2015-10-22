package com.leidos.xchangecore.core.infrastructure.service;

import java.util.Map;

import org.oasisOpen.docs.wsn.b2.NotificationMessageDocument;

import com.leidos.xchangecore.core.infrastructure.exceptions.EmptySubscriberNameException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductIDException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductTypeException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NullSubscriberException;
import com.saic.precis.x2009.x06.base.IdentificationType;

/**
 * Provides WS-Notification v1.3 services for XchangeCore services.
 * 
 * @author Nathan Lewnes
 * @ssdd
 */
// @Transactional
public interface NotificationService
    extends PullPoint, NotificationProducer, NotificationConsumer, PubSubNotificationService {

    public static final String NOTIFICATION_SERVICE_NAME = "NotificationService";

    // FLi modified on 11/29/2011
    public int findMsgCountByEntityId(String entityId);

    /**
     * Gets the ConfigurationService dependency
     * 
     * @return ConfigurationService
     * @see ConfigurationService
     * @ssdd
     */
    public ConfigurationService getConfigurationService();

    /**
     * Returns all the work product identifiers of work products of interest to entityId
     * 
     * @param entityId - the entity Id
     * @return arrayOfIdentificationType - an array of work product identifications
     * @ssdd
     */
    public IdentificationType[] getMatchingMessages(String entityId);

    /**
     * Gets the PubSubService dependency
     * 
     * @return PubSubService
     * @see PubSubService
     * @ssdd
     */
    public PubSubService getPubSubService();

    /**
     * Sets the ConfigurationService dependency
     * 
     * @return void
     * @see ConfigurationService
     * @ssdd
     */
    public void setConfigurationService(ConfigurationService service);

    /**
     * Sets the PubSubService dependency
     * 
     * @return void
     * @see PubSubService
     * @ssdd
     */
    public void setPubSubService(PubSubService service);

    /**
     * Register a subscription for a specific agreementID from a specific entityID
     * 
     * @param agreementID specific agreementID to subscribe for notifications
     * @param entityID entity to retrieve notifications from (user who wants to receive updates)
     * @ssdd
     */
    public void subscribeAgreement(String agreementID, String entityID)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException;

    /**
     * Register a subscription for a specific profileRefID from a specific entityID
     * 
     * @param profileID specific profile entityID to subscribe for notifications
     * @param entityID entity to retrieve notifications from (user who wants to receive updates)
     * @ssdd
     */
    public void subscribeProfile(String profileID, String entityID)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException;

    /**
     * Register a subscription for a specific work product from a specific entity
     * 
     * @param workProductID specific work productID to subscribe for notifications
     * @param entityID entity to retrieve notifications from
     * @return Integer subscriptionID
     * @see NotificationMessageDocument
     * @ssdd
     */
    public Integer subscribeWorkProductID(String workProductID, String entityID)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException;

    /**
     * Register a subscription for a specific work product from a specific entity
     * 
     * @param workProductID specific work product to subscribe for notifications
     * @param entityID entity to retrieve notifications from
     * @return Integer subscriptionID
     * @see NotificationMessageDocument
     * @ssdd
     */
    public Integer subscribeWorkProductType(String wpType,
                                            String xpContext,
                                            Map<String, String> namespaceMap,
                                            String entityID) throws InvalidProductTypeException,
        NullSubscriberException, EmptySubscriberNameException;

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
     * Update the endpoint address for a paticular entityID. Could either be a pullpoint address
     * where the user manually looks for messages to pull from or it could be a web service url
     * where messages will automatically be sent to the specided ws url.
     * 
     * @param entityID entityID to apply this new endpoint to
     * @param endpointAddress address of the endpoint desired
     * @param isWebService tells the notification service whether the endpoint is a WS.
     * @ssdd
     */
    public void updateEndpoint(String entityID, String endpointAddress, boolean isWebService);

}
