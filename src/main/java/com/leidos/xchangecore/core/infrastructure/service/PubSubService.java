package com.leidos.xchangecore.core.infrastructure.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.leidos.xchangecore.core.infrastructure.exceptions.EmptySubscriberNameException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductIDException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductTypeException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NullSubscriberException;
import com.leidos.xchangecore.core.infrastructure.listener.NotificationListener;
import com.leidos.xchangecore.core.infrastructure.messages.AgreementRosterMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductChangeNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProfileNotificationMessage;

/**
 * The PubSubService interface provides the ability for XchangeCore services to subscribe work products.
 * Two types of subscription are provided:
 *
 * <ul>
 * <li>Subscribe by product ID - subscribers will be notified when the work product with the given
 * ID is published.
 * <li>Subscribe by product type - subscribers will be notified when a work product of the given
 * type is published.
 * </ul>
 * <p>
 * When subscribing by product type, a XPath query string (optional) may be provided to narrow the
 * subscription down to a set of work products which meet the criteria specified by the query
 * string, e.g. alerts that are weather related, etc.
 *
 * This interface is implemented by the PubSub Service.
 *
 * @author Aruna Hau
 * @since 1.0
 * @ssdd
 *
 */
@Transactional
public interface PubSubService {

    /**
     * Subscribe to an agreement with the specified ID.
     * <p>
     * The subscriber is expected to provide a unique subscriber name, i.e. the service name, via
     * the PubSubNotification interface's getServiceName() operation.
     * <p>
     * This operation generates and, if successful, returns a subscription ID.
     *
     * @param ID of agreement
     * @param subscriber
     * @throws InvalidProductIDException
     * @throws NullSubscriberException
     * @throws EmptySubscriberNameException
     * @ssdd
     */
    public void addAgreementListener(Integer agreementID,
                                     NotificationListener<AgreementRosterMessage> listener)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException;

    /**
     * Subscribe to a profile with the specified ID.
     * <p>
     * The subscriber is expected to provide a unique subscriber name, i.e. the service name, via
     * the PubSubNotification interface's getServiceName() operation.
     * <p>
     * This operation generates and, if successful, returns a subscription ID.
     *
     * @param ID of profile
     * @param subscriber
     * @throws InvalidProductIDException
     * @throws NullSubscriberException
     * @throws EmptySubscriberNameException
     * @ssdd
     */
    public void addProfileListener(String profileID,
                                   NotificationListener<ProfileNotificationMessage> listener)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException;

    /**
     * Handles agreement roster change notifications.
     *
     * This operation is wired by Spring to listen to the agreementNotificationHandler to get
     * notifications of changes to agreements from the AgreementService
     *
     * @param message
     * @see AgreementRosterMessage
     * @ssdd
     */
    public void agreementNotificationHandler(AgreementRosterMessage message);

    /**
     * Announces the deletion of a new work product version
     * <p>
     * As a result, all subscribers of this work product, either by product ID or product type, are
     * notified.
     *
     * @param workProductID
     * @param workProductType
     * @ssdd
     */
    public void deleteWorkProduct(ProductChangeNotificationMessage changedMessage);

    /**
     * Gets the last published message on topicName
     *
     * @param topicName name of topic to get message from
     * @return message
     * @ssdd
     */
    public String getLastPublishedMessage(String topicName);

    /**
     * Get a list of subscription ids for the given service name.
     *
     * @return subscription ids
     */
    public List<Integer> getSubscriptionsByServiceName(String serviceName);

    /**
     * Handles work product change notifications.
     *
     * This operation is wired by Spring to listen to the productChangeNotificationChannel to get
     * notifications of changes to work products from the WorkProductService
     *
     * @param message
     * @see ProductChangeNotificationMessage
     * @ssdd
     */
    public void productChangeNotificationHandler(ProductChangeNotificationMessage message);

    /**
     * Handles profile change notifications.
     *
     * This operation is wired by Spring to listen to the profileNotificationHandler to get
     * notifications of changes to profiles from the ProfileService
     *
     * @param message
     * @see ProfileNotificationMessage
     * @ssdd
     */
    public void profileNotificationHandler(ProfileNotificationMessage message);

    /**
     * Announces a publication of a new work product version, as a result of product creation or
     * product update.
     * <p>
     * As a result, all subscribers of this work product, either by product ID or product type, are
     * notified.
     * <p>
     * As this is merely a notification and no actual work product is included, the subscribers are
     * responsible for obtaining the work product directly from the Work Product service.
     *
     * @param workProductID
     * @param workProductType
     * @ssdd
     */
    public void publishWorkProduct(ProductChangeNotificationMessage message);

    /**
     * Subscribe to work products of the specified product type for the specified interest group.
     * <p>
     * The subscriber is expected to provide a unique subscriber name, generally, the service name,
     * via the PubSubNotification interface's getServiceName() operation.
     * <p>
     * This operation generates and, if successful, returns a subscription ID.
     *
     * @param workProductType
     * @param interestGroupID
     * @param xPath
     * @param namespaceMap
     * @param subscriber
     * @return integer value
     * @throws InvalidProductTypeException
     * @throws NullSubscriberException
     * @throws EmptySubscriberNameException
     * @ssdd
     */
    public Integer subscribeInterestGroupIdAndWorkProductType(String workProductType,
                                                              String interestGroupID,
                                                              String xPath,
                                                              Map<String, String> namespaceMap,
                                                              PubSubNotificationService subscriber)
        throws NullSubscriberException, EmptySubscriberNameException;

    /**
     * Sends its subscriber interface upon restart
     *
     * @param subscriber
     * @see PubSubNotificationService
     * @ssdd
     */
    public void subscriberInterface(PubSubNotificationService subscriber);

    /**
     * Subscribe to a work product with the specified product ID.
     * <p>
     * The subscriber is expected to provide a unique subscriber name, i.e. the service name, via
     * the PubSubNotification interface's getServiceName() operation.
     * <p>
     * This operation generates and, if successful, returns a subscription ID.
     *
     * @param workProductID
     * @param subscriber
     * @return integer value
     * @throws InvalidProductIDException
     * @throws NullSubscriberException
     * @throws EmptySubscriberNameException
     * @ssdd
     */
    public Integer subscribeWorkProductID(String workProductID, PubSubNotificationService subscriber)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException;

    /**
     * Subscribe to new versions of a work product with the specified product ID.
     * <p>
     * The subscriber is expected to provide a unique subscriber name, i.e. the service name, via
     * the PubSubNotification interface's getServiceName() operation.
     * <p>
     * This operation generates and, if successful, returns a subscription ID.
     *
     * @param workProductID
     * @param subscriber
     * @return integer value
     * @throws InvalidProductIDException
     * @throws NullSubscriberException
     * @throws EmptySubscriberNameException
     * @ssdd
     */
    public Integer subscribeWorkProductIDNewVersions(String workProductID,
                                                     PubSubNotificationService subscriber)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException;

    /**
     * Subscribe to work products of the specified product type.
     * <p>
     * The subscriber is expected to provide a unique subscriber name, generally, the service name,
     * via the PubSubNotification interface's getServiceName() operation.
     * <p>
     * This operation generates and, if successful, returns a subscription ID.
     *
     * @param workProductType
     * @param xPath
     * @param namespaceMap
     * @param subscriber
     * @return integer value
     * @throws InvalidProductTypeException
     * @throws NullSubscriberException
     * @throws EmptySubscriberNameException
     * @ssdd
     */
    public Integer subscribeWorkProductType(String workProductType,
                                            String xPath,
                                            Map<String, String> namespaceMap,
                                            PubSubNotificationService subscriber)
        throws NullSubscriberException, EmptySubscriberNameException;

    /**
     * Unregister a previous subscription, identified by the specified subscription ID.
     *
     * @param subscriptionID
     * @ssdd
     */
    public void unsubscribeBySubscriptionID(Integer subscriptionID);

}
