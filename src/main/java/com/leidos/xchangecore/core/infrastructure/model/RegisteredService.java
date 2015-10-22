package com.leidos.xchangecore.core.infrastructure.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

/**
 * The RegisteredService data model.
 *
 * @ssdd
 */
@Entity
// @Table(name = "REGISTERED_SERVICE")
public class RegisteredService {

    public enum SERVICE_TYPE {
        UICDS, EXTERNAL
    }

    @Id
    @Column(name = "REGISTERED_SERVICE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "URN")
    private String urn;

    @Column(name = "SERVICE_NAME")
    private String serviceName;;

    @Column(name = "SERVICE_TYPE")
    private SERVICE_TYPE serviceType;

    @Column(name = "CORE_NAME")
    private String coreName;

    @OneToMany(cascade = {
        CascadeType.ALL
    }, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private final Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();

    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE,
        CascadeType.REMOVE
    })
    private final Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();

    public RegisteredService() {

    }

    /**
     * Instantiates a new registered service.
     *
     * @param urn the urn
     * @param serviceName the service name
     * @param serviceType the service type
     * @param coreName the core name
     * @param publishedProducts the published products
     * @param subscribedProducts the subscribed products
     * @ssdd
     */
    public RegisteredService(String urn, String serviceName, SERVICE_TYPE serviceType,
                             String coreName, Set<PublishedProduct> publishedProducts,
                             Set<SubscribedProduct> subscribedProducts) {

        setURN(urn);
        setServiceName(serviceName);
        setServiceType(serviceType);
        setCoreName(coreName);
        setPublishedProducts(publishedProducts);
        setSubscribedProducts(subscribedProducts);
    }

    @Override
    public boolean equals(Object obj) {

        final RegisteredService svcObj = (RegisteredService) obj;
        return serviceName.equals(svcObj.getServiceName());
    }

    /**
     * Gets the core name.
     *
     * @return the core name
     * @ssdd
     */
    public String getCoreName() {

        return coreName;
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
     * Gets the published products.
     *
     * @return the published products
     * @ssdd
     */
    public Set<PublishedProduct> getPublishedProducts() {

        return publishedProducts;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     * @ssdd
     */
    public String getServiceName() {

        return serviceName;
    }

    /**
     * Gets the service type.
     *
     * @return the service type
     * @ssdd
     */
    public SERVICE_TYPE getServiceType() {

        return serviceType;
    }

    /**
     * Gets the subscribed products.
     *
     * @return the subscribed products
     * @ssdd
     */
    public Set<SubscribedProduct> getSubscribedProducts() {

        return subscribedProducts;
    }

    /**
     * Gets the uRN.
     *
     * @return the uRN
     * @ssdd
     */
    public String getURN() {

        return urn;
    }

    @Override
    public int hashCode() {

        return serviceName.hashCode();
        // return 42;
    }

    /**
     * Sets the core name.
     *
     * @param coreName the new core name
     * @ssdd
     */
    public void setCoreName(String coreName) {

        this.coreName = coreName;
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
     * Sets the published products.
     *
     * @param publishedProducts the new published products
     * @ssdd
     */
    public void setPublishedProducts(Set<PublishedProduct> publishedProducts) {

        for (final PublishedProduct product : publishedProducts) {
            product.setPublisher(this);
            this.publishedProducts.add(product);
        }
    }

    /**
     * Sets the service name.
     *
     * @param serviceName the new service name
     * @ssdd
     */
    public void setServiceName(String serviceName) {

        this.serviceName = serviceName;
    }

    /**
     * Sets the service type.
     *
     * @param serviceType the new service type
     * @ssdd
     */
    public void setServiceType(SERVICE_TYPE serviceType) {

        this.serviceType = serviceType;
    }

    /**
     * Sets the subscribed products.
     *
     * @param subscribedProducts the new subscribed products
     * @ssdd
     */
    public void setSubscribedProducts(Set<SubscribedProduct> subscribedProducts) {

        for (final SubscribedProduct product : subscribedProducts) {
            product.getSubscribers().add(this);
            this.subscribedProducts.add(product);
        }
    }

    /**
     * Sets the uRN.
     *
     * @param urn the new uRN
     * @ssdd
     */
    public void setURN(String urn) {

        this.urn = urn;
    }

}
