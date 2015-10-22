package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.List;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.ProductSubscriptionByType;

public interface ProductSubscriptionByTypeDAO
    extends GenericDAO<ProductSubscriptionByType, Integer> {

    public List<ProductSubscriptionByType> findByProductType(String productType);

    public List<ProductSubscriptionByType> findByInterestGroupIdAndProductType(String interestGroupID,
                                                                               String productType);

    public List<ProductSubscriptionByType> findByXPath(String xPath);

    public List<ProductSubscriptionByType> findBySubscriberName(String subscriberName);

    public List<ProductSubscriptionByType> findBySubscriptionId(Integer subscriptionId);

}
