package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The NotificaionSubscription data model.
 *
 * @ssdd
 */
@Entity
// @Table(name = "NOTIFICATION_SUBSCRIPTION")
public class NotificationSubscription
    implements Serializable {

    private static final long serialVersionUID = 7064547589797190026L;

    @Id
    @Column(name = "NOTIFICATION_SUBSCRIPTION_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "SUBSCRIPTION_ID")
    @Field(index = Index.TOKENIZED)
    private Integer subscriptionID;

    @ManyToOne(targetEntity = Notification.class)
    @JoinColumn(name = "NOTIFICATION_ID", nullable = false)
    private Notification notification;

    // @CollectionOfElements
    // private Set<String> messages = new LinkedHashSet<String>();

    @Override
    public boolean equals(Object obj) {

        final NotificationSubscription subObj = (NotificationSubscription) obj;
        return (subscriptionID.compareTo(subObj.getSubscriptionID()) == 0);
    }

    /**
     * Gets the subscription id.
     *
     * @return the subscription id
     * @ssdd
     */
    public Integer getId() {

        return id;
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

    public Integer getSubscriptionID() {

        return subscriptionID;
    }

    @Override
    public int hashCode() {

        final String hash = "" + subscriptionID;
        return hash.hashCode();
    }

    /**
     * Sets the subscription id.
     *
     * @param subscriptionID the new subscription id
     * @ssdd
     */
    public void setId(Integer id) {

        this.id = id;
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

    // public Set<String> getMessages() {
    // return messages;
    // }
    //
    // public void setMessages(Set<String> messages) {
    // this.messages = messages;
    // }
    //
    // public void addMessage(String message) {
    // this.messages.add(message);
    // }
    //
    // public void clearMessages() {
    // this.messages.clear();
    // this.messages = new LinkedHashSet<String>();
    // }

    public void setSubscriptionID(Integer subscriptionID) {

        this.subscriptionID = subscriptionID;
    }

}
