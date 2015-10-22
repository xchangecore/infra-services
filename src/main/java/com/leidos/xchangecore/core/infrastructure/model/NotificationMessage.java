package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The NotificationMessage data model.
 *
 * @ssdd
 */
@Entity
public class NotificationMessage
    implements Serializable {

    private static final long serialVersionUID = 6530136814536038579L;

    /**
     *
     */
    @Id
    @Column(name = "NOTIFICATION_MESSAGE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "SUBSCRIPTION_ID")
    @Field(index = Index.TOKENIZED)
    private Integer subscriptionID;

    @Column(name = "MESSAGE_TYPE")
    @Field(index = Index.TOKENIZED)
    private String type;

    @Column(name = "MESSAGE_STRING")
    @Lob
    private byte[] message;

    @ManyToOne(targetEntity = Notification.class)
    @JoinColumn(name = "NOTIFICATION_ID", nullable = false)
    private Notification notification;

    // public boolean equals(Object obj) {
    // NotificationMessage msgObj = (NotificationMessage) obj;
    // String hash = type + subscriptionID;
    // String msgObjHash = msgObj.getType() + msgObj.getSubscriptionID();
    // return (hash.equals(msgObjHash));
    // }

    // public int hashCode() {
    // String hash = "" + subscriptionID;
    // return hash.hashCode();
    // }

    /**
     * Gets the message id.
     *
     * @return the message id
     * @ssdd
     */
    public Integer getId() {

        return id;
    }

    /**
     * Gets the message.
     *
     * @return the message
     * @ssdd
     */
    public byte[] getMessage() {

        return message;
    }

    /**
     * Gets the notification.
     *
     * @return the notification
     * @ssdd
     */
    public Notification getNotification() {

        return notification;
    }

    /**
     * Gets the subscription id.
     *
     * @return the subscription id
     * @ssdd
     */
    public Integer getSubscriptionID() {

        return subscriptionID;
    }

    /**
     * Gets the type.
     *
     * @return the type
     * @ssdd
     */
    public String getType() {

        return type;
    }

    /**
     * Sets the message id.
     *
     * @param messageID the new message id
     * @ssdd
     */
    public void seId(Integer id) {

        this.id = id;
    }

    /**
     * Sets the message.
     *
     * @param message the new message
     * @ssdd
     */
    public void setMessage(byte[] message) {

        this.message = message;
    }

    /**
     * Sets the notification
     *
     * @param notification - the new notification
     * @ssdd
     */

    public void setNotification(Notification notification) {

        this.notification = notification;
    }

    /**
     * Sets the subscription id.
     *
     * @param subscriptionID the new subscription id
     * @ssdd
     */
    public void setSubscriptionID(Integer subscriptionID) {

        this.subscriptionID = subscriptionID;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     * @ssdd
     */
    public void setType(String type) {

        this.type = type;
    }

}
