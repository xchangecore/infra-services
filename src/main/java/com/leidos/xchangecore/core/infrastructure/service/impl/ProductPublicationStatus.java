package com.leidos.xchangecore.core.infrastructure.service.impl;

import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;

/**
 * The Class ProductPublicationStatus.
 * @see com.leidos.xchangecore.core.infrastructure.model.WorkProduct WorkProduct Data Model
 */
public class ProductPublicationStatus {

    /** The Constant SuccessStatus. */
    public static final String SuccessStatus = "Success";

    /** The Constant FailureStatus. */
    public static final String FailureStatus = "Failure";

    /** The Constant PendingStatus. */
    public static final String PendingStatus = "Pending";

    private String status;
    private WorkProduct product;
    private String reasonForFailure;
    private String act;

    /**
     * Instantiates a new product publication status.
     */
    public ProductPublicationStatus() {

    }

    /**
     * Instantiates a new product publication status.
     * 
     * @param reasonForFailure the reason for failure
     */
    public ProductPublicationStatus(String reasonForFailure) {

        super();
        this.status = ProductPublicationStatus.FailureStatus;
        this.reasonForFailure = reasonForFailure;
    }

    /**
     * Gets the status.
     * 
     * @return the status
     */
    public String getStatus() {

        return status;
    }

    /**
     * Sets the status.
     * 
     * @param status the new status
     */
    public void setStatus(String status) {

        this.status = status;
    }

    /**
     * Gets the product.
     * 
     * @return the product
     */
    public WorkProduct getProduct() {

        return product;
    }

    /**
     * Sets the product.
     * 
     * @param product the new product
     */
    public void setProduct(WorkProduct product) {

        this.product = product;
    }

    /**
     * Gets the reason for failure.
     * 
     * @return the reason for failure
     */
    public String getReasonForFailure() {

        return reasonForFailure;
    }

    /**
     * Sets the reason for failure.
     * 
     * @param reasonForFailure the new reason for failure
     */
    public void setReasonForFailure(String reasonForFailure) {

        this.reasonForFailure = reasonForFailure;
    }

    /**
     * Gets the act.
     * 
     * @return the act
     */
    public String getAct() {

        return act;
    }

    /**
     * Sets the act.
     * 
     * @param act the new act
     */
    public void setAct(String act) {

        this.act = act;
    }
}
