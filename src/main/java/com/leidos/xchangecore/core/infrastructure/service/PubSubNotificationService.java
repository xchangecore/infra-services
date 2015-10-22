package com.leidos.xchangecore.core.infrastructure.service;

import org.springframework.transaction.annotation.Transactional;

import com.leidos.xchangecore.core.infrastructure.messages.ProductChangeNotificationMessage;

/**
 * The PubSubNotificationService interface provides the ability for XchangeCore services to be notified
 * about work products being published.
 *
 * @author Aruna Hau
 * @since 1.0
 * @ssdd
 *
 */
@Transactional
public interface PubSubNotificationService {

    /**
     * Provides the subscriber name, for example, the XchangeCore service name.
     *
     * @return service name
     * @ssdd
     */
    public String getServiceName();

    /**
     * Notifies a subscriber that an XPath provided with a product type subscription, identified by
     * the subscription ID, is badly formed and failed to execute. Note that this notification will
     * not be received until there is a publication of at least one work product of the subscribed
     * type. The subscription is removed as a result.
     *
     * @param subscriptionId
     * @ssdd
     */
    public void InvalidXpathNotification(Integer subscriptionId, String errorMessage);

    /**
     * Notifies the subscriber that a new version of a work product, identified by the product ID,
     * is available and may be retrieved from the Work Product service.
     *
     * @param workProductID
     * @param subscriptionId
     * @ssdd
     */
    public void newWorkProductVersion(String productID, Integer subscriptionId);

    /**
     * Notifies the subscriber that a work product, identified by the product ID, has been deleted
     * and is no longer avaible from the Work Product service.
     *
     * @param workProductID
     * @param workProductType
     * @param subscriptionId
     * @ssdd
     */
    public void workProductDeleted(ProductChangeNotificationMessage changedMessage,
                                   Integer subscriptionId);
}
