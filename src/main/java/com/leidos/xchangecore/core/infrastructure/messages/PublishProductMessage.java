package com.leidos.xchangecore.core.infrastructure.messages;

public class PublishProductMessage {

    private String workProduct;
    private String owningCore;

    public PublishProductMessage(String product, String owningCore) {

        setWorkProduct(product);
        setOwningCore(owningCore);
    }

    public String getWorkProduct() {

        return workProduct;
    }

    public void setWorkProduct(String workProduct) {

        this.workProduct = workProduct;
    }

    public String getOwningCore() {

        return owningCore;
    }

    public void setOwningCore(String owningCore) {

        this.owningCore = owningCore;
    }

}
