package com.leidos.xchangecore.core.infrastructure.service.impl;

import gov.ucore.ucore.x20.EventType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.uicds.agreementService.AgreementType;
import org.uicds.directoryServiceData.WorkProductTypeListType;
import org.uicds.notificationService.NotifyRequestDocument;
import org.uicds.notificationService.WorkProductDeletedNotificationDocument;
import org.uicds.workProductService.WorkProductPublicationResponseDocument;
import org.uicds.workProductService.WorkProductPublicationResponseType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3.x2005.x08.addressing.MetadataType;

import com.ibm.icu.util.StringTokenizer;
import com.leidos.xchangecore.core.infrastructure.dao.AgreementDAO;
import com.leidos.xchangecore.core.infrastructure.dao.NotificationDAO;
import com.leidos.xchangecore.core.infrastructure.dao.ProductSubscriptionByTypeDAO;
import com.leidos.xchangecore.core.infrastructure.dao.UserInterestGroupDAO;
import com.leidos.xchangecore.core.infrastructure.exceptions.EmptySubscriberNameException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductIDException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductTypeException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NullSubscriberException;
import com.leidos.xchangecore.core.infrastructure.listener.NotificationListener;
import com.leidos.xchangecore.core.infrastructure.messages.AgreementRosterMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductChangeNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProfileNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.model.Agreement;
import com.leidos.xchangecore.core.infrastructure.model.Notification;
import com.leidos.xchangecore.core.infrastructure.model.NotificationMessage;
import com.leidos.xchangecore.core.infrastructure.model.NotificationSubscription;
import com.leidos.xchangecore.core.infrastructure.model.ProductSubscriptionByType;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.CommunicationsService;
import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.service.NotificationService;
import com.leidos.xchangecore.core.infrastructure.service.PubSubService;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.util.AgreementUtil;
import com.leidos.xchangecore.core.infrastructure.util.DigestHelper;
import com.leidos.xchangecore.core.infrastructure.util.FilterUtil;
import com.leidos.xchangecore.core.infrastructure.util.LogEntry;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.saic.precis.x2009.x06.base.IdentificationType;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;

/**
 * The NotificationService implementation.
 *
 * @see com.leidos.xchangecore.core.infrastructure.model.Agreement Agreement Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.Notification Notification Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.NotificationMessage Notification Message
 *      Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.NotificationSubscription
 *      NotificationSubscription Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.ProductSubscriptionByType
 *      ProductSubscriptionByType Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.Profile Profile Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.WorkProduct WorkProduct Data Model
 * @ssdd
 */
public class NotificationServiceImpl
    implements NotificationService, ServiceNamespaces {

    private class MessageComparator
        implements Comparator<NotificationMessage> {

        @Override
        public int compare(final NotificationMessage msg1, final NotificationMessage msg2) {

            return msg1.getId() - msg2.getId();
        }
    }

    /** The agreement dao. */
    private AgreementDAO agreementDAO;

    private CommunicationsService communicationsService;

    /** The configuration service. */
    private ConfigurationService configurationService;

    /** The directory service. */
    private DirectoryService directoryService;

    /** The log. */
    Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    /** The notification dao. */
    private NotificationDAO notificationDAO;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    /** The product subscription by type dao. */
    private ProductSubscriptionByTypeDAO productSubscriptionByTypeDAO;

    /** The pub sub service. */
    private PubSubService pubSubService;

    // UserInterestGroupDAO
    private UserInterestGroupDAO userInterestGroupDAO;

    /** The web service template. */
    private WebServiceTemplate webServiceTemplate;

    /** The work product service. */
    private WorkProductService workProductService;

    /**
     * Add a notification message.
     *
     * @param subscriptionID
     *            the subscription id
     * @param msgType
     *            the msg type
     * @param message
     *            the message
     * @ssdd
     */

    // @Transactional
    private void addNotificationMessage(final Notification notification,
                                        final Integer subscriptionID,
                                        final String msgType,
                                        final String message) {

        logger.debug("addNotificationMessage:\n\tNotification: " + notification +
            "\n\tsubscriptionID: " + subscriptionID + "\n\tmsgType: " + msgType +
            "\n\tmesage: " + message);
        notification.addMessage(subscriptionID, msgType, message);
        notificationDAO.makePersistent(notification);
    }

    /**
     * Adds the subscription.
     *
     * @param subscription
     *            the subscription
     * @ssdd
     */
    // @Transactional
    private void addNotificationSubscription(final Notification notification,
                                             final NotificationSubscription subscription) {

        notification.addSubscription(subscription);
        notificationDAO.makePersistent(notification);

    }

    /**
     * Clear notification messages.
     *
     * @ssdd
     */

    // @Transactional
    private void clearNotificationMessages(final Notification notification) {

        notification.clearMessages();
        notificationDAO.makePersistent(notification);

    }

    /**
     * Creates the notification message holder.
     *
     * @param msgType
     *            the msg type
     * @param message
     *            the message
     * @param jid
     *            the requestor jid
     * @return the notification message holder type
     */
    private NotificationMessageHolderType createNotificationMessageHolder(final String msgType,
                                                                          final String message) {

        final NotificationMessageHolderType t = NotificationMessageHolderType.Factory.newInstance();
        final Message m = t.addNewMessage();
        final XmlCursor xc = m.newCursor();
        xc.toNextToken();
        XmlCursor ec = null;

        // if notification msg is profile msg type
        // if (msgType.equals(ProfileNotificationMessage.NAME)) {
        // Profile profile = profileDAO.findByEntityID(message);
        // if (profile != null) {
        // UserProfileType userProfile = ProfileUtil.copyProperties(profile);
        // ec = userProfile.newCursor();
        // }
        // }
        // if notification msg is agreement msg type
        // else if (msgType.equals(AgreementRosterMessage.NAME)) {
        if (msgType.equals(AgreementRosterMessage.NAME)) {
            logger.debug("createNotificationMessageHolder: for agreementID: " + message);
            final Agreement agreement = agreementDAO.findById(Integer.parseInt(message));
            if (agreement != null) {
                final AgreementType agreementType = AgreementUtil.copyProperties(agreement);
                ec = agreementType.newCursor();
            }
        }
        // if notification msg is a notify msg
        else if (msgType.equals(NOTIFY_MESSAGE)) {
            logger.debug("NOTIFY MESSAGE FOUND");
            XmlObject doc;
            try {
                doc = XmlObject.Factory.parse(message);
                if (doc != null)
                    ec = doc.newCursor();
            } catch (final XmlException e) {
                logger.debug("createNotificationMessageHolder: Error parsing message [" + message +
                    "]  into xml object");
                e.printStackTrace();
            }

            // xc.toChild(notificationMsg.getMessage());
        } else if (msgType.equals("WorkProductDeleted")) {
            logger.debug("WorkProductDeleted MESSAGE FOUND");
            XmlObject doc;
            try {
                doc = XmlObject.Factory.parse(message);
                if (doc != null)
                    ec = doc.newCursor();
            } catch (final XmlException e) {
                logger.debug("createNotificationMessageHolder: Error parsing message [" + message +
                    "]  into xml object");
                e.printStackTrace();
            }
        }
        // else if notification msg is workProductID
        else {
            // WorkProduct product = workProductService.getProduct(message);
            // WorkProductNotificationType wpNotification =
            // WorkProductNotificationType.Factory.newInstance();
            // wpNotification.addNewWorkProduct().set(WorkProductHelper.toWorkProductSummary(product));
            // ec = wpNotification.newCursor();
            logger.debug("WorkProductID MESSAGE FOUND");
            XmlObject doc;
            try {
                doc = XmlObject.Factory.parse(message);
                if (doc != null)
                    ec = doc.newCursor();
            } catch (final XmlException e) {
                logger.debug("createNotificationMessageHolder: Error parsing message [" + message +
                    "]  into xml object");
                e.printStackTrace();
            }
        }

        if (ec != null) {
            ec.toFirstContentToken();
            ec.moveXml(xc);
            ec.dispose();
        }
        xc.dispose();

        return t;
    }

    /**
     * Creates the pull point.
     *
     * @param entityID
     *            the entity id
     * @return the endpoint reference type
     * @ssdd
     */
    @Override
    public EndpointReferenceType createPullPoint(final String entityID) {

        logger.debug("createPullPoint: jid: '" + entityID + "'");

        final XmlOptions options = new XmlOptions();
        options.setSaveInner();
        final EndpointReferenceType endpoint = EndpointReferenceType.Factory.newInstance();

        // Set the url for the notification service for pull points
        endpoint.addNewAddress().setStringValue(
            getConfigurationService().getWebServiceBaseURL() + "/" + entityID);

        // Add the service identification
        final MetadataType metadata = endpoint.addNewMetadata();
        final XmlCursor xc = metadata.newCursor();
        xc.toNextToken();
        xc.insertElementWithText("scheme", getConfigurationService().getServiceNameURN(
            NOTIFICATION_SERVICE_NAME));
        xc.dispose();

        // create or update notification model and persist
        Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification == null) {
            notification = new Notification();
            notification.setEntityID(entityID);
            notification.setEndpointWS(false);// not webservice but pullpoint
        }
        notification.setEndpointURL(endpoint.getAddress().getStringValue());
        makePersistent(notification);

        return endpoint;
    }

    @Override
    public boolean destroyPullPoint(final String entityID) {

        Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification == null)
            return false;

        // Remove all the subscriptions for this pull point
        for (final NotificationSubscription subscription : notification.getSubscriptions())
            pubSubService.unsubscribeBySubscriptionID(subscription.getSubscriptionID());

        // Given the CASCADE, clearing subscriptions and making the notification transient causes an
        // constraint error.
        // HashSet<NotificationSubscription> subscriptions = new
        // HashSet<NotificationSubscription>();
        // notification.setSubscriptions(subscriptions);

        // Remove all the messages
        notification.clearMessages();

        // making the notification transient causes an constraint error if you try
        // a findByEntityId again on the same entity So until we figure out that issue
        // we'll just leave the Notification object
        makeTransient(notification);
        notification = null;

        return true;
    }

    // FLi modified on 11/29/2011
    @Override
    public int findMsgCountByEntityId(final String entityId) {

        return notificationDAO.findMsgCountByEntityId(entityId);
    }

    /**
     * Gets the agreement dao.
     *
     * @return the agreement dao
     */
    public AgreementDAO getAgreementDAO() {

        return agreementDAO;
    }

    public CommunicationsService getCommunicationsService() {

        return communicationsService;
    }

    /**
     * Gets the configuration service.
     *
     * @return the configuration service
     */
    @Override
    public ConfigurationService getConfigurationService() {

        return configurationService;
    }

    /**
     * Gets the current message.
     *
     * @param topic
     *            the topic
     * @return the current message
     * @ssdd
     */
    @Override
    public NotificationMessageHolderType getCurrentMessage(final QName topic, final String jid) {

        logger.debug("Looking for last current message on topic: " + topic.toString() + ", jid: " +
            jid);
        final String message = pubSubService.getLastPublishedMessage(topic.toString());
        logger.debug("Last current message received: " + message);

        NotificationMessageHolderType t = NotificationMessageHolderType.Factory.newInstance();

        if (message != null)
            if (topic.toString().toLowerCase().startsWith("profile"))
                t = createNotificationMessageHolder(ProfileNotificationMessage.NAME, message);
            else if (topic.toString().toLowerCase().startsWith("agreement"))
                t = createNotificationMessageHolder(AgreementRosterMessage.NAME, message);
            else
                t = createNotificationMessageHolder("WorkProductID", message);
        return t;
    }

    /**
     * Gets the matching messages for the specified entity Id.
     *
     * @param entityId
     *            the entity id
     * @return the matching messages
     * @ssdd
     */
    @Override
    @Transactional
    public IdentificationType[] getMatchingMessages(final String entityId) {

        IdentificationType[] identifications = null;

        logger.debug("getMatchingMessages for user: " + entityId);

        final Notification notification = notificationDAO.findByEntityId(entityId);
        if (notification != null) {
            final List<IdentificationType> workProductIdentificationList = new ArrayList<IdentificationType>();

            final Set<NotificationSubscription> notificationSubscriptionSet = notification.getSubscriptions();

            for (final NotificationSubscription notificationSubscription : notificationSubscriptionSet) {

                final Integer subscriptionId = notificationSubscription.getSubscriptionID();

                final List<ProductSubscriptionByType> productSubscriptionList = productSubscriptionByTypeDAO.findBySubscriptionId(subscriptionId);
                for (final ProductSubscriptionByType productSubscription : productSubscriptionList) {
                    final List<WorkProduct> workProductList = workProductService.listByProductType(productSubscription.getProductType());
                    for (final WorkProduct workProduct : workProductList)
                        if (userInterestGroupDAO.isEligible(entityId,
                            workProduct.getFirstAssociatedInterestGroupID())) {
                            final IdentificationType identification = WorkProductHelper.getWorkProductIdentification(workProduct);
                            workProductIdentificationList.add(identification);
                        }
                }
            }

            if (workProductIdentificationList != null && workProductIdentificationList.size() > 0)
                identifications = new IdentificationType[workProductIdentificationList.size()];
            else
                identifications = new IdentificationType[0];
            workProductIdentificationList.toArray(identifications);
        }
        return identifications;
    }

    /**
     * Gets the messages.
     *
     * @param entityID
     *            the entity id
     * @param num
     *            the num
     * @return the messages
     * @ssdd
     */
    @Override
    @Transactional
    public NotificationMessageHolderType[] getMessages(final String entityID, final int num) {

        NotificationMessageHolderType[] response = null;

        // make a performance log entry
        final LogEntry logEntry = new LogEntry();
        logEntry.setCategory(LogEntry.CATEGORY_NOTIFICATION);
        logEntry.setAction(LogEntry.ACTION_NOTIFICATION_POLL);
        logEntry.setEntityId(entityID);
        logger.info(logEntry.getLogEntry());

        // if there's an entry for this id (i.e. bonnera@core.1.saic.com)
        final Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification != null) {

            final ArrayList<NotificationMessageHolderType> messages = new ArrayList<NotificationMessageHolderType>();

            // Get Subscriptions, iterate through, and build message per workProduct message
            final Set<NotificationMessage> notfMessages = notification.getMessages();
            if (notfMessages.size() > 0) {

                logger.debug("getMessage: User: " + entityID + " has " + notfMessages.size() +
                    " messages");
                final List<NotificationMessage> notificationMessages = new ArrayList<NotificationMessage>();
                notificationMessages.addAll(notfMessages);
                Collections.sort(notificationMessages, new MessageComparator());
                for (final NotificationMessage msg : notificationMessages) {
                    final NotificationMessageHolderType notifyMessage = createNotificationMessageHolder(
                        msg.getType(), new String(msg.getMessage()));
                    if (notifyMessage != null)
                        messages.add(notifyMessage); // ddh - filter out interest group by the jid
                }
            }

            // Removes messages from queue
            if (messages.size() > 0)
                clearNotificationMessages(notification);
            logger.debug("getMessage: User: " + entityID + " can asscess " + messages.size() +
                " messages");

            response = new NotificationMessageHolderType[messages.size()];
            response = messages.toArray(response);

        } else
            logger.error(entityID + " not found in subscription map");

        return response;
    }

    /**
     * Gets the notification dao.
     *
     * @return the notification dao
     */
    public NotificationDAO getNotificationDAO() {

        return notificationDAO;
    }

    private String getNotificationMessageBody(final WorkProduct product) {

        final StringBuffer body = new StringBuffer();
        body.append("UICDS ");
        body.append(product.getProductType());
        body.append(" work product");

        EventType event = null;
        if (product.getDigest() != null)
            event = DigestHelper.getFirstEventWithActivityNameIdentifier(product.getDigest().getDigest());
        if (event != null) {
            body.append(" associated with incident ");
            body.append(event.getIdentifierArray(0).getStringValue());
        }

        if (product.isActive()) {
            if (product.getProductVersion() == 1)
                body.append(" created");
            else
                body.append(" updated ");
        } else
            body.append(" deleted ");
        body.append(" by ");
        body.append(product.getUpdatedBy());
        body.append(" at ");
        body.append(product.getUpdatedDate().toString());

        return body.toString();
    }

    /**
     * Gets the product subscription by type dao.
     *
     * @return the product subscription by type dao
     */
    public ProductSubscriptionByTypeDAO getProductSubscriptionByTypeDAO() {

        return productSubscriptionByTypeDAO;
    }

    /**
     * Gets the pub sub service.
     *
     * @return the pub sub service
     */
    @Override
    public PubSubService getPubSubService() {

        return pubSubService;
    }

    /**
     * Gets the pull point.
     *
     * @param entityID
     *            the entity id
     * @return the pull point or null if one doesn't exist for the entity id
     * @ssdd
     */
    @Override
    public EndpointReferenceType getPullPoint(final String entityID) {

        EndpointReferenceType endpoint = null;
        final Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification != null) {
            endpoint = EndpointReferenceType.Factory.newInstance();
            endpoint.addNewAddress().setStringValue(notification.getEndpointURL());
            // Add the service identification
            final MetadataType metadata = endpoint.addNewMetadata();
            final XmlCursor xc = metadata.newCursor();
            xc.toNextToken();
            xc.insertElementWithText("scheme", getConfigurationService().getServiceNameURN(
                NOTIFICATION_SERVICE_NAME));
            xc.dispose();
        } else
            return null;
        return endpoint;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     */
    @Override
    public String getServiceName() {

        return NOTIFICATION_SERVICE_NAME;
    }

    private Hashtable<String, List<String>> getUserIGIDList(List<Notification> notifications) {

        final Hashtable<String, List<String>> userIGIDListHash = new Hashtable<String, List<String>>();
        for (final Notification notification : notifications) {
            final List<String> igIDList = userInterestGroupDAO.getInterestGroupList(notification.getEntityID());
            if (igIDList != null && igIDList.size() > 0)
                userIGIDListHash.put(notification.getEntityID(), igIDList);
            else
                userIGIDListHash.put(notification.getEntityID(), new ArrayList<String>());
        }
        return userIGIDListHash;
    }

    public UserInterestGroupDAO getUserInterestGroupDAO() {

        return userInterestGroupDAO;
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
    public void InvalidXpathNotification(final Integer subscriptionId, final String errorMessage) {

        // search all notifications and update any subscription msgs which have specified ID
        for (final Notification notification : notificationDAO.findAll())
            for (final NotificationSubscription sub : notification.getSubscriptions())
                if (subscriptionId.compareTo(sub.getSubscriptionID()) == 0) {
                    final ProductPublicationStatus status = new ProductPublicationStatus();
                    status.setStatus(ProductPublicationStatus.FailureStatus);
                    status.setReasonForFailure("Subscription for [" + notification.getEntityID() +
                                               "]. " + errorMessage);
                    final WorkProductPublicationResponseType errorResponse = WorkProductHelper.toWorkProductPublicationResponse(status);
                    final WorkProductPublicationResponseDocument errorResponseDoc = WorkProductPublicationResponseDocument.Factory.newInstance();
                    errorResponseDoc.addNewWorkProductPublicationResponse().set(errorResponse);

                    addNotificationMessage(notification, subscriptionId, NOTIFY_MESSAGE,
                        errorResponseDoc.toString());
                }
    }

    /**
     * Invoke web service template.
     *
     * @param notification
     *            the notification
     * @param msgType
     *            the msg type
     * @param message
     *            the message
     */
    private void invokeWebServiceTemplate(final Notification notification,
                                          final String msgType,
                                          final String message) {

        // set url on webservice template
        webServiceTemplate.setDefaultUri(notification.getEndpointURL());

        // create the NotificationMessageRequest
        final NotifyRequestDocument request = NotifyRequestDocument.Factory.newInstance();

        logger.debug("invokeWebServiceTemplate: using jid as " + notification.getEndpointURL());
        request.addNewNotifyRequest().addNewNotificationMessage().setMessage(
            createNotificationMessageHolder(msgType, message).getMessage());

        final XmlCursor xc = request.getNotifyRequest().newCursor();
        xc.toNextToken();
        final QName to = new QName("http://www.w3.org/2005/08/addressing", "To");
        xc.insertElementWithText(to, notification.getEntityID());
        xc.dispose();
        logger.debug("NOTIFY_REQUEST: " + request.toString());

        // invoke webService
        webServiceTemplate.marshalSendAndReceive(request);
    }

    /**
     * Parsing the message to figure out whether the jid can access the work product based on the
     * Interest Group
     *
     * @param message
     * @return
     */
    private boolean isEligible(final String message, final String jid) {

        WorkProductDocument wpDoc = null;
        try {
            wpDoc = WorkProductDocument.Factory.parse(message);
        } catch (final Exception e) {
            logger.error("Parsing: " + message + "\nParsing Error: " + e.getMessage());
            return false;
        }

        // log.debug("WorkProductDocument:\n" + wpDoc.toString());
        final String igID = WorkProductHelper.getInterestGroupID(wpDoc.getWorkProduct());
        logger.debug("interest group ID: " + igID);
        final boolean eligibility = userInterestGroupDAO.isEligible(jid, igID);
        logger.debug("User: " + jid + " is" + (eligibility ? " " : " not") +
            " allowed to access IG: " + igID);
        return eligibility;
    }

    // @Transactional
    private Notification makePersistent(final Notification notification) {

        return notificationDAO.makePersistent(notification);
    }

    // @Transactional
    private void makeTransient(final Notification notification) {

        notificationDAO.makeTransient(notification);
    }

    /**
     * New work product version. Searches all notifications and updates any subscription messages
     * which have the specified subscription Id Executes every time a new version on any workproduct
     * is updated
     *
     * @param workProductID
     *            the work product id
     * @param subscriptionId
     *            the subscription id
     * @ssdd
     */
    @Override
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void newWorkProductVersion(final String productID, final Integer subscriptionId) {

        // get the product
        final WorkProduct product = workProductService.getProduct(productID);

        final WorkProductDocument productDoc = WorkProductDocument.Factory.newInstance();
        final WorkProductDocument.WorkProduct summary = WorkProductHelper.toWorkProductSummary(product);
        final String igID = WorkProductHelper.getInterestGroupID(summary);
        productDoc.setWorkProduct(summary);
        final String productVersion = WorkProductHelper.getProductVersion(summary);
        final int version = Integer.parseInt(productVersion);
        final Hashtable<String, List<String>> userIGIDListHash = getUserIGIDList(notificationDAO.findBySubscriptionId(subscriptionId));
        try {
            final TransactionTemplate tt = new TransactionTemplate(platformTransactionManager);
            tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            // tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
            tt.execute(new TransactionCallbackWithoutResult() {

                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {

                    // search all notifications and update any subscription msgs which have
                    // specified ID
                    final List<Notification> notifications = notificationDAO.findBySubscriptionId(subscriptionId);

                    for (final Notification notification : notifications) {
                        logger.debug("adding workproduct: " + productID + " with IGID: " + igID +
                            " to subid: " + subscriptionId + " to entity: " +
                            notification.getEntityID());
                        productDoc.setWorkProduct(summary);

                        // if endpoint is web service url
                        if (notification.isEndpointWS()) {
                            logger.debug("Notification going to be SENT to webServiceURL: " +
                                notification.getEndpointURL());
                            final String url = notification.getEndpointURL();
                            try {
                                final URI u = new URI(url);
                                final String proto = u.getScheme();
                                // log.debug("Handling protocol: " + proto);
                                if (proto.equalsIgnoreCase("xmpp"))
                                    try {
                                        logger.debug("XMPP message sent to: " + u.getHost());
                                        communicationsService.sendXMPPMessage(
                                            NotificationServiceImpl.this.getNotificationMessageBody(product),
                                            null, productDoc.xmlText(), u.getSchemeSpecificPart());
                                    } catch (final IllegalArgumentException e) {
                                        logger.error("IllegalArgumentException newWorkProductVersion sending XMPP message: " +
                                            e.getMessage());
                                    } catch (final Exception e) {
                                        logger.error("Exception newWorkProductVersion IllegalArgumentException: " +
                                            e.getMessage());
                                    }
                                else {
                                    // invoke specified webService
                                    logger.debug("WS-Notify message sent to :" + u.toString());
                                    NotificationServiceImpl.this.invokeWebServiceTemplate(
                                        notification, "WorkProductID", productDoc.toString());
                                }
                            } catch (final URISyntaxException e) {
                                logger.debug("Error decoding URI: " + e.getMessage());
                            }
                        }
                        // if endpoint is pullpoint address
                        else {
                            final List<String> igIDList = userIGIDListHash.get(notification.getEntityID());
                            logger.debug("User: " + notification.getEntityID() + " can access " +
                                igIDList.size() + " IGs");
                            if (igID == null || igIDList.size() > 0 && igIDList.contains(igID)) {

                                logger.debug("Add notification for User: " +
                                    notification.getEntityID());

                                if (version > 1)
                                    notification.clearoldMessage(productID);

                                notification.addMessage(subscriptionId, productID,
                                    productDoc.toString());
                            } else
                                logger.debug("User: " + notification.getEntityID() +
                                    " can NOT access " + igID);
                        }
                    }
                }
            });
        } catch (final Exception e) {
            // System.err.println("productChangeNotificationHandler calling publishWorkProduct Exception sending message: "
            // + e.getMessage());
            logger.error("Exception handling product change notification message when publishing: " +
                e.getMessage());
        }
    }

    /**
     * Adds notification messages for the specified entity Id
     *
     * @param entityID
     *            the entity id
     * @param notifications
     *            the notifications
     * @ssdd
     */
    @Override
    public void notify(final String entityID, final NotificationMessageHolderType[] notifications) {

        // find Notification for desired entityID
        logger.debug("Notify entityID: " + entityID);
        final Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification != null) {
            logger.debug("notifications to add: " + notifications.length);
            // for each notify message add it to the list of notification messages
            for (final NotificationMessageHolderType notf : notifications) {
                final NotificationMessageHolderType.Message msg = notf.getMessage();
                if (msg != null)
                    addNotificationMessage(notification, 0, NOTIFY_MESSAGE, msg.toString());
            }
        }
    }

    /**
     * Searches all notifications and sends any subscription messages to the subscriber interface.
     *
     * @ssdd
     */
    public void sendSunscriberInterface() {

        // search all notifications and update any subscription msgs which have specified ID
        final List<Notification> notifications = notificationDAO.findAll();
        if (!notifications.isEmpty())
            pubSubService.subscriberInterface(this);
    }

    /**
     * Sets the agreement dao.
     *
     * @param agreementDAO
     *            the new agreement dao
     */
    public void setAgreementDAO(final AgreementDAO agreementDAO) {

        this.agreementDAO = agreementDAO;
    }

    public void setCommunicationsService(final CommunicationsService communicationsService) {

        this.communicationsService = communicationsService;
    }

    /**
     * Sets the configuration service.
     *
     * @param service
     *            the new configuration service
     */
    @Override
    public void setConfigurationService(final ConfigurationService service) {

        configurationService = service;
    }

    /**
     * Sets the directory service.
     *
     * @param directoryService
     *            the new directory service
     */
    public void setDirectoryService(final DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    /**
     * Sets the notification dao.
     *
     * @param n
     *            the new notification dao
     */
    public void setNotificationDAO(final NotificationDAO n) {

        notificationDAO = n;
    }

    /**
     * Sets the product subscription by type dao.
     *
     * @param n
     *            the new product subscription by type dao
     */
    public void setProductSubscriptionByTypeDAO(final ProductSubscriptionByTypeDAO n) {

        productSubscriptionByTypeDAO = n;
    }

    /**
     * Sets the pub sub service.
     *
     * @param service
     *            the new pub sub service
     */
    @Override
    public void setPubSubService(final PubSubService service) {

        pubSubService = service;
    }

    public void setUserInterestGroupDAO(final UserInterestGroupDAO userInterestGroupDAO) {

        this.userInterestGroupDAO = userInterestGroupDAO;
    }

    /**
     * Sets the web service template.
     *
     * @param webServiceTemplate
     *            the new web service template
     */
    public void setWebServiceTemplate(final WebServiceTemplate webServiceTemplate) {

        this.webServiceTemplate = webServiceTemplate;
    }

    /**
     * Sets the work product service.
     *
     * @param workProductService
     *            the new work product service
     */
    public void setWorkProductService(final WorkProductService workProductService) {

        this.workProductService = workProductService;
    }

    /**
     * Subscribes to messages of the specified topic type on the specified endpoint
     *
     * @param who
     *            the endpoint reference
     * @param what
     *            the filter type
     * @ssdd
     */
    @Override
    public void subscribe(final EndpointReferenceType who, final FilterType what)
        throws InvalidProductTypeException, NullSubscriberException, EmptySubscriberNameException,
        InvalidProductIDException {

        if (who.getAddress() != null) {

            final String entityID = who.getAddress().getStringValue().substring(
                who.getAddress().getStringValue().lastIndexOf("/") + 1);
            logger.debug("subscribing who: " + entityID);

            // Process topic subscriptions - Allow TopicExpression elements from either
            // the ProfileService schema or the WS-Topics schema
            final String topicExpression = FilterUtil.getTopic(what);
            logger.debug("subscribing what: " + topicExpression);

            // get the namespace map for xpaths
            final Map<String, String> namespaceMap = FilterUtil.getNamespaceMap(what);

            // get the xpath
            final String xPath = FilterUtil.getXPath(what);
            logger.debug("xPath: " + xPath);

            // Process xpath expressions

            final StringTokenizer tokenizer = new StringTokenizer(topicExpression, "/");
            final int topicTreeCount = tokenizer.countTokens();

            String productType = "";
            String productTypeValue = "";
            if (topicTreeCount > 0) {
                productType = tokenizer.nextToken();
                if (tokenizer.hasMoreElements() && topicTreeCount > 1)
                    productTypeValue = tokenizer.nextToken();
                else
                    productTypeValue = "*";

                // WorkProductID TopicExpression (workproduct/* or workproduct/1234)
                if (productType.equalsIgnoreCase("workproduct")) {
                    if (productTypeValue.equals("*")) {
                        logger.debug("Subscribing to ALL WorkProduct updates.");
                        subscribeWorkProductID(productTypeValue, entityID);
                    } else {
                        logger.debug("Subscribing to WorkProductByID: " + productTypeValue);
                        subscribeWorkProductID(String.valueOf(productTypeValue), entityID);
                    }
                }
                // AgreementID TopicExpression (agreement/* or agreement/1234)
                else if (productType.equalsIgnoreCase("agreement")) {
                    if (productTypeValue.equals("*")) {
                        logger.debug("Subscribing to ALL Agreement updates.");
                        subscribeAgreement(productTypeValue, entityID);
                    } else {
                        logger.debug("Subscribing to AgreementID: " + productTypeValue);
                        subscribeAgreement(productTypeValue, entityID);
                    }
                }
                // ProfileID TopicExpression (profile/* or profile/user@core1)
                else if (productType.equalsIgnoreCase("profile")) {
                    if (productTypeValue.equals("*")) {
                        logger.debug("Subscribing to ALL Profile updates.");
                        subscribeProfile(productTypeValue, entityID);
                    } else {
                        logger.debug("Subscribing to Profile Name: " + productTypeValue);
                        subscribeProfile(productTypeValue, entityID);
                    }
                }
                // IncidentID TopicExpression (incident/* or incident/12345)
                else if (productType.equalsIgnoreCase("Incident")) {
                    if (productTypeValue.equals("*")) {
                        logger.debug("Subscribing to ALL Incident updates.");
                        subscribeIncidentIdAndWorkProductType(productType, productTypeValue, xPath,
                            namespaceMap, entityID);
                    } else {
                        logger.debug("Subscribing to IncidentID: " + productTypeValue);
                        subscribeIncidentIdAndWorkProductType(productType, productTypeValue, xPath,
                            namespaceMap, entityID);
                    }
                } else // if productType has incident qualifier (<productType>/incident/*)
                if (productTypeValue.equalsIgnoreCase("Incident")) {
                    String incidentIdQualifier = "";
                    if (tokenizer.hasMoreTokens() && topicTreeCount > 2)
                        incidentIdQualifier = tokenizer.nextToken();
                    else
                        incidentIdQualifier = "*";
                    logger.debug("Subscribing to ProductType : " + productType +
                            " associated with IncidentID : " + incidentIdQualifier);
                    subscribeIncidentIdAndWorkProductType(productType, incidentIdQualifier, xPath,
                        namespaceMap, entityID);
                }
                // All other productTypes in from <productType>/*
                else if (productTypeValue.equals("*")) {
                    logger.debug("Subscribing ProductType: " + productType);
                    subscribeWorkProductType(productType, xPath, namespaceMap, entityID);
                } else {
                    // the type could be something like application/pdf
                    final String type = productType + "/" + productTypeValue;
                    logger.debug("Subscribing ProductType: " + type);
                    subscribeWorkProductType(type, xPath, namespaceMap, entityID);
                }
            } else
                throw new InvalidProductTypeException();
        }
    }

    /**
     * Subscribes to agreements.
     *
     * @param agreementID
     *            the agreement id
     * @param entityID
     *            the entity id
     * @throws InvalidProductIDException
     *             the invalid product id exception
     * @throws NullSubscriberException
     *             the null subscriber exception
     * @throws EmptySubscriberNameException
     *             the empty subscriber name exception
     * @ssdd
     */
    @Override
    public void subscribeAgreement(final String agreementID, final String entityID)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException {

        // create/update notification and persist
        Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification == null) {
            notification = new Notification();
            notification.setEntityID(entityID);
        }

        final Integer subscriptionID = new Random().nextInt();

        final NotificationSubscription sub = new NotificationSubscription();
        sub.setSubscriptionID(subscriptionID);

        addNotificationSubscription(notification, sub);

        final String eeID = entityID;
        final Integer sID = subscriptionID;
        final Integer aID = agreementID.equals("*") ? -1 : Integer.parseInt(agreementID);
        getPubSubService().addAgreementListener(aID,
            new NotificationListener<AgreementRosterMessage>() {

                @Override
                public void onChange(final AgreementRosterMessage message) {

                    logger.debug("AgreementListener.:onChange: agreementID: " +
                    message.getAgreementID());

                    final Notification notification = notificationDAO.findByEntityId(eeID);
                    final Agreement agreement = agreementDAO.findById(message.getAgreementID());
                    String msg = new Integer(message.getAgreementID()).toString();
                    if (agreement != null)
                        msg = agreement.toString();
                    NotificationServiceImpl.this.addNotificationMessage(notification, sID,
                        AgreementRosterMessage.NAME, msg);
                }
            });

    }

    /**
     * Subscribes to notifications for the specified incident id and work product type.
     *
     * @param wpType
     *            the wp type
     * @param incidentID
     *            the incident id
     * @param xpContext
     *            the xp context
     * @param namespaceMap
     *            the namespace map
     * @param entityID
     *            the entity id
     * @return the integer
     * @throws InvalidProductTypeException
     *             the invalid product type exception
     * @throws NullSubscriberException
     *             the null subscriber exception
     * @throws EmptySubscriberNameException
     *             the empty subscriber name exception
     * @ssdd
     */
    public Integer subscribeIncidentIdAndWorkProductType(final String wpType,
                                                         final String incidentID,
                                                         final String xpContext,
                                                         final Map<String, String> namespaceMap,
                                                         final String entityID)
        throws InvalidProductTypeException, NullSubscriberException, EmptySubscriberNameException {

        final Integer subID = getPubSubService().subscribeInterestGroupIdAndWorkProductType(wpType,
            incidentID, xpContext, namespaceMap, this);

        // create/update notification and persist
        Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification == null) {
            notification = new Notification();
            notification.setEntityID(entityID);
        }
        final NotificationSubscription sub = new NotificationSubscription();
        sub.setSubscriptionID(subID);

        addNotificationSubscription(notification, sub);

        return subID;
    }

    /**
     * Subscribes to profile notifications.
     *
     * @param profileID
     *            the profile id
     * @param entityID
     *            the entity id
     * @throws InvalidProductIDException
     *             the invalid product id exception
     * @throws NullSubscriberException
     *             the null subscriber exception
     * @throws EmptySubscriberNameException
     *             the empty subscriber name exception
     * @ssdd
     */
    @Override
    public void subscribeProfile(final String profileID, final String entityID)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException {

        // create/update notification and persist
        Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification == null) {
            notification = new Notification();
            notification.setEntityID(entityID);
        }

        final Integer subscriptionID = new Random().nextInt();

        final NotificationSubscription sub = new NotificationSubscription();
        sub.setSubscriptionID(subscriptionID);

        addNotificationSubscription(notification, sub);

        final String eeID = entityID;
        final Integer sID = subscriptionID;
        getPubSubService().addProfileListener(profileID,
            new NotificationListener<ProfileNotificationMessage>() {

                @Override
                public void onChange(final ProfileNotificationMessage message) {

                    final Notification notification = notificationDAO.findByEntityId(eeID);
                    if (notification != null)
                        NotificationServiceImpl.this.addNotificationMessage(notification, sID,
                            ProfileNotificationMessage.NAME, message.toString());

                }
            });
    }

    /**
     * Subscribes to notifications for the specified work product id.
     *
     * @param workProductID
     *            the work product id
     * @param entityID
     *            the entity id
     * @return the integer
     * @throws InvalidProductIDException
     *             the invalid product id exception
     * @throws NullSubscriberException
     *             the null subscriber exception
     * @throws EmptySubscriberNameException
     *             the empty subscriber name exception
     * @ssdd
     */
    @Override
    public Integer subscribeWorkProductID(final String workProductID, final String entityID)
        throws InvalidProductIDException, NullSubscriberException, EmptySubscriberNameException {

        // Sub with PubSub
        final Integer subscriptionID = getPubSubService().subscribeWorkProductID(workProductID,
            this);

        // create/update notification and persist
        Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification == null) {
            notification = new Notification();
            notification.setEntityID(entityID);
        }
        final NotificationSubscription sub = new NotificationSubscription();
        sub.setSubscriptionID(subscriptionID);

        addNotificationSubscription(notification, sub);

        return subscriptionID;
    }

    /**
     * Subscribe to notifications for the specified work product type.
     *
     * @param wpType
     *            the wp type
     * @param xpContext
     *            the xp context
     * @param namespaceMap
     *            the namespace map
     * @param entityID
     *            the entity id
     * @return the integer
     * @throws InvalidProductTypeException
     *             the invalid product type exception
     * @throws NullSubscriberException
     *             the null subscriber exception
     * @throws EmptySubscriberNameException
     *             the empty subscriber name exception
     * @ssdd
     */
    @Override
    public Integer subscribeWorkProductType(final String wpType,
                                            final String xpContext,
                                            final Map<String, String> namespaceMap,
                                            final String entityID)
        throws InvalidProductTypeException, NullSubscriberException, EmptySubscriberNameException {

        final Integer subID = getPubSubService().subscribeWorkProductType(wpType, xpContext,
            namespaceMap, this);

        // create/update notification and persist
        Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification == null) {
            notification = new Notification();
            notification.setEntityID(entityID);
        }
        final NotificationSubscription sub = new NotificationSubscription();
        sub.setSubscriptionID(subID);

        addNotificationSubscription(notification, sub);

        return subID;
    }

    /**
     * System initialized handler.
     *
     * @param messgae
     *            the messgae
     */
    @Override
    public void systemInitializedHandler(final String messgae) {

        logger.debug("systemInitializedHandler: ... start ...");
        final WorkProductTypeListType typeList = WorkProductTypeListType.Factory.newInstance();
        directoryService.registerUICDSService(NS_NotificationService, NOTIFICATION_SERVICE_NAME,
            typeList, typeList);
        sendSunscriberInterface();
        logger.debug("systemInitializedHandler: ... done ...");
    }

    /**
     * Update endpoint.
     *
     * @param entityID
     *            the entity id
     * @param endpointAddress
     *            the endpoint address
     * @param isWebService
     *            the is web service
     * @ssdd
     */
    @Override
    public void updateEndpoint(final String entityID,
                               final String endpointAddress,
                               final boolean isWebService) {

        Notification notification = notificationDAO.findByEntityId(entityID);
        if (notification == null) {
            notification = new Notification();
            notification.setEntityID(entityID);
        }
        notification.setEndpointURL(endpointAddress);
        notification.setEndpointWS(isWebService); // flag to tell notification if this is WS URL
        notification = makePersistent(notification);
        if (notification == null)
            logger.error("Error updating notification - makePersistent returned null");
    }

    /**
     * Notifies of a work product deletion.
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
    public void workProductDeleted(final ProductChangeNotificationMessage productChangedMessage,
                                   final Integer subscriptionId) {

        // search all notifications and update any subscription msgs which have specified ID
        final List<Notification> notifications = notificationDAO.findBySubscriptionId(subscriptionId);
        for (final Notification notification : notifications) {
            final WorkProductDeletedNotificationDocument doc = WorkProductDeletedNotificationDocument.Factory.newInstance();
            doc.addNewWorkProductDeletedNotification();
            doc.getWorkProductDeletedNotification().addNewWorkProductIdentification().set(
                productChangedMessage.getIdentification().getWorkProductIdentification());
            doc.getWorkProductDeletedNotification().addNewWorkProductProperties().set(
                productChangedMessage.getProperties().getWorkProductProperties());
            final String interestGroupID = productChangedMessage.getProperties().getWorkProductProperties().getAssociatedGroups().getIdentifierArray(
                0).getStringValue();
            logger.debug("trying to delete work product for " + notification.getEntityID() +
                " with IGID: " + interestGroupID);

            // if endpoint is web service url
            if (notification.isEndpointWS()) {
                logger.debug("Notification going to be SENT to webServiceURL: " +
                    notification.getEndpointURL());
                // invoke specified webService
                invokeWebServiceTemplate(notification, "WorkProductDeleted", doc.toString());
            } else if (userInterestGroupDAO.isEligible(notification.getEntityID(), interestGroupID)) {
                logger.debug("Delete Work Product: " + doc);
                addNotificationMessage(notification, subscriptionId, "WorkProductDeleted",
                    doc.toString());
            }

        }
    }
}
