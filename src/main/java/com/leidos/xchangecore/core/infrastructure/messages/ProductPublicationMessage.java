package com.leidos.xchangecore.core.infrastructure.messages;

public class ProductPublicationMessage {

    public static enum PublicationType {
        Publish, Delete,
    };

    private PublicationType pubType;
    private String interestGroupID;
    private String productID;
    private String productType;
    private String product;

    public ProductPublicationMessage(PublicationType pubType, String interestGroupID,
                                     String productID, String productType, String product) {

        setPubType(pubType);
        setInterestGroupID(interestGroupID);
        setProductID(productID);
        setProductType(productType);
        setProduct(product);
    }

    public PublicationType getPubType() {

        return pubType;
    }

    public void setPubType(PublicationType pubType) {

        this.pubType = pubType;
    }

    public String getInterestGroupID() {

        return interestGroupID;
    }

    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    public String getProductID() {

        return productID;
    }

    public void setProductID(String productID) {

        this.productID = productID;
    }

    public String getProductType() {

        return productType;
    }

    public void setProductType(String productType) {

        this.productType = productType;
    }

    public void setProduct(String product) {

        this.product = product;
    }

    public String getProduct() {

        return this.product;
    }
}
