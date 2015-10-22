package com.leidos.xchangecore.core.infrastructure.service;

import javax.xml.namespace.QName;

import org.oasisOpen.docs.wsn.b2.FilterType;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.springframework.transaction.annotation.Transactional;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import com.leidos.xchangecore.core.infrastructure.exceptions.EmptySubscriberNameException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductIDException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductTypeException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NullSubscriberException;

/**
 * WS-Notification v1.3 Interface
 *
 * @see com.leidos.xchangecore.core.endpoint.NotificationServiceEndpoint
 * @ssdd
 *
 */
@Transactional
public interface NotificationProducer {

    public static final String WSN_NAMESPACE = "docs.oasis-open.org/wsn/b-2";
    public static final String WST_NAMESPACE = "http://docs.oasis-open.org/wsn/t-1";
    public static final String WSN_PREFIX = "wsnt";
    public static final String WST_PREFIX = "wst";
    public static final String NOTIFY_NAMESPACE = "docs.oasis-open.org/wsn/b-2";
    public static final String TOPIC_NAMESPACE_URI = "http://docs.oasis-open.org/wsn/t-1";
    public static final QName FIXED_SET_QNAME = new QName(WSN_NAMESPACE,
                                                          "FixedTopicSet",
                                                          WST_PREFIX);
    public static final QName TOPIC_DIALECT_QNAME = new QName(WSN_NAMESPACE,
                                                              "TopicExpressionDialect",
                                                              WSN_PREFIX);
    public static final QName TOPIC_EXPRESSION_QNAME = new QName(WSN_NAMESPACE,
                                                                 "TopicExpression",
                                                                 WSN_PREFIX);
    public static final QName TOPIC_SET_QNAME = new QName(WST_NAMESPACE, "TopicSet", WST_PREFIX);

    QName[] PROPERTIES = new QName[] {
        FIXED_SET_QNAME,
        TOPIC_DIALECT_QNAME,
        TOPIC_EXPRESSION_QNAME,
        TOPIC_SET_QNAME
    };

    /**
     * Retrieve the last message published for the topicName provided.
     *
     * @param The
     *            Topic QN
     * @return The last NotificationMessage published on the topic
     * @ssdd
     */
    public NotificationMessageHolderType getCurrentMessage(QName tp, String jid);// throws

    /**
     * subscribe to receive events that are published to a topic
     *
     * @param who
     *            The EPR to send the notifications TO
     * @param what
     *            filter to set criteria for publication, like topic names,message patterns
     * @ssdd
     */
    public void subscribe(EndpointReferenceType who, FilterType what)
        throws InvalidProductTypeException, NullSubscriberException, EmptySubscriberNameException,
        InvalidProductIDException;
}