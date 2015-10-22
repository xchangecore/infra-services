package com.leidos.xchangecore.core.infrastructure.endpoint;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.uicds.notificationService.GetCurrentMessageRequestDocument;
import org.uicds.notificationService.GetCurrentMessageResponseDocument;
import org.uicds.notificationService.GetMatchingMessagesRequestDocument;
import org.uicds.notificationService.GetMatchingMessagesResponseDocument;
import org.uicds.notificationService.GetMatchingMessagesResponseDocument.GetMatchingMessagesResponse;
import org.uicds.notificationService.GetMessagesRequestDocument;
import org.uicds.notificationService.GetMessagesResponseDocument;
import org.uicds.notificationService.NotifyRequestDocument;

import com.leidos.xchangecore.core.infrastructure.service.NotificationService;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;
import com.leidos.xchangecore.core.infrastructure.util.ServletUtil;
import com.saic.precis.x2009.x06.base.IdentificationListType;
import com.saic.precis.x2009.x06.base.IdentificationType;

/**
 * The XchangeCore system Notification Service provides a WS-Notification (WSN) compliant service
 * interface to provide push and pull based notifications to XchangeCore Notification Consumers (XchangeCore
 * clients). The situations that XchangeCore may notify Notification Consumers about are:
 * <ul>
 * <li>Changes to work products that an applied resource profile has expressed interest in
 * <li>Domain specific messages
 * <li>Changes to a resource profile
 * <li>Assignment of a role in an ICS
 * <li>Changes to agreements
 * </ul>
 * <p>
 * The XchangeCore core will subscribe a XchangeCore client to the topics specified in the resource profile that
 * is applied to a XchangeCore's resource instance. Thus the XchangeCore core acts as a Subscriber as defined in
 * WSN. The XchangeCore core does not supply a NotificationProducer subscribe operation or a
 * SubscriptionManager interface for clients to directly subscribe or manage subscriptions. All
 * subscription changes should be initiated by manipulating the appropriate resource profile.
 * <p>
 * A single pull point will be created by the XchangeCore core for each XchangeCore resource instance when it is
 * created. The URL for that pull point will be contained in the resource instance data. Clients may
 * then retrieve their resource instance to obtain the pull point address. The pull point only
 * implements the GetMessages operation of the PullPoint interface not the DestroyPullPoint
 * operation. Pull points will be destroyed if the resource instance is unregistered through the
 * Resource Instance Service. The XchangeCore notification service also does not implement the
 * CreatePullPoint interface because XchangeCore manages pullpoints through resource instance creation.
 * 
 * An additional operation, GetMatchingMessages is provided to get a list of the work products that
 * match the input resource instance's current subscriptions. This method allows an endpoint to get
 * a clean view of what it would be notified about if it had been in existance when all of the
 * current work products were created or updated. Only the latest notification per work product will
 * be included in the list.
 * <p>
 * A XchangeCore client can become a push client by setting the endpoint for the resource instance
 * associated with the client using the updateEndpoint operation of the Resource Instance Service.
 * The endpoint must implement the WSN Notify interface or be an XMPP client. WSN Notify endpoints
 * should be an http url to the service. XMPP clients should be of the form xmpp:<JID> (i.e.
 * xmpp:alice@wonderland.lit).
 * 
 * The notification will be delivered to XMPP clients as an XMPP message. The body will be a short
 * summary of the work product type, the name of the associated incident, who updated it, and when.
 * The full notification will be a sub-element of the XMPP message element.
 * <p>
 * Interests in the XchangeCore resource profiles are expressed using the WS-Topics Full Topic Expression
 * Language. Notifications for domain specific work products such as Alerts, Incidents, and Maps in
 * the Emergency Management domain may also be subscribed to in this manner.
 * 
 * The base set of topics in the XchangeCore TopicSpace are:
 * <ul>
 * <li>profile
 * <li>agreement
 * <li>workproduct
 * <li>specific XchangeCore work product types as defined in the domain's services
 * </ul>
 * 
 * The following are examples of TopicExpressions that can be used in profiles:
 * <p/>
 * 
 * Notification of a create/update/close/archive of an incident:
 * <ul>
 * <li>Incident*
 * </ul>
 * Notification of all Alert work products for incidents that a endpoint has been notified about:
 * <ul>
 * <li>alert/incident/*
 * </ul>
 * Notification of all work products associated with incident 23456
 * <ul>
 * <li>incident/23456
 * </ul>
 * Notification of new or updated Alert work products for incident 123456:
 * <ul>
 * <li>alert/incident/123456
 * </ul>
 * Notifications for a specific work product (work product id = 98765)
 * <ul>
 * <li>workproduct/98765
 * </ul>
 * Notification of all Alert work products regardless of what incident they are associated with.
 * This pattern can be used for any work product type that any of the XchangeCore domain services produce
 * or that clients submit as direct work products.
 * <ul>
 * <li>alert</li>
 * </ul>
 * <p>
 * As a second example a client may submit a binary of a pdf with the work product type sent to
 * "application/pdf". All clients with an interest set to the following will receive notifications
 * about that work product:
 * <ul>
 * <li>application/pdf</li>
 * </ul>
 * Notification about changes to a profile (profile IncidentManagement):
 * <ul>
 * <li>profile/IncidentManagement
 * </ul>
 * Notification about changes to an agreement for the named core:
 * <ul>
 * <li>agreement/XchangeCore@other.core.org
 * </ul>
 * <p>
 * 
 * A single Notification Message may contain a single WorkProduct, WorkProductDeletedNotification,
 * and EDXL-DE elements as shown in in the following example XML snippet:
 * 
 * <pre>
 * &lt;not:GetMessagesResponse xmlns:not="http://uicds.org/NotificationService">
 *     &lt;not:NotificationMessage>
 *       &lt;b:Message xmlns:b="http://docs.oasis-open.org/wsn/b-2">
 *         &lt;str:WorkProduct xmlns:str="http://www.saic.com/precis/2009/06/structures">
 *         ...
 *         &lt;/str:WorkProduct>
 *       &lt;/b:Message>
 *     &lt;/not:NotificationMessage>
 *     &lt;not:NotificationMessage>
 *       &lt;b:Message xmlns:b="http://docs.oasis-open.org/wsn/b-2">
 *         &lt;not:WorkProductDeletedNotification>
 *         ...
 *         &lt;/ not:WorkProductDeletedNotification >
 *       &lt;/b:Message>
 *     &lt;/not:NotificationMessage>
 *     &lt;not:NotificationMessage>
 *       &lt;b:Message xmlns:b="http://docs.oasis-open.org/wsn/b-2">
 *         &lt;edxlde:EDXLDistribution>
 *         ...
 *         &lt;/edxlde:EDXLDistribution>
 *       &lt;/b:Message>
 *   &lt;/not:GetMessagesResponse>
 * </pre>
 * 
 * @author Nathan Lewnes
 * @see <a href="../../wsdl/NotificationService.wsdl">Appendix: NotificationService.wsdl</a>
 * @see <a href="../../services/Notification/0.1/NotificationService.xsd">Appendix:
 *      NotificationService.xsd</a>
 * @see <a href="http://www.oasis-open.org/committees/tc_home.php?wg_abbrev=wsn">OASIS Web Services
 *      Notification Specification</a>
 * @see com.leidos.xchangecore.core.infrastructure.endpoint.ResourceProfileServiceEndpoint
 * @see com.leidos.xchangecore.core.infrastructure.endpoint.ResourceInstanceServiceEndpoint
 * @idd
 */
@Endpoint
public class NotificationServiceEndpoint
    implements MessageEndpoint, ServiceNamespaces {

    @Autowired
    NotificationService notificationService;

    public void setNotificationService(NotificationService p) {

        notificationService = p;
    }

    Logger log = LoggerFactory.getLogger(NotificationServiceEndpoint.class);

    /**
     * Retrieve one queued notification from an entity.
     * 
     * @param GetCurrentMessageRequestDocument
     * 
     * @return GetCurrentMessageResponseDocument
     * @see <a href="../../services/Notification/0.1/NotificationService.xsd">Appendix:
     *      NotificationService.xsd</a>
     * @idd
     * @deprecated
     */
    @Deprecated
    @PayloadRoot(namespace = NS_NotificationService, localPart = "GetCurrentMessageRequest")
    public GetCurrentMessageResponseDocument GetCurrentMessage(GetCurrentMessageRequestDocument request) {

        log.debug("GetCurrentMessage: " + request.toString());

        GetCurrentMessageResponseDocument response = GetCurrentMessageResponseDocument.Factory.newInstance();
        response.addNewGetCurrentMessageResponse();

        // get the entity id from the to element
        XmlObject[] to = request.getGetCurrentMessageRequest().selectChildren("http://docs.oasis-open.org/wsn/b-2",
            "Topic");
        if (to.length > 0) {
            log.debug("GetCurrentMessageRequest: to: " + to[0].toString());
            XmlCursor cursor = to[0].newCursor();
            // log.debug("token type: "+cursor.currentTokenType());
            String topicName = "";
            while (cursor.currentTokenType() != XmlCursor.TokenType.ENDDOC) {
                // log.debug("token type: "+cursor.currentTokenType());
                if (cursor.currentTokenType() == XmlCursor.TokenType.TEXT) {
                    // log.debug("chars: " + cursor.getChars());
                    topicName = cursor.getChars().trim();
                    break;
                }
                cursor.toNextToken();
            }
            cursor.dispose();

            String jid = "admin";
            log.debug("getCurrentMessage: use jid whenever it's available" + jid);
            NotificationMessageHolderType notificationMessage = notificationService.getCurrentMessage(new QName(topicName),
                jid);
            if (notificationMessage != null) {
                response.getGetCurrentMessageResponse().set(notificationMessage.getMessage());
            }
        }

        return response;
    }

    /**
     * Retrieve queued notifications for a resource instance.
     * 
     * @param GetMessagesRequestDocument
     * 
     * @return GetMessagesResponseDocument
     * @see <a href="../../services/Notification/0.1/NotificationService.xsd">Appendix:
     *      NotificationService.xsd</a>
     * @idd
     */
    @PayloadRoot(namespace = NS_NotificationService, localPart = "GetMessagesRequest")
    public GetMessagesResponseDocument GetMessages(GetMessagesRequestDocument request) {

        GetMessagesResponseDocument response = GetMessagesResponseDocument.Factory.newInstance();
        response.addNewGetMessagesResponse();

        String entityID = ServletUtil.getPrincipalName();
        log.debug("GetMessageRequest for " + entityID);

        NotificationMessageHolderType[] notificationMessageArray = notificationService.getMessages(entityID,
            1);
        if (notificationMessageArray != null && notificationMessageArray.length > 0) {
            response.getGetMessagesResponse().setNotificationMessageArray(notificationMessageArray);
        }

        return response;
    }

    /**
     * Notify a resource instance with notification messages.
     * 
     * @param NotifyRequestDocument
     * @see <a href="../../services/Notification/0.1/NotificationService.xsd">Appendix:
     *      NotificationService.xsd</a>
     * 
     */
    @PayloadRoot(namespace = NS_NotificationService, localPart = "NotifyRequest")
    public void Notify(NotifyRequestDocument request) {

        if (log.isDebugEnabled()) {
            log.debug("Notify: ");
        }
        // NotifyRequestDocument response =
        // NotifyRequestDocument.Factory.newInstance();
        // response.addNewNotifyRequest();

        // get the entity id from the to element
        XmlObject[] to = request.getNotifyRequest().selectChildren("http://www.w3.org/2005/08/addressing",
            "To");
        if (to.length > 0) {
            // log.debug("found "+to[0].toString());
            XmlCursor cursor = to[0].newCursor();
            // log.debug("token type: "+cursor.currentTokenType());
            String entityID = "";
            while (cursor.currentTokenType() != XmlCursor.TokenType.ENDDOC) {
                // log.debug("token type: "+cursor.currentTokenType());
                if (cursor.currentTokenType() == XmlCursor.TokenType.TEXT) {
                    // log.debug("chars: "+cursor.getChars());
                    entityID = cursor.getChars().substring(cursor.getChars().lastIndexOf("/") + 1);
                    break;
                }
                cursor.toNextToken();
            }
            cursor.dispose();

            NotificationMessageHolderType[] msgArray = request.getNotifyRequest().getNotificationMessageArray();
            log.debug("msgArray size: " + msgArray.length);

            // send notification messages to entityID
            notificationService.notify(entityID, msgArray);
        }

    }

    /**
     * Get a list of notifications by evaluating the interests against the current work products.
     * This method allows a client that is starting with a new resource instance to get the current
     * state of the core with respect to its interests. This list contains notifications for the
     * work products the client would have been notified about if it had the endpoint registered
     * when that work product was created or updated.
     * 
     * @param GetMatchingMessagesRequestDocument
     * 
     * @return GetMatchingMessagesResponseDocument list of notifications
     * @see <a href="../../services/Notification/0.1/NotificationService.xsd">Appendix:
     *      NotificationService.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_NotificationService, localPart = "GetMatchingMessagesRequest")
    public GetMatchingMessagesResponseDocument getMatchingMessages(GetMatchingMessagesRequestDocument request)
        throws DatatypeConfigurationException {

        GetMatchingMessagesResponseDocument responseDoc = GetMatchingMessagesResponseDocument.Factory.newInstance();
        GetMatchingMessagesResponse response = responseDoc.addNewGetMatchingMessagesResponse();
        log.debug("getMatchingMessages: requested user: " +
                  request.getGetMatchingMessagesRequest().getID().getStringValue() +
                  ", real user: " + ServletUtil.getPrincipalName());
        // IdentificationType[] identificationArray = notificationService.getMatchingMessages(request.getGetMatchingMessagesRequest().getID().getStringValue());
        IdentificationType[] identificationArray = notificationService.getMatchingMessages(ServletUtil.getPrincipalName());
        IdentificationListType workProductIdentificationList = response.addNewWorkProductIdentificationList();
        workProductIdentificationList.setIdentificationArray(identificationArray);

        return responseDoc;
    }

    @Override
    public void invoke(MessageContext arg0) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("GOT MESSAGE CONTEXT: " + arg0);
        }
    }

}
