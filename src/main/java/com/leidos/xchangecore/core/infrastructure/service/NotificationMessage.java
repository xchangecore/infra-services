package com.leidos.xchangecore.core.infrastructure.service;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3c.dom.Element;

/**
 * A NotificationMessage is an artifact of a Situation containing information about that Situation
 * that some entity wishes to communicate to other entities.
 * <p>
 * A NotificationMessage is represented as an XML element with a Namespace qualified QName and a
 * type defined using XML Schema.
 * <p>
 * A typical usage pattern is to define a single NotificationMessage type (to be precise, its
 * defining XML element) for each kind of Situation, containing information pertinent to that kind
 * of Situation; in this case one can think of a NotificationMessage instance as in some sense being
 * (or at least representing) the Situation.
 * <p>
 * A designer could choose to associate several different NotificationMessage types with a
 * Situation, for example, describing different aspects of the Situation, destined for different
 * target recipients, etc. Conversely it is possible that several essentially different Situations
 * give rise to NotificationMessages of the same type.
 *
 * @see com.leidos.xchangecore.core.endpoint.NotificationServiceEndpoint
 *
 */
public interface NotificationMessage {

    /**
     * This method allows you to add message content
     *
     * @param what
     *            Element
     */
    public void addMessageContent(Element what);

    /**
     * This method allows you to add message content
     *
     * @param qn
     *            for QName
     * @param what
     *            Object
     */
    public void addMessageContent(QName qn, Object what); // throws fault

    /**
     * @param qn
     *            for Qname
     * @return The element Message that has that name
     */
    public Element getMessageContent(QName qn);

    /**
     * @param qn
     * @param what
     *            class
     * @return The Object that has that Qname
     */
    public Object getMessageContent(QName qname, Class<?> what); // throws fault

    /**
     *
     * @return Element names added under Messages
     */
    public Collection<?> getMessageContentNames();

    /**
     *
     * @return The EPR of who sent the message
     *
     */
    public EndpointReferenceType getProducerReference();

    /**
     *
     * @return The EPR of the subscription
     *
     */
    public EndpointReferenceType getSubscriptionReference();

    /**
     *
     * @return The qn topic that this message was published under.
     *
     */
    public QName getTopic();

    /**
     *
     * @return topic dialect string
     *
     */
    public String getTopicDialect();

    public void setProducerReference(EndpointReferenceType producer);

    public void setSubscriptionReference(EndpointReferenceType subscription);

    /**
     * @param tp
     *            (topic path)
     */
    public void setTopic(QName tp);// throws what fault?

    /**
     * @param d
     *            (topic dialect)
     */
    public void setTopicDialect(String d);// throws TopicExpressionDialectUnknownFault;
}
