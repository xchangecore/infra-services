package com.leidos.xchangecore.core.infrastructure.service;

import com.leidos.xchangecore.core.infrastructure.exceptions.EmptyCoreNameListException;
import com.leidos.xchangecore.core.infrastructure.exceptions.LocalCoreNotOnlineException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareRuleInAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnavailableException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnknownException;
import com.leidos.xchangecore.core.infrastructure.messages.Core2CoreMessage;

/**
 * The CommunicationsService interface provides the mechanism for XchangeCore services to send
 * messages to a remote core.  The messages are handled by the CommunicationsService
 * at the receiver and dispatched to the correct message handler based on message type.
 *
 * @author Aruna Hau
 * @since 1.0
 * @ssdd
 *
 */
public interface CommunicationsService {

    public enum CORE2CORE_MESSAGE_TYPE {
        RESOURCE_MESSAGE, BROADCAST_MESSAGE, XMPP_MESSAGE
    }

    public static final String COMMUNICATIONS_SERVICE_NAME = "CommunicationsService";
    public static final String UICDSExplicitAddressScheme = "uicds:user";
    public static final String UICDSCoreAddressScheme = "uicds:core";

    public static final String XMPPAddressScheme = "xmpp";

    /**
     * Handles notifications of a Core to Core message received from a remote core and
     * dispatches to the correct handler based on message type.
     *
     * @param message
     * @see Core2CoreMessage
     * @ssdd
     */
    public void core2CoreMessageNotificationHandler(Core2CoreMessage message);

    /**
     * Sends a message to a remote core.
     *
     * @param message The string message
     * @param messageType The type of message
     * @param hostName JID of the receiving core (e.g. XchangeCore@XchangeCore-test1.leidos.com)
     * @throws LocalCoreNotOnlineException
     * @throws NoShareRuleInAgreementException
     * @throws NoShareAgreementException
     * @throws EmptyCoreNameListException
     * @ssdd
     */
    public void sendMessage(String message, CORE2CORE_MESSAGE_TYPE messageType, String hostName)
        throws IllegalArgumentException, RemoteCoreUnknownException,
        RemoteCoreUnavailableException, LocalCoreNotOnlineException, NoShareAgreementException,
        NoShareRuleInAgreementException;

    /**
     * Sends an XMPP message to a specific JID
     *
     * @param body Standard XMPP Message body text
     * @param xhtml XHTML version of the body (maybe null)
     * @param xml XML version of the body (maybe null);
     */
    public void sendXMPPMessage(String body, String xhtml, String xml, String jid);

}
