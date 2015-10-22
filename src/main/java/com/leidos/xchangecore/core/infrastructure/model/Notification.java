package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import com.leidos.xchangecore.core.infrastructure.dao.hb.NotificationMessageDAOHibernate;

/**
 * The Notification data model.
 *
 * @ssdd
 */
@Entity
// @Table(name = "NOTIFICATION")
public class Notification
    implements Serializable {

    private static final Logger logger = Logger.getLogger(Notification.class);
    public static final String NOTIFICATION_PRODUCT_TYPE = "Notification";
    private static final long serialVersionUID = -7000808870449438458L;

    @Column(name = "ENDPOINT_URL")
    @Field(index = Index.TOKENIZED)
    private String endpointURL;

    @Column(name = "ENDPOINT_WS")
    @Field(index = Index.TOKENIZED)
    private boolean endpointWS; // is the endpoint a webserviceURL

    @Column(name = "ENTITY_ID")
    @Field(index = Index.TOKENIZED)
    private String entityID;

    @Id
    @Column(name = "NOTIFICATION_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(cascade = {
        CascadeType.ALL
    }, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private final Set<NotificationMessage> messages = new LinkedHashSet<NotificationMessage>();

    //FLi modified on 11/29/2011
    private int msgCount;
    @OneToMany(cascade = {
        CascadeType.ALL
    }, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private final Set<NotificationSubscription> subscriptions = new HashSet<NotificationSubscription>();

    /**
     * Adds the message.
     *
     * @param subscriptionID
     *            the subscription id
     * @param msgType
     *            the msg type
     * @param message
     *            the message
     * @ssdd
     */
    public void addMessage(Integer subscriptionID, String msgType, String message) {

        logger.debug("addMessage: msgType: " + msgType);
        final NotificationMessage msg = new NotificationMessage();
        msg.setNotification(this);
        msg.setMessage(message.getBytes());
        msg.setType(msgType);
        msg.setSubscriptionID(subscriptionID);
        messages.add(msg);
        logger.debug("addMessage: message count: " + messages.size());
    }

    /**
     * Adds the subscription.
     *
     * @param subscription
     *            the subscription
     * @ssdd
     */
    public void addSubscription(NotificationSubscription subscription) {

        subscription.setNotification(this);
        subscriptions.add(subscription);
    }

    /*
     * To remove the NotificationMessage from the Set. If it's successful then removed it from
     * database.
     */
    private void clearMessage(NotificationMessage message) {

        if (messages.remove(message)) {
            NotificationMessageDAOHibernate.getInstance().makeTransient(message);
        }
    }

    /**
     * Clear messages.
     *
     * @ssdd
     */
    public void clearMessages() {

        // removes all associated notifMessages
        final Set<NotificationMessage> messageSet = new HashSet<NotificationMessage>(messages);
        for (final NotificationMessage message : messageSet) {
            clearMessage(message);
        }
    }

    /**
     * Clear messages of old product versions.
     *
     * @ssdd
     */
    public void clearoldMessage(String messageType) {

        if (!"WorkProductDeleted".equals(messageType)) {
            final Set<NotificationMessage> messages = getMessages();
            if (!messages.isEmpty()) {
                for (final NotificationMessage message : messages) {
                    if (messageType.equals(message.getType())) {
                        clearMessage(message);
                        break;

                    }
                }
            }
        }

    }

    @Override
    public boolean equals(Object obj) {

        final Notification notificationObj = (Notification) obj;
        return entityID.equals(notificationObj.getEntityID());
    }

    /**
     * Gets the endpoint url.
     *
     * @return the endpoint url
     * @ssdd
     */
    public String getEndpointURL() {

        return endpointURL;
    }

    /**
     * Gets the entity id.
     *
     * @return the entity id
     * @ssdd
     */
    public String getEntityID() {

        return entityID;
    }

    /**
     * Gets the id.
     *
     * @return the id
     * @ssdd
     */
    public Integer getId() {

        return id;
    }

    /**
     * Gets the messages set.
     *
     * @return the messages set
     * @ssdd
     */
    public Set<NotificationMessage> getMessages() {

        return messages;
    }

    /**
     * Gets the messages.
     *
     * @return the messages
     * @ssdd
     */
    public Set<String> getMessagesAsString() {

        final Set<String> msgSet = new LinkedHashSet<String>();
        for (final NotificationMessage msg : messages) {
            msgSet.add(new String(msg.getMessage()));
        }
        return msgSet;
    }

    //FLi modified on 11/29/2011
    public int getMsgCount() {

        msgCount = messages.size();
        return msgCount;
    }

    /**
     * Gets the subscriptions.
     *
     * @return the subscriptions
     * @ssdd
     */
    public Set<NotificationSubscription> getSubscriptions() {

        return subscriptions;
    }

    @Override
    public int hashCode() {

        return entityID.hashCode();
    }

    /**
     * Checks if is endpoint ws.
     *
     * @return true, if is endpoint ws
     * @ssdd
     */
    public boolean isEndpointWS() {

        return endpointWS;
    }

    /**
     * Sets the endpoint url.
     *
     * @param endpointURL
     *            the new endpoint url
     * @ssdd
     */
    public void setEndpointURL(String endpointURL) {

        this.endpointURL = endpointURL;
    }

    /**
     * Sets the endpoint ws.
     *
     * @param endpointWS
     *            the new endpoint ws
     * @ssdd
     */
    public void setEndpointWS(boolean endpointWS) {

        this.endpointWS = endpointWS;
    }

    /**
     * Sets the entity id.
     *
     * @param entityID
     *            the new entity id
     * @ssdd
     */
    public void setEntityID(String entityID) {

        this.entityID = entityID;
    }

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     * @ssdd
     */
    public void setId(Integer id) {

        this.id = id;
    }

    /**
     * Sets the messages.
     *
     * @param messages
     *            the new messages
     * @ssdd
     */
    public void setMessages(Set<NotificationMessage> messages) {

        this.messages.clear();
        for (final NotificationMessage message : messages) {
            message.setNotification(this);
            this.messages.add(message);
        }

        setMsgCount(this.messages.size());;
    }

    public void setMsgCount(int msgCount) {

        this.msgCount = msgCount;
    }

    /**
     * Sets the subscriptions.
     *
     * @param subscriptions
     *            the new subscriptions
     * @ssdd
     */
    public void setSubscriptions(Set<NotificationSubscription> subscriptions) {

        this.subscriptions.clear();
        for (final NotificationSubscription subscription : subscriptions) {
            subscription.setNotification(this);
            this.subscriptions.add(subscription);
        }
    }

    @Override
    public String toString() {

        String sNotf = "";
        sNotf += "EntityID: " + entityID + "##";
        sNotf += "EndpointURL: " + endpointURL + "##";
        for (final NotificationSubscription sub : subscriptions) {
            sNotf += "SubID: " + String.valueOf(sub.getSubscriptionID()) + "##";
        }
        sNotf += "Message list size: " + messages.size() + "##";
        return sNotf;
    }

}
