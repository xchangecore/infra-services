package com.leidos.xchangecore.core.infrastructure.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * The PublishedProduct data model.
 *
 * @ssdd
 */
@Entity
// @Table(name = "PUBLISHED_PRODUCT")
public class PublishedProduct {

    @Id
    @Column(name = "PUBLISHED_PRODUCT_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "PRODUCT_TYPE")
    private String productType;

    @ManyToOne(targetEntity = RegisteredService.class)
    @JoinColumn(name = "REGISTERED_SERVICE_ID", nullable = false)
    private RegisteredService publisher;

    public PublishedProduct() {

    }

    /**
     * Instantiates a new published product.
     *
     * @param productType the product type
     */
    public PublishedProduct(String productType) {

        this.productType = productType;
    }

    @Override
    public boolean equals(Object obj) {

        final PublishedProduct pubObj = (PublishedProduct) obj;
        return productType.equals(pubObj.getProductType());
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
     * Gets the publisher.
     *
     * @return the publisher
     * @ssdd
     */
    public RegisteredService getPublisher() {

        return publisher;
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
     * Sets the publisher.
     *
     * @param publisher the new publisher
     * @ssdd
     */
    public void setPublisher(RegisteredService publisher) {

        this.publisher = publisher;
    }
}
