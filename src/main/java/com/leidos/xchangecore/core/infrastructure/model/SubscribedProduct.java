package com.leidos.xchangecore.core.infrastructure.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

/**
 * The Subscribed Product data model.
 * @ssdd
 */
@Entity
// @Table(name = "SUBSCRIBED_PRODUCT")
public class SubscribedProduct {

    @Id
    @Column(name = "SUBSCRIBED_PRODUCT_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "PRODUCT_TYPE")
    private String productType;

    @ManyToMany(targetEntity = RegisteredService.class)
    @JoinTable(name = "PRODUCT_SUBSCRIBERS", joinColumns = {
        @JoinColumn(name = "SUBSCRIBED_PRODUCT_ID")
    }, inverseJoinColumns = {
        @JoinColumn(name = "REGISTERED_SERVICE_ID")
    })
    private Set<RegisteredService> subscribers = new HashSet<RegisteredService>();

    public SubscribedProduct() {

    }

    /**
     * Instantiates a new subscribed product.
     *
     * @param productType the product type
     * @ssdd
     */
    public SubscribedProduct(String productType) {

        setProductType(productType);
    }

    @Override
    public boolean equals(Object obj) {

        final SubscribedProduct subObj = (SubscribedProduct) obj;
        return productType.equals(subObj.getProductType());
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
     * Gets the product type.
     *
     * @return the product type
     * @ssdd
     */
    public String getProductType() {

        return productType;
    }

    /**
     * Gets the subscribers.
     *
     * @return the subscribers
     * @ssdd
     */
    public Set<RegisteredService> getSubscribers() {

        return subscribers;
    }

    @Override
    public int hashCode() {

        return productType.hashCode();
        // return 42;

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
     * Sets the product type.
     *
     * @param productType the new product type
     * @ssdd
     */
    public void setProductType(String productType) {

        this.productType = productType;
    }

    /**
     * Sets the subscribers.
     *
     * @param subscribers the new subscribers
     * @ssdd
     */
    public void setSubscribers(Set<RegisteredService> subscribers) {

        this.subscribers = subscribers;
    }

}
