package com.leidos.xchangecore.core.infrastructure.service;

import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.springframework.transaction.annotation.Transactional;

/**
 * WS-Notification v1.3 Interface
 *
 * @see com.leidos.xchangecore.core.endpoint.NotificationServiceEndpoint
 * @ssdd
 *
 */
@Transactional
public interface NotificationConsumer {

    public static final String NOTIFY_MESSAGE = "NOTIFY";
    public static final String NOTIFY_URN_MSG = "urn:uicds:notify:message";
    public static final String NOTIFY_URN_PRODUCER = "urn:uicds:notifiy:producer";

    /**
     * Send a directed notify message to a desired client with the specified array of notifications
     *
     * @param entityID
     *            is the name of the client you want to notify
     * @param notifications
     *            to be delivered to endpoint when notified
     * @ssdd
     */
    public void notify(String entityID, NotificationMessageHolderType[] notifications);// throws

}