package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The ProductSubscriptionByType data model.
 *
 * @ssdd
 */
@Entity
// @Table(name = "PRODUCT_SUBSCRIPTION_BY_TYPE")
public class ProductSubscriptionByType
    implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PRODUCT_TYPE")
    @Field(index = Index.TOKENIZED)
    private String productType;

    @Column(name = "INTEREST_GROUP_ID")
    @Field(index = Index.TOKENIZED)
    private String interestGroupID;

    @Column(name = "XPATH")
    @Field(index = Index.TOKENIZED)
    private String xPath;

    @Column(name = "SUBSCRIBER_NAME")
    @Field(index = Index.TOKENIZED)
    private String subscriberName;

    // Randomly generated subscription ID
    @Column(name = "SUBSCRIPTION_ID")
    @Field(index = Index.TOKENIZED)
    private Integer subscriptionId;

    // nameSpaceMap
    // key: prefix
    // value: namespace. //also named as URI

    @OneToMany(cascade = {
        CascadeType.ALL
    }, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<NamespaceMap> namespacemap = new HashSet<NamespaceMap>();

    public ProductSubscriptionByType() {

    }

    /**
     * Instantiates a new product subscription by type.
     *
     * @param productType the product type
     * @param interestGroupID the interest group id
     * @param xPath the x path
     * @param subscriberName the subscriber name
     * @param subscriptionId the subscription id
     * @param namespaceMap the namespace map
     * @ssdd
     */
    public ProductSubscriptionByType(String productType, String interestGroupID, String xPath,
                                     String subscriberName, Integer subscriptionId,
                                     Set<NamespaceMap> namespaceMap) {

        setProductType(productType);
        setInterestGroupID(interestGroupID);
        setXPath(xPath);
        setSubscriberName(subscriberName);
        setSubscriptionId(subscriptionId);
        setNamespacemap(namespaceMap);
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
     * Gets the interest group id.
     *
     * @return the interest group id
     * @ssdd
     */
    public String getInterestGroupID() {

        return interestGroupID;
    }

    public Set<NamespaceMap> getNamespacemap() {

        return namespacemap;
    }

    public Map<String, String> getNamespaceMap() {

        final HashMap<String, String> map = new HashMap<String, String>();
        for (final NamespaceMap namespace : namespacemap) {
            map.put(namespace.getPrefix(), namespace.getUri());
        }
        return map;
    }

    /**
     * Gets the product type.
     *
     * @return the product type
     * @ssdd
     */
    public String getProductType() {

        return productType;
    }

    /**
     * Gets the subscriber name.
     *
     * @return the subscriber name
     * @ssdd
     */
    public String getSubscriberName() {

        return subscriberName;
    }

    /**
     * Gets the subscription id.
     *
     * @return the subscription id
     * @ssdd
     */
    public Integer getSubscriptionId() {

        return subscriptionId;
    }

    /**
     * Gets the x path.
     *
     * @return the x path
     * @ssdd
     */
    public String getXPath() {

        return xPath;
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
     * Sets the interest group id.
     *
     * @param interestGroupID the new interest group id
     * @ssdd
     */
    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    public void setNamespacemap(Set<NamespaceMap> namespacemap) {

        this.namespacemap = namespacemap;
    }

    /**
     * Sets the product type.
     *
     * @param type the new product type
     * @ssdd
     */
    public void setProductType(String type) {

        productType = type;
    }

    /**
     * Sets the subscriber name.
     *
     * @param subscriberName the new subscriber name
     * @ssdd
     */
    public void setSubscriberName(String subscriberName) {

        this.subscriberName = subscriberName;
    }

    /**
     * Sets the subscription id.
     *
     * @param subscriptionId the new subscription id
     * @ssdd
     */
    public void setSubscriptionId(Integer subscriptionId) {

        this.subscriptionId = subscriptionId;
    }

    /**
     * Sets the x path.
     *
     * @param xPath the new x path
     * @ssdd
     */
    public void setXPath(String xPath) {

        this.xPath = xPath;
    }
}
