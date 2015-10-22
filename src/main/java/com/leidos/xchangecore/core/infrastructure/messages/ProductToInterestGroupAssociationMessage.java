package com.leidos.xchangecore.core.infrastructure.messages;

public class ProductToInterestGroupAssociationMessage {

    public static enum AssociationType {
        Associate, Unassociate,
    };

    private AssociationType associationType;
    private String productId;
    private String productType;
    private String interestGroupId;
    private String owningCore;

    public AssociationType getAssociationType() {

        return associationType;
    }

    public void setAssociationType(AssociationType associationType) {

        this.associationType = associationType;
    }

    public String getProductId() {

        return productId;
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

    public String getInterestGroupId() {

        return interestGroupId;
    }

    public void setInterestGroupId(String interestGroupId) {

        this.interestGroupId = interestGroupId;
    }

    public String getOwningCore() {

        return owningCore;
    }

    public void setOwningCore(String owningCore) {

        this.owningCore = owningCore;
    }

}
