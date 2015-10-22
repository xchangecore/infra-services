package com.leidos.xchangecore.core.infrastructure.service;

import com.leidos.xchangecore.core.infrastructure.exceptions.LocalCoreNotOnlineException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnavailableException;
import com.leidos.xchangecore.core.infrastructure.messages.DeleteJoinedInterestGroupMessage;
import com.leidos.xchangecore.core.infrastructure.messages.DeleteJoinedProductMessage;
import com.leidos.xchangecore.core.infrastructure.messages.InterestGroupStateNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.JoinedInterestGroupNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.JoinedPublishProductRequestMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductPublicationStatusNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductToInterestGroupAssociationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.PublishProductMessage;

/**
 * The CommunicationsInterestGroupSharingService interface provides the ability for XchangeCore services
 * to share an interest group with other cores. This interface should be implemented by XchangeCore
 * services that manage interest groups.
 *
 * @author Aruna Hau
 * @since 1.0
 * @see com.leidos.xchangecore.core.infrastructure.service.impl.CommunicationsService
 * @see com.leidos.xchangecore.core.infrastructure.service.impl.CommunicationsInterestGroupSharingService
 * @see com.leidos.xchangecore.core.infrastructure.service.impl.PubSubNotificationService
 * @ssdd
 */
public interface CommunicationsInterestGroupSharingService {

    /**
     * Handles the notification of the deletion of a joined interest group. This notification is
     * received by and handled in the joined core. The interest group may have been deleted in the
     * owning core while the joined core was down.
     *
     * @param message
     * @see DeleteJoinedInterestGroupMessage
     * @ssdd
     */
    public void deleteJoinedInterestGroupNotificationHandler(DeleteJoinedInterestGroupMessage message);

    /**
     * Handles the notification of the deletion of a joined work product. This notification is
     * received by and handled in the joined core. The interest group may have been deleted in the
     * owning core while the joined core was down.
     *
     * @param message
     * @see DeleteJoinedProductMessage
     * @ssdd
     */
    public void deleteJoinedProductNotificationHandler(DeleteJoinedProductMessage message);

    /**
     * Handles the interest group state change notifications. These notifications are received when
     * an interest group is
     * <ul>
     * <li>created (in both core that owns and core that is joined to the interest group)
     * <li>shared (in core that owns the interest group)
     * <li>joined (in core that is joined to the interest group)
     * </ul>
     * <p>
     *
     * @param message
     * @throws LocalCoreNotOnlineException
     * @throws RemoteCoreUnavailableException
     * @see InterestGroupStateNotificationMessage
     * @ssdd
     */

    public void handleInterestGroupState(InterestGroupStateNotificationMessage message);

    /**
     * Handles the notification of a joined interest group. This notification is received by and
     * handled in the joined core.
     *
     * @param message
     * @see JoinedInterestGroupNotificationMessage
     * @ssdd
     */
    public void joinedInterestGroupNotificationHandler(JoinedInterestGroupNotificationMessage message);

    /**
     * Handles the notification of a joined core's request to publish/uodate a work product
     * associated with a shared interest group..
     *
     * @param message
     * @see JoinedPublishProductRequestMessage
     * @ssdd
     */
    public void joinedPublishProductNotificationHandler(JoinedPublishProductRequestMessage message);

    /**
     * Handles the product notifications. The product notifications are received by a core that is
     * joined to a shared interest group when a new work product has been added to the shared
     * interest group or an existing work product associated with the shared interest group has been
     * updated.
     *
     * @param message
     * @see PublishProductMessage
     * @ssdd
     */
    public void owningCoreWorkProductNotificationHandler(PublishProductMessage msg);

    /**
     * Handles the product to interest group association notifications. The product to interest
     * group associations are received when a work product is associated with an interest group when
     * the product is first published. These notifications are received by both the core that owns
     * the interest group and by the core that is joined to a shared interest group.
     *
     * @param message
     * @see ProductToInterestGroupAssociationMessage
     * @ssdd
     */
    public void productAssociationHandler(ProductToInterestGroupAssociationMessage message);

    /**
     * Handles the notification of a status of a pending product publication request. This
     * notification is received by and handled in the owning core.
     *
     * @param message
     * @see ProductPublicationStatusNotificationMessage
     * @ssdd
     */
    public void productPublicationStatusNotificationHandler(ProductPublicationStatusNotificationMessage message);

}
