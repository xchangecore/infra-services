package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;
import org.uicds.agreementService.AgreementListType;
import org.uicds.agreementService.AgreementType;
import org.uicds.agreementService.AgreementType.ShareRules;
import org.uicds.coreConfig.CoreConfigType;
import org.uicds.coreConfig.CoreStatusType;
import org.uicds.workProductService.WorkProductPublicationResponseDocument;

import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductTypeException;
import com.leidos.xchangecore.core.infrastructure.exceptions.LocalCoreNotOnlineException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareRuleInAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnavailableException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnknownException;
import com.leidos.xchangecore.core.infrastructure.messages.Core2CoreMessage;
import com.leidos.xchangecore.core.infrastructure.messages.DeleteInterestGroupMessage;
import com.leidos.xchangecore.core.infrastructure.messages.DeleteJoinedInterestGroupMessage;
import com.leidos.xchangecore.core.infrastructure.messages.DeleteJoinedProductMessage;
import com.leidos.xchangecore.core.infrastructure.messages.InterestGroupStateNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.JoinedInterestGroupNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.JoinedPublishProductRequestMessage;
import com.leidos.xchangecore.core.infrastructure.messages.NewInterestGroupCreatedMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductChangeNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductPublicationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductPublicationStatusMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductPublicationStatusNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductToInterestGroupAssociationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.PublishProductMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ShareInterestGroupMessage;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.AgreementService;
import com.leidos.xchangecore.core.infrastructure.service.CommunicationsInterestGroupSharingService;
import com.leidos.xchangecore.core.infrastructure.service.CommunicationsService;
import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.service.InterestGroupManagementComponent;
import com.leidos.xchangecore.core.infrastructure.service.NotificationService;
import com.leidos.xchangecore.core.infrastructure.service.PubSubNotificationService;
import com.leidos.xchangecore.core.infrastructure.service.PubSubService;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.saic.dctd.uicds.schemas.core2CoreMessage.Core2CoreMessageDocument;
import com.saic.dctd.uicds.schemas.core2CoreMessage.Core2CoreMessageType;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;

/**
 * Sends messages between cores using a Spring Message Channel connected to an implementation that
 * does the actual send and receive. Received messages are also delivered to this class via a Spring
 * Message Channel. This allows the messaging infrastructure to be changed without affecting the
 * clients of the Communication Service interface and without affecting the implementation of this
 * class. The other end of the message channel that does the actual core to core messaging is
 * configured in the Spring application context files.<br/>
 * <br/>
 * The CommunicationsServiceImpl sends on the following Spring Message Channels:<br/>
 * newInterestGroupCreated<br/>
 * deleteInterestGroup<br/>
 * shareInterestGroup<br/>
 * publishProduct<br/>
 * publishProductRequestFromJoinedCore<br/>
 * broadcastMessageNotification<br/>
 * core2CoreMessage<br/>
 * <br/>
 * The CommunicationsServiceImpl receives on the following Spring Message Channels:<br/>
 * interestGroupStateNotification<br/>
 * productAssociation<br/>
 * productChangeNotification<br/>
 * joinedPublishProdcutRequest<br/>
 * core2CoreMessageNotification<br/>
 * joinedInterestGroupNotification<br/>
 * deleteJoinedInterestGroupNotification<br/>
 *
 * @ssdd
 */
public class CommunicationsServiceImpl
    implements CommunicationsService, CommunicationsInterestGroupSharingService,
    PubSubNotificationService {

    /** The logger. */
    Logger logger = LoggerFactory.getLogger(CommunicationsServiceImpl.class);

    /** The share interest group channel. */
    private MessageChannel shareInterestGroupChannel;

    /** The new interest group created channel. */
    private MessageChannel newInterestGroupCreatedChannel;

    /** The product publication channel. */
    private MessageChannel productPublicationChannel;

    /** The product publication status channel. */
    private MessageChannel productPublicationStatusChannel;

    /** The core2 core message channel. */
    private MessageChannel core2CoreMessageChannel;

    /** The resource message notification channel. */
    private MessageChannel resourceMessageNotificationChannel;

    /** The broadcast message notification channel. */
    private MessageChannel broadcastMessageNotificationChannel;

    /** The delete interest group channel. */
    private MessageChannel deleteInterestGroupChannel;

    /** The directory service. */
    private DirectoryService directoryService;

    /** The pub sub service. */
    private PubSubService pubSubService;

    /** The work product service. */
    private WorkProductService workProductService;

    /** The interest group management component. */
    private InterestGroupManagementComponent interestGroupManagementComponent;

    /** The configuration service. */
    private ConfigurationService configurationService;

    /** The notification service. */
    private NotificationService notificationService;

    /** The agreement service. */
    private AgreementService agreementService;

    /*
     * private HashMap<String, MessageChannel> publishWPMessageChannels;
     *
     * public HashMap<String, MessageChannel> getPublishWPMessageChannels() { return
     * publishWPMessageChannels; }
     *
     * public void setPublishWPMessageChannels(HashMap<String, MessageChannel>
     * publishWPMessageChannels) { this.publishWPMessageChannels = publishWPMessageChannels; }
     */

    // Key: published product type
    // Value: ServiceName
    /** The service product type map. */
    private final HashMap<String, String> serviceProductTypeMap = new HashMap<String, String>();

    // Key: interestGroupID
    /** The interest group list. */
    private final List<String> interestGroupList = new ArrayList<String>();

    /**
     * Core to core message notification handler. Receives messages from other cores via a Spring
     * message channel.
     *
     * @param message
     *            the message
     * @ssdd
     */
    @Override
    public void core2CoreMessageNotificationHandler(Core2CoreMessage message) {

        logger.debug("core2CoreMessageNotificationHandler: received messageType=" +
                     message.getMessageType() + "] from " + message.getFromCore()); // + " message=[" +
        // message.getMessage()

        if (!message.getToCore().equals(configurationService.getCoreName())) {
            logger.error("core2CoreMessageNotificationHandler - received message intended for another core - core=" +
                         message.getToCore());
        } else {
            try {
                Core2CoreMessageDocument doc = Core2CoreMessageDocument.Factory.parse(message.getMessage());
                Core2CoreMessageType msg = Core2CoreMessageType.Factory.parse(doc.getCore2CoreMessage().toString());
                logger.debug("===> core2CoreMessageNotificationHandler - msg=[" + msg.toString() +
                             "]");

                // set the message content to the element inside the document
                message.setMessage(msg.toString());
                Message<Core2CoreMessage> notification = new GenericMessage<Core2CoreMessage>(message);

                CORE2CORE_MESSAGE_TYPE msgType = CORE2CORE_MESSAGE_TYPE.valueOf(message.getMessageType());
                switch (msgType) {
                case RESOURCE_MESSAGE:
                    logger.debug("core2CoreMessageNotificationHandler: sending message to Resource Management Service ");
                    resourceMessageNotificationChannel.send(notification);
                    break;

                case BROADCAST_MESSAGE:
                    logger.debug("core2CoreMessageNotificationHandler: sending message to Broadcast Service");
                    broadcastMessageNotificationChannel.send(notification);
                    break;

                default:
                    logger.debug("core2CoreMessageNotificationHandler: Unknown message type - ignore it for now!");
                    break;

                }

            } catch (Throwable e) {
                logger.error("core2CoreMessageNotificationHandler: Unable to parse message into Core2CoreMessageDocument");
                e.printStackTrace();
                return;
            }

        }
    }

    /**
     * Delete joined interest group notification handler. This method handles the joined interest
     * group notification. The joined interest group notification is received by and handled in the
     * joined core.
     *
     * @param message
     *            the message
     * @ssdd
     */
    @Override
    public void deleteJoinedInterestGroupNotificationHandler(DeleteJoinedInterestGroupMessage message) {

        logger.debug("deleteJoinedInterestGroupNotificationHandler: received notification of deleted joined interest group id=" +
                     message.getInterestGroupID());

        // Message<JoinedInterestGroupNotificationMessage> notification = new
        // GenericMessage<JoinedInterestGroupNotificationMessage>(
        // message);
        // receivedJoinedInterestGroupChannel.send(notification);
        interestGroupManagementComponent.deleteJoinedInterestGroup(message);
    }

    /**
     * Delete joined product notification handler.
     *
     * @param message
     *            the message that contains the work product id
     * @ssdd
     */
    @Override
    public void deleteJoinedProductNotificationHandler(DeleteJoinedProductMessage message) {

        logger.debug("deleteJoinedProductNotificationHandler: received notification of deleted joined interest group id=" +
                     message.getProductID());
        workProductService.deleteWorkProductWithoutNotify(message.getProductID());
    }

    /**
     * Gets the agreement service.
     *
     * @return the agreement service
     */
    public AgreementService getAgreementService() {

        return agreementService;
    }

    /**
     * Gets the core jid from agreements.
     *
     * @param hostName
     *            the host name
     *
     * @return the core jid from agreements
     */
    private String getCoreJIDFromAgreements(String hostName) {

        String coreJID = null;

        AgreementListType agreementList = agreementService.getAgreementList();
        for (AgreementType agreement : agreementList.getAgreementArray()) {

            // If there's no active agreement with this core, we share all types
            if (agreement != null &&
                agreement.getPrincipals().getRemoteCore().getStringValue().contains(hostName)) {
                coreJID = agreement.getPrincipals().getRemoteCore().getStringValue();
                break;
            }
        }
        return coreJID;
    }

    /**
     * Gets the notification service.
     *
     * @return the notification service
     */
    public NotificationService getNotificationService() {

        return notificationService;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     * @ssdd
     */
    @Override
    public String getServiceName() {

        return COMMUNICATIONS_SERVICE_NAME;
    }

    /**
     * Gets the service name from map. This method returns the name of the UICDS service that
     * publishes a given work product type
     *
     * @param workProductType
     *            the work product type
     *
     * @return the service name from map
     *
     * @throws InvalidProductTypeException
     *             the invalid product type exception
     * @ssdd
     */
    public String getServiceNameFromMap(String workProductType) throws InvalidProductTypeException {

        logger.debug("====> workProductType=" + workProductType);
        String serviceName = serviceProductTypeMap.get(workProductType);
        if (serviceName == null) {
            logger.debug("Get servicename from directory service");
            serviceName = directoryService.getServiceNameByPublishedProductType(workProductType);
            serviceProductTypeMap.put(workProductType, serviceName);
        }
        logger.debug("=====> serviceName= " + serviceName);
        return serviceName;
    }

    /**
     * Sends notifications of interest group state changes to joined cores
     *
     * @param message
     *            the message
     * @ssdd
     */
    @Override
    public void handleInterestGroupState(InterestGroupStateNotificationMessage message) {

        logger.info("handleInterestGroupState: IGID: " + message.getInterestGroupID() + " state: " +
                    message.getState() + " sharingStatus: " + message.getSharingStatus() +
                    " IGIDType: " + message.getInterestGroupType());

        if (message.getState().equals(InterestGroupStateNotificationMessage.State.NEW) ||
            message.getState().equals(InterestGroupStateNotificationMessage.State.RESTORE)) {
            logger.debug("===> Receive Interest Group state change: interestGroupID=" +
                         message.getInterestGroupID() + " state:" + message.getState());

            if (!interestGroupList.contains(message.getInterestGroupID())) {
                interestGroupList.add(message.getInterestGroupID());
                NewInterestGroupCreatedMessage request = new NewInterestGroupCreatedMessage();
                request.setInterestGroupID(message.getInterestGroupID());
                request.setInterestGroupType(message.getInterestGroupType());
                request.setOwningCore(message.getOwningCore());
                if (message.getState().equals(InterestGroupStateNotificationMessage.State.RESTORE)) {
                    request.setRestored(true);
                    request.setOwnerProperties(message.getOwmnerProperties());
                    request.setJoinedWPTYpes(message.getJoinedWPTypes());
                } else {
                    if (message.getState().equals(InterestGroupStateNotificationMessage.State.SHARE)) {
                        request.setSharedCoreList(message.getSharedCoreList());
                    }
                    request.setRestored(false);
                }
                request.setSharingStatus(message.getSharingStatus());
                Message<NewInterestGroupCreatedMessage> requestMessage = new GenericMessage<NewInterestGroupCreatedMessage>(request);
                newInterestGroupCreatedChannel.send(requestMessage);
            }

        } else if (message.getState().equals(InterestGroupStateNotificationMessage.State.JOIN)) {
            // no action required for join state, which indicates that an interest group has joined
            // should only be received by the joining core.
        } else if (message.getState().equals(InterestGroupStateNotificationMessage.State.SHARE)) {

            for (String targetCore : message.getSharedCoreList()) {
                // although, we only support sharing with one core per call, loop through all anyway
                // to provide
                // flexibility in the future should we decide to change the interface to allow
                // multiple shares

                logger.info("===> Receive Interest group state change: interestGroupID=" +
                            message.getInterestGroupID() + " coreToShareWith=" + targetCore +
                            " state SHARE");

                if (targetCore.equals(configurationService.getCoreName())) {
                    logger.debug("handleInterestGroupState: request to share with self is ignored.");
                } else {

                    ShareInterestGroupMessage notification = new ShareInterestGroupMessage();
                    notification.setInterestGroupID(message.getInterestGroupID());
                    notification.setRemoteCore(targetCore);
                    notification.setInterestGroupInfo(message.getInterestGroupInfo());
                    notification.setWorkProductTypesToShare(message.getWorkProductTypesToShare());
                    Message<ShareInterestGroupMessage> msg = new GenericMessage<ShareInterestGroupMessage>(notification);

                    try {
                        shareInterestGroupChannel.send(msg);
                    } catch (IllegalArgumentException e) {
                        logger.error("unowned incident " + e.getMessage());
                    } catch (IllegalStateException e) {
                        logger.error("core is offline: " + e.getMessage());
                    }
                }
            }

        } else if (message.getState().equals(InterestGroupStateNotificationMessage.State.UPDATE)) {
            logger.info("===> Receive Interest group state change: interestGroupID=" +
                        message.getInterestGroupID() + " state UPDATE");

            // No need to do anything here. Updated interest group info will be handled by work
            // product update
        } else if (message.getState().equals(InterestGroupStateNotificationMessage.State.DELETE)) {
            if (interestGroupList.contains(message.getInterestGroupID())) {
                DeleteInterestGroupMessage request = new DeleteInterestGroupMessage();
                request.setInterestGroupID(message.getInterestGroupID());
                Message<DeleteInterestGroupMessage> requestMessage = new GenericMessage<DeleteInterestGroupMessage>(request);
                deleteInterestGroupChannel.send(requestMessage);
                interestGroupList.remove(message.getInterestGroupID());
            }

        } else if (message.getState().equals(InterestGroupStateNotificationMessage.State.RESIGN)) {

        } else {
            // should not happen. throw an exception (TBD)
        }

    }

    // returns true if there is an agreement between this core and the targetCore
    // else throws an exception.
    /**
     * Checks for share agreement.
     *
     * @param targetCore
     *            the target core
     *
     * @return true, if successful
     *
     * @throws NoShareAgreementException
     *             the no share agreement exception
     * @throws NoShareRuleInAgreementException
     *             the no share rule in agreement exception
     */
    private boolean hasShareAgreement(String targetCore) throws NoShareAgreementException,
        NoShareRuleInAgreementException {

        logger.debug("getShareAgreement - targetCore=" + targetCore);

        // TODO: ticket #248
        // since agreement is stored by coreJID and share incident is by host name (for now)
        // we cannot get the agreement by coreJID. For now, we have to get all the agreements and
        // loop through to get the right one.
        // This can go back to just getting agreement by coreJID when ticket #248 is implemented
        boolean agreementFound = false;
        boolean shareRulesEnabled = false;

        AgreementListType agreementList = agreementService.getAgreementList();
        for (AgreementType agreement : agreementList.getAgreementArray()) {

            // If there's no active agreement with this core, we share all types
            if (agreement != null &&
                agreement.getPrincipals().getRemoteCore().getStringValue().contains(targetCore)) {
                agreementFound = true;
                ShareRules shareRules = agreement.getShareRules();
                if (shareRules != null) {
                    if (shareRules.getEnabled()) {
                        shareRulesEnabled = true;
                    } // end if shareRule enabled
                } // end if shareRules not null
            } // end of agreement not null
        }

        if (!agreementFound) {
            // no agreement between the core and the target core
            throw new NoShareAgreementException(configurationService.getFullyQualifiedHostName(),
                                                targetCore);
        } else if (!shareRulesEnabled) {
            throw new NoShareRuleInAgreementException(configurationService.getFullyQualifiedHostName(),
                                                      targetCore,
                                                      null,
                                                      null);
        }

        return true;
    }

    /**
     * Invalid xpath notification.
     *
     * @param subscriptionId
     *            the subscription id
     * @param errorMessage
     *            the error message
     * @ssdd
     */
    @Override
    public void InvalidXpathNotification(Integer subscriptionId, String errorMessage) {

        logger.error(errorMessage + " for Subscriber: " + subscriptionId);
    }

    /**
     * Joined interest group notification handler. This method handles the joined interest group
     * notification. The joined interest group notification is received by and handled in the joined
     * core.
     *
     * @param message
     *            the message
     * @ssdd
     */
    @Override
    public void joinedInterestGroupNotificationHandler(JoinedInterestGroupNotificationMessage message) {

        logger.debug("joinedInterestGroupNotificationHandler: received notification of joined interest group id=" +
                     message.interestGroupID);

        // Message<JoinedInterestGroupNotificationMessage> notification = new
        // GenericMessage<JoinedInterestGroupNotificationMessage>(
        // message);
        // receivedJoinedInterestGroupChannel.send(notification);
        interestGroupManagementComponent.receivedJoinedInterestGroup(message);
    }

    /**
     * Sends the product publication status to the joined core that made the request.
     *
     * @param message
     *            the message
     * @ssdd
     */
    @Override
    public void joinedPublishProductNotificationHandler(JoinedPublishProductRequestMessage message) {

        String product = message.getWorkProduct();
        String requestingCore = message.getRequestingCore();
        String act = message.getAct();
        String userID = message.getUserID();

        logger.debug("joinedPublishProductNotificationHandler: receive product publication from " +
                     requestingCore + " act=" + act);

        // This message should only be received from another core
        if (!requestingCore.equals(configurationService.getCoreName())) {
            // TODO: need to parse back into some otheer XML object that we convert into WorkProduct
            // model
            WorkProductDocument doc = null;
            logger.debug("joinedPublishProductNotificationHandler: receive a publish request from another core - wp=[" +
                         product + "]");
            try {
                doc = WorkProductDocument.Factory.parse(product);
            } catch (Exception exception) {
                logger.error("joinedPublishProductNotificationHandler: error parsing XML WP");
                exception.printStackTrace();
            }

            ProductPublicationStatus status = workProductService.publishProducRequesttFromJoinedCore(WorkProductHelper.toModel(doc.getWorkProduct()),
                userID);

            // set the access control token in the return status to send back to caller
            status.setAct(act);

            // TODO: send publication status back to the requesting user

            WorkProductPublicationResponseDocument statusDoc = WorkProductPublicationResponseDocument.Factory.newInstance();
            statusDoc.addNewWorkProductPublicationResponse().set(WorkProductHelper.toWorkProductPublicationResponse(status));

            ProductPublicationStatusMessage notification = new ProductPublicationStatusMessage();
            notification.setUserID(userID);
            notification.setRequestingCore(requestingCore);
            notification.setStatus(statusDoc.toString());
            Message<ProductPublicationStatusMessage> msg = new GenericMessage<ProductPublicationStatusMessage>(notification);

            logger.debug("joinedPublishProductNotificationHandler: sending status back to requesting core.  status=[" +
                         notification.getStatus() + "]");
            productPublicationStatusChannel.send(msg);
        }
    }

    /**
     * Sends out a notification of a new work product version.
     *
     * @param workProductID
     *            the work product id
     * @param subscriptionId
     *            the subscription id
     * @ssdd
     */
    @Override
    public void newWorkProductVersion(String workProductID, Integer subscriptionId) {

        logger.debug("newWorkProductVersion: workProductID: " + workProductID +
                     ", subscriptionId:" + subscriptionId);

        WorkProduct wp = workProductService.getProduct(workProductID);
        if (wp != null) {
            // logger.debug("===> wp string:[" + wp.toString() + "]");

            String interestGroupID = wp.getFirstAssociatedInterestGroupID();
            String wpID = wp.getProductID();
            String wpType = wp.getProductType();

            WorkProductDocument doc = WorkProductHelper.toWorkProductDocument(wp);

            String wpString = null;
            wpString = doc.xmlText();

            // send work product publication to CommunicationServiceXmpp
            logger.debug("newWorkProductVersion: sending ProductPublicationMessage IGID: " +
                         interestGroupID + ", productID: " + wpID + ", status: " +
                         ProductPublicationMessage.PublicationType.Publish);
            ProductPublicationMessage notification = new ProductPublicationMessage(ProductPublicationMessage.PublicationType.Publish,
                                                                                   interestGroupID,
                                                                                   wpID,
                                                                                   wpType,
                                                                                   wpString);
            Message<ProductPublicationMessage> msg = new GenericMessage<ProductPublicationMessage>(notification);
            productPublicationChannel.send(msg);
        } else {
            logger.error("newWorkProductVersion: productID: " + workProductID + " Not Found");
        }

    }

    /**
     * Owning core work product notification handler. This method handles product publications
     * received from the XMPP Communications Service as a result of a shared interest group. This
     * method is handled in the joining core.
     *
     * @param msg
     *            the msg
     * @ssdd
     */
    @Override
    public void owningCoreWorkProductNotificationHandler(PublishProductMessage msg) {

        String product = msg.getWorkProduct();
        String owningCore = msg.getOwningCore();

        logger.info("owningCoreWorkProductNotificationHandler: receive product publication from " +
                    owningCore + "'s XMPP nodes.");

        // This message should only be received for product owned by another core
        if (!owningCore.equals(configurationService.getCoreName())) {
            WorkProductDocument doc = null;

            logger.debug("owningCoreWorkProductNotificationHandler: receive product from another core");

            try {
                doc = WorkProductDocument.Factory.parse(product);
            } catch (Exception exception) {
                logger.error("owningCoreWorkProductNotificationHandler: error parsing XML WP");
                exception.printStackTrace();
            }

            try {
                workProductService.publishProductFromOwner(WorkProductHelper.toModel(doc.getWorkProduct()));

            } catch (Throwable e) {
                e.printStackTrace();
                logger.error("owningCoreWorkProductNotificationHandler - error publishing the received product");
            }
        }
    }

    /**
     * Product association handler associates a work product with an interest group.
     *
     * @param message
     *            the message
     * @ssdd
     */
    @Override
    public void productAssociationHandler(ProductToInterestGroupAssociationMessage message) {

        ProductToInterestGroupAssociationMessage.AssociationType associationType = message.getAssociationType();
        String productID = message.getProductId();
        String productType = message.getProductType();
        String interestGroupID = message.getInterestGroupId();
        String owningCore = message.getOwningCore();

        logger.debug("===========> *** productAssociationHandler -  productID=" + productID +
                     " productType=" + productType + " interestGroupID=" + interestGroupID +
                     " owningCore=" + owningCore + " associationType=" + associationType.toString());

        if (associationType == ProductToInterestGroupAssociationMessage.AssociationType.Associate) {
            if (!interestGroupList.contains(interestGroupID)) {
                interestGroupList.add(interestGroupID);

                // Notify the CommunicationsServiceXmpp component if this is a new interest group
                logger.debug("NotifyCommunicationsServiceXmppImpl of new  interest group : interestGroupID:" +
                             interestGroupID);

                NewInterestGroupCreatedMessage request = new NewInterestGroupCreatedMessage();
                request.setInterestGroupID(interestGroupID);
                request.setOwningCore(owningCore);
                request.setRestored(false);
                request.setSharingStatus(InterestGroupStateNotificationMessage.SharingStatus.None.toString());
                Message<NewInterestGroupCreatedMessage> requestMessage = new GenericMessage<NewInterestGroupCreatedMessage>(request);
                newInterestGroupCreatedChannel.send(requestMessage);

            }

            // subscribe to the associated work product ID

            try {
                pubSubService.subscribeWorkProductID(productID, this);
            } catch (Exception e) {
                logger.error("productAssociationHandler - error subscribing to productID:" +
                             productID);
                e.printStackTrace();
            }
        } else if (associationType == ProductToInterestGroupAssociationMessage.AssociationType.Unassociate) {
            if (interestGroupList.contains(interestGroupID)) {

                logger.debug("productAssociationHandler - sending ProductPublicationMessage with interest groupID=" +
                             interestGroupID +
                             " wpID=" +
                             productID +
                             " pubStatus" +
                             ProductPublicationMessage.PublicationType.Delete);
                ProductPublicationMessage notification = new ProductPublicationMessage(ProductPublicationMessage.PublicationType.Delete,
                                                                                       interestGroupID,
                                                                                       productID,
                                                                                       productType,
                                                                                       null);
                Message<ProductPublicationMessage> msg = new GenericMessage<ProductPublicationMessage>(notification);
                productPublicationChannel.send(msg);
            }
        }

    }

    /**
     * Product publication status notification handler receives and processes a publication status
     * notification and notifies the requesting user
     *
     * @param message
     *            the message
     * @ssdd
     */
    @Override
    public void productPublicationStatusNotificationHandler(ProductPublicationStatusNotificationMessage message) {

        String userID = message.getUserID();
        String statusStr = message.getStatus();

        logger.debug("productPublicationStatusNotificationHandler: sending notification to " +
                     userID);

        // ProductPublicationStatusType status = WorkProductHelper
        // .toProductPublicationStatusType(statusStr);

        ArrayList<NotificationMessageHolderType> messages = new ArrayList<NotificationMessageHolderType>();

        NotificationMessageHolderType t = NotificationMessageHolderType.Factory.newInstance();
        NotificationMessageHolderType.Message m = t.addNewMessage();
        XmlObject object;
        try {
            object = XmlObject.Factory.parse(statusStr);
            m.set(object);
            messages.add(t);

            NotificationMessageHolderType[] notification = new NotificationMessageHolderType[messages.size()];

            notification = messages.toArray(notification);
            logger.debug("productPublicationStatusNotificationHandler: size:" + notification.length);
            notificationService.notify(userID, notification);
        } catch (Throwable e) {
            logger.error("productPublicationStatusNotificationHandler: error creating and sending product publication status notification to " +
                         userID);
            e.printStackTrace();
        }
    }

    /**
     * Send local message.
     *
     * @param message
     *            the message
     * @param messageType
     *            the message type
     */
    private void sendLocalMessage(String message, CORE2CORE_MESSAGE_TYPE messageType) {

        logger.debug("sendLocalMessage: messageType=" + messageType + " message=[" + message + "]");

        XmlObject xmlObj;
        try {
            xmlObj = XmlObject.Factory.parse(message);
        } catch (Throwable e) {
            logger.error("sendMessage: Error parsing message - not a valid XML string");
            throw new IllegalArgumentException("Message is not a valid XML string");
        }
        Core2CoreMessageDocument doc = Core2CoreMessageDocument.Factory.newInstance();
        doc.addNewCore2CoreMessage().set(xmlObj);

        Core2CoreMessage msg = new Core2CoreMessage();
        msg.setFromCore(configurationService.getCoreName());
        msg.setToCore(configurationService.getCoreName());
        msg.setMessageType(messageType.toString());
        msg.setMessage(doc.toString());

        core2CoreMessageNotificationHandler(msg);
    }

    /**
     * Sends a message to the local core or remote cores with which there are agreements and that
     * are online.
     *
     * @param message
     *            the message
     * @param messageType
     *            the message type
     * @param hostName
     *            the host name
     *
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws RemoteCoreUnknownException
     *             the remote core unknown exception
     * @throws RemoteCoreUnavailableException
     *             the remote core unavailable exception
     * @throws LocalCoreNotOnlineException
     *             the local core not online exception
     * @throws NoShareAgreementException
     *             the no share agreement exception
     * @throws NoShareRuleInAgreementException
     *             the no share rule in agreement exception
     * @ssdd
     */
    @Override
    public void sendMessage(String message, CORE2CORE_MESSAGE_TYPE messageType, String hostName)
        throws IllegalArgumentException, RemoteCoreUnknownException,
        RemoteCoreUnavailableException, LocalCoreNotOnlineException, NoShareAgreementException,
        NoShareRuleInAgreementException {

        logger.debug("sendMessage - send message=[" + message + "]");

        if (messageType == CORE2CORE_MESSAGE_TYPE.XMPP_MESSAGE) {
            throw new IllegalArgumentException("cannot send generic XMPP message with sendMessage");
            // // send to the XMPP component
            // Core2CoreMessage msg = new Core2CoreMessage();
            // msg.setFromCore(configurationService.getCoreName());
            // msg.setToCore(hostName);
            // msg.setMessageType(messageType.name());
            // msg.setMessage(message);
            // Message<Core2CoreMessage> notification =
            // (Message<Core2CoreMessage>) new GenericMessage<Core2CoreMessage>(msg);
            // core2CoreMessageChannel.send(notification);
            //
            // return;
        }

        if (hostName.equals(configurationService.getFullyQualifiedHostName())) {
            sendLocalMessage(message, messageType);
        } else {

            if (hasShareAgreement(hostName)) {
                CoreConfigType coreConfig = directoryService.getCoreConfig(configurationService.getCoreName());

                if (coreConfig == null || coreConfig.getOnlineStatus() != CoreStatusType.ONLINE) {
                    throw new LocalCoreNotOnlineException();
                } else {

                    String coreJID = getCoreJIDFromAgreements(hostName);
                    coreConfig = directoryService.getCoreConfig(coreJID);

                    if (coreConfig == null) {
                        throw new RemoteCoreUnknownException(coreJID);
                    } else if (coreConfig.getOnlineStatus() != CoreStatusType.ONLINE) {
                        throw new RemoteCoreUnavailableException(coreJID);
                    } else {
                        XmlObject xmlObj;
                        try {
                            xmlObj = XmlObject.Factory.parse(message);
                        } catch (Throwable e) {
                            logger.error("sendMessage: Error parsing message - not a valid XML string");
                            throw new IllegalArgumentException("Message is not a valid XML string");
                        }

                        Core2CoreMessageDocument doc = Core2CoreMessageDocument.Factory.newInstance();
                        doc.addNewCore2CoreMessage().set(xmlObj);

                        // send to the XMPP component
                        Core2CoreMessage msg = new Core2CoreMessage();
                        msg.setFromCore(configurationService.getCoreName());
                        msg.setToCore(coreJID);
                        msg.setMessageType(messageType.name());
                        msg.setMessage(doc.toString());
                        Message<Core2CoreMessage> notification = new GenericMessage<Core2CoreMessage>(msg);
                        core2CoreMessageChannel.send(notification);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.leidos.xchangecore.core.infrastructure.service.CommunicationsService#sendXMPPMessage
     * .String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void sendXMPPMessage(String body, String xhtml, String xml, String jid) {

        // send to the XMPP component
        Core2CoreMessage msg = new Core2CoreMessage();
        msg.setFromCore(configurationService.getCoreName());
        msg.setToCore(jid);
        msg.setMessageType(CORE2CORE_MESSAGE_TYPE.XMPP_MESSAGE.name());
        msg.setBody(body);
        msg.setXhtml(xhtml);
        msg.setMessage(xml);
        Message<Core2CoreMessage> notification = new GenericMessage<Core2CoreMessage>(msg);
        core2CoreMessageChannel.send(notification);
    }

    /**
     * Sets the agreement service.
     *
     * @param agreementService
     *            the new agreement service
     */
    public void setAgreementService(AgreementService agreementService) {

        this.agreementService = agreementService;
    }

    /**
     * Sets the broadcast message notification channel.
     *
     * @param broadcastMessageNotificationChannel
     *            the new broadcast message notification channel
     */
    public void setBroadcastMessageNotificationChannel(MessageChannel broadcastMessageNotificationChannel) {

        this.broadcastMessageNotificationChannel = broadcastMessageNotificationChannel;
    }

    /**
     * Sets the configuration service.
     *
     * @param configurationService
     *            the new configuration service
     */
    public void setConfigurationService(ConfigurationService configurationService) {

        this.configurationService = configurationService;
    }

    /**
     * Sets the core2 core message channel.
     *
     * @param core2CoreMessageChannel
     *            the new core2 core message channel
     */
    public void setCore2CoreMessageChannel(MessageChannel core2CoreMessageChannel) {

        this.core2CoreMessageChannel = core2CoreMessageChannel;
    }

    /**
     * Sets the delete interest group channel.
     *
     * @param deleteInterestGroupChannel
     *            the new delete interest group channel
     */
    public void setDeleteInterestGroupChannel(MessageChannel deleteInterestGroupChannel) {

        this.deleteInterestGroupChannel = deleteInterestGroupChannel;
    }

    /**
     * Sets the directory service.
     *
     * @param directoryService
     *            the new directory service
     */
    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    /**
     * Sets the interest group management component.
     *
     * @param interestGroupManagementComponent
     *            the new interest group management component
     */
    public void setInterestGroupManagementComponent(InterestGroupManagementComponent interestGroupManagementComponent) {

        this.interestGroupManagementComponent = interestGroupManagementComponent;
    }

    /**
     * Sets the new interest group created channel.
     *
     * @param newInterestGroupCreatedChannel
     *            the new new interest group created channel
     */
    public void setNewInterestGroupCreatedChannel(MessageChannel newInterestGroupCreatedChannel) {

        this.newInterestGroupCreatedChannel = newInterestGroupCreatedChannel;
    }

    /**
     * Sets the notification service.
     *
     * @param notificationService
     *            the new notification service
     */
    public void setNotificationService(NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    /**
     * Sets the product publication channel.
     *
     * @param productPublicationChannel
     *            the new product publication channel
     */
    public void setProductPublicationChannel(MessageChannel productPublicationChannel) {

        this.productPublicationChannel = productPublicationChannel;
    }

    /**
     * Sets the product publication status channel.
     *
     * @param productPublicationStatusChannel
     *            the new product publication status channel
     */
    public void setProductPublicationStatusChannel(MessageChannel productPublicationStatusChannel) {

        this.productPublicationStatusChannel = productPublicationStatusChannel;
    }

    /**
     * Sets the pub sub service.
     *
     * @param pubSubService
     *            the new pub sub service
     */
    public void setPubSubService(PubSubService pubSubService) {

        this.pubSubService = pubSubService;
    }

    /**
     * Sets the resource message notification channel.
     *
     * @param resourceMessageNotificationChannel
     *            the new resource message notification channel
     */
    public void setResourceMessageNotificationChannel(MessageChannel resourceMessageNotificationChannel) {

        this.resourceMessageNotificationChannel = resourceMessageNotificationChannel;
    }

    /**
     * Sets the share interest group channel.
     *
     * @param channel
     *            the new share interest group channel
     */
    public void setShareInterestGroupChannel(MessageChannel channel) {

        shareInterestGroupChannel = channel;
    }

    /**
     * Sets the work product service.
     *
     * @param workProductService
     *            the new work product service
     */
    public void setWorkProductService(WorkProductService workProductService) {

        this.workProductService = workProductService;
    }

    /**
     * Work product deleted.
     *
     * @param workProductID
     *            the work product id
     * @param workProductType
     *            the work product type
     * @param subscriptionId
     *            the subscription id
     * @ssdd
     */
    @Override
    public void workProductDeleted(ProductChangeNotificationMessage changedMessage,
                                   Integer subscriptionId) {

        pubSubService.unsubscribeBySubscriptionID(subscriptionId);
    }
}
