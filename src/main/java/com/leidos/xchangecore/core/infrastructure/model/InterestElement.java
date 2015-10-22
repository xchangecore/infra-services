package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The InterestElement data model.
 *
 * @ssdd
 */

@Entity
// @Table(name = "INTEREST")
public class InterestElement
    implements Serializable {

    private static final long serialVersionUID = -231519527018186603L;

    @Id
    @Column(name = "INTEREST_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "TOPIC_EXPRESSION")
    @Field(index = Index.TOKENIZED)
    private String topicExpression;

    @Column(name = "MESSAGE_CONTENT")
    @Field(index = Index.TOKENIZED)
    private String messageContent;

    @OneToMany(targetEntity = InterestNamespaceType.class, cascade = CascadeType.ALL)
    @Cascade({
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN
    })
    private Set<InterestNamespaceType> namespaces = new HashSet<InterestNamespaceType>();

    public InterestElement() {

        // No arg constructor
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InterestElement other = (InterestElement) obj;
        if (messageContent == null) {
            if (other.messageContent != null) {
                return false;
            }
        } else if (!messageContent.equals(other.messageContent)) {
            return false;
        }
        if (namespaces == null) {
            if (other.namespaces != null) {
                return false;
            }
        } else if (!namespaces.equals(other.namespaces)) {
            return false;
        }
        if (topicExpression == null) {
            if (other.topicExpression != null) {
                return false;
            }
        } else if (!topicExpression.equals(other.topicExpression)) {
            return false;
        }
        return true;
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
     * Gets the message content.
     *
     * @return the message content
     * @ssdd
     */
    public String getMessageContent() {

        return messageContent;
    }

    /**
     * Gets the namespaces.
     *
     * @return the namespaces
     * @ssdd
     */
    public Set<InterestNamespaceType> getNamespaces() {

        return namespaces;
    }

    /**
     * Gets the topic expression.
     *
     * @return the topic expression
     * @ssdd
     */
    public String getTopicExpression() {

        return topicExpression;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((messageContent == null) ? 0 : messageContent.hashCode());
        result = (prime * result) + ((namespaces == null) ? 0 : namespaces.hashCode());
        result = (prime * result) + ((topicExpression == null) ? 0 : topicExpression.hashCode());
        return result;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     * @ssdd
     */
    public void setId(Integer id) {

        this.id = id;
    }

    /**
     * Sets the message content.
     *
     * @param messageContent the new message content
     * @ssdd
     */
    public void setMessageContent(String messageContent) {

        this.messageContent = messageContent;
    }

    /**
     * Sets the namespaces.
     *
     * @param namespaces the new namespaces
     * @ssdd
     */
    public void setNamespaces(Set<InterestNamespaceType> namespaces) {

        this.namespaces = namespaces;
    }

    /**
     * Sets the topic expression.
     *
     * @param topicExpression the new topic expression
     * @ssdd
     */
    public void setTopicExpression(String topicExpression) {

        this.topicExpression = topicExpression;
    }
}
