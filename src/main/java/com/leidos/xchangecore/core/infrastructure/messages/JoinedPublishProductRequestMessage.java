package com.leidos.xchangecore.core.infrastructure.messages;

public class JoinedPublishProductRequestMessage {

    private String act;
    private String interestGroupId;
    private String userID;
    private String owningCore;
    private String requestingCore;
    private String productId;
    private String productType;
    private String workProduct;

    public String getAct() {

        return act;
    }

    public String getInterestGroupId() {

        return interestGroupId;
    }

    public void setInterestGroupId(String interestGroupId) {

        this.interestGroupId = interestGroupId;
    }

    public String getUserID() {

        return userID;
    }

    public void setUserID(String userID) {

        this.userID = userID;
    }

    public String getOwningCore() {

        return owningCore;
    }

    public void setOwningCore(String owningCore) {

        this.owningCore = owningCore;
    }

    public String getRequestingCore() {

        return requestingCore;
    }

    public void setRequestingCore(String requestingCore) {

        this.requestingCore = requestingCore;
    }

    public String getProductId() {

        return productId;
    }

    public void setAct(String act) {

        this.act = act;
    }

    public void setProductId(String productId) {

        this.productId = productId;
    }

    public String getProductType() {

        return productType;
    }

    public void setProductType(String productType) {

        this.productType = productType;
    }

    public String getWorkProduct() {

        return workProduct;
    }

    public void setWorkProduct(String workProduct) {

        this.workProduct = workProduct;
    }
}
